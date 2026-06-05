package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.response.DetectedFoodItemResponse;
import com.examp.genifit.dto.response.GeminiFoodScanResponse;
import com.examp.genifit.service.GeminiFoodScanService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiFoodScanServiceImpl implements GeminiFoodScanService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.model}")
    private String geminiModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    @Override
    public GeminiFoodScanResponse scanFoodImage(MultipartFile image) {
        try {
            if (image == null || image.isEmpty()) {
                throw new RuntimeException("Vui lòng upload ảnh món ăn");
            }

            String contentType = image.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("File upload phải là ảnh");
            }

            String base64Image = Base64.getEncoder().encodeToString(image.getBytes());

            String prompt = """
                    Bạn là AI chuyên nhận diện món ăn và ước tính dinh dưỡng.
                    
                    Hãy phân tích ảnh món ăn này và trả về DUY NHẤT JSON hợp lệ.
                    Không giải thích thêm, không markdown, không ```json.
                    
                    Format JSON bắt buộc:
                    {
                      "foods": [
                        {
                          "foodName": "Tên món ăn bằng tiếng Việt",
                          "estimatedQuantity": "Khẩu phần ước tính, ví dụ: 1 chén, 1 quả, 100g",
                          "calories": 0,
                          "protein": 0,
                          "carbs": 0,
                          "fat": 0
                        }
                      ],
                      "totalCalories": 0,
                      "totalProtein": 0,
                      "totalCarbs": 0,
                      "totalFat": 0,
                      "confidence": 0.0,
                      "note": "Calories chỉ là ước tính vì ảnh không xác định chính xác khối lượng."
                    }
                    """;

            Map<String, Object> inlineData = new LinkedHashMap<>();
            inlineData.put("mime_type", contentType);
            inlineData.put("data", base64Image);

            Map<String, Object> imagePart = new LinkedHashMap<>();
            imagePart.put("inline_data", inlineData);

            Map<String, Object> textPart = new LinkedHashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> content = new LinkedHashMap<>();
            content.put("parts", List.of(imagePart, textPart));

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("contents", List.of(content));

            String geminiRawResponse = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", geminiModel)
                    .header("x-goog-api-key", geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            String aiText = extractTextFromGeminiResponse(geminiRawResponse);
            String cleanJson = cleanJson(aiText);

            JsonNode aiJson = objectMapper.readTree(cleanJson);

            List<DetectedFoodItemResponse> foods = new ArrayList<>();

            JsonNode foodsNode = aiJson.path("foods");

            if (foodsNode.isArray()) {
                for (JsonNode item : foodsNode) {
                    DetectedFoodItemResponse food = new DetectedFoodItemResponse(
                            item.path("foodName").asText("Không rõ món"),
                            item.path("estimatedQuantity").asText("Không rõ khẩu phần"),
                            item.path("calories").asDouble(0.0),
                            item.path("protein").asDouble(0.0),
                            item.path("carbs").asDouble(0.0),
                            item.path("fat").asDouble(0.0)
                    );

                    foods.add(food);
                }
            }

            return new GeminiFoodScanResponse(
                    "Nhận diện món ăn thành công",
                    foods,
                    aiJson.path("totalCalories").asDouble(0.0),
                    aiJson.path("totalProtein").asDouble(0.0),
                    aiJson.path("totalCarbs").asDouble(0.0),
                    aiJson.path("totalFat").asDouble(0.0),
                    aiJson.path("confidence").asDouble(0.0),
                    aiJson.path("note").asText("Calories chỉ là ước tính bởi AI."),
                    "GEMINI_VISION"
            );

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi scan món ăn bằng Gemini: " + e.getMessage());
        }
    }

    private String extractTextFromGeminiResponse(String geminiRawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(geminiRawResponse);

        JsonNode textNode = root
                .path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new RuntimeException("Gemini không trả về nội dung hợp lệ");
        }

        return textNode.asText();
    }

    private String cleanJson(String text) {
        return text
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
}