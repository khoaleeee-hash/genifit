package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.response.DetectedFoodItemResponse;
import com.examp.genifit.dto.response.GeminiFoodScanResponse;
import com.examp.genifit.entity.AIScanHistory;
import com.examp.genifit.entity.Guest;
import com.examp.genifit.entity.SuitabilityStatus;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.AIScanHistoryRepository;
import com.examp.genifit.repository.GuestRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.GeminiFoodScanService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
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

    private final UserRepository userRepository;
    private final GuestRepository guestRepository;
    private final AIScanHistoryRepository aiScanHistoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.model}")
    private String geminiModel;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    @Override
    public GeminiFoodScanResponse scanFoodImage(MultipartFile image, Integer userId, Integer guestId) {
        validateUserOrGuest(userId, guestId);
        validateImage(image);

        try {
            String base64Image = Base64.getEncoder().encodeToString(image.getBytes());

            String prompt = buildPrompt();

            Map<String, Object> requestBody = buildGeminiRequestBody(
                    prompt,
                    base64Image,
                    image.getContentType()
            );

            String geminiRawResponse = callGeminiWithRetry(requestBody);

            String aiText = extractTextFromGeminiResponse(geminiRawResponse);

            String cleanJson = cleanJson(aiText);

            JsonNode root = objectMapper.readTree(cleanJson);

            List<DetectedFoodItemResponse> foods = parseFoods(root.path("foods"));

            double totalCalories = getDoubleOrCalculate(root, "totalCalories", foods, "calories");
            double totalProtein = getDoubleOrCalculate(root, "totalProtein", foods, "protein");
            double totalCarbs = getDoubleOrCalculate(root, "totalCarbs", foods, "carbs");
            double totalFat = getDoubleOrCalculate(root, "totalFat", foods, "fat");

            double confidence = root.path("confidence").asDouble(0.0);

            String note = root.path("note").asText("Calories và dinh dưỡng chỉ là ước lượng từ hình ảnh.");
            String source = root.path("source").asText("GEMINI_IMAGE_SCAN");

            String detectedFoodText = buildDetectedFoodText(foods);

            SuitabilityStatus suitabilityStatus = calculateSuitabilityStatus(confidence);

            String nutritionResult = objectMapper.writeValueAsString(Map.of(
                    "foods", foods,
                    "totalCalories", totalCalories,
                    "totalProtein", totalProtein,
                    "totalCarbs", totalCarbs,
                    "totalFat", totalFat,
                    "confidence", confidence,
                    "note", note,
                    "source", source,
                    "rawGeminiText", aiText
            ));

            AIScanHistory savedHistory = saveScanHistory(
                    userId,
                    guestId,
                    detectedFoodText,
                    totalCalories,
                    nutritionResult,
                    suitabilityStatus
            );

            return new GeminiFoodScanResponse(
                    savedHistory.getScanId(),
                    "Scan món ăn thành công và đã lưu vào database",
                    foods,
                    totalCalories,
                    totalProtein,
                    totalCarbs,
                    totalFat,
                    confidence,
                    note,
                    source
            );

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi scan món ăn bằng Gemini: " + e.getMessage());
        }
    }

    private String buildPrompt() {
        return """
                Bạn là AI nhận diện món ăn cho app GENIFIT.

                Hãy phân tích hình ảnh món ăn.
                Một ảnh có thể có một hoặc nhiều món ăn.
                Trả về DUY NHẤT JSON hợp lệ.
                Không markdown.
                Không ```json.
                Không giải thích ngoài JSON.

                Format JSON bắt buộc:
                {
                  "message": "Scan món ăn thành công",
                  "foods": [
                    {
                      "foodName": "Tên món ăn",
                      "calories": 0,
                      "protein": 0,
                      "carbs": 0,
                      "fat": 0,
                      "quantity": 1,
                      "unit": "phần"
                    }
                  ],
                  "totalCalories": 0,
                  "totalProtein": 0,
                  "totalCarbs": 0,
                  "totalFat": 0,
                  "confidence": 0.0,
                  "note": "Calories và dinh dưỡng chỉ là ước lượng từ hình ảnh.",
                  "source": "GEMINI_IMAGE_SCAN"
                }

                Quy tắc:
                - Nếu ảnh có nhiều món, trả tất cả món trong mảng foods.
                - calories/protein/carbs/fat là giá trị ước lượng theo khẩu phần nhìn thấy trong ảnh.
                - totalCalories là tổng calories của tất cả món.
                - confidence từ 0.0 đến 1.0.
                - Nếu không nhận diện được, foods là mảng rỗng, totalCalories/protein/carbs/fat là 0, confidence thấp.
                """;
    }

    private Map<String, Object> buildGeminiRequestBody(
            String prompt,
            String base64Image,
            String contentType
    ) {
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.IMAGE_JPEG_VALUE;
        }

        Map<String, Object> textPart = new LinkedHashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> inlineData = new LinkedHashMap<>();
        inlineData.put("mimeType", contentType);
        inlineData.put("data", base64Image);

        Map<String, Object> imagePart = new LinkedHashMap<>();
        imagePart.put("inlineData", inlineData);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("parts", List.of(textPart, imagePart));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("contents", List.of(content));

        return requestBody;
    }

    private String callGeminiWithRetry(Map<String, Object> requestBody) {
        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return restClient.post()
                        .uri("/v1beta/models/{model}:generateContent", geminiModel)
                        .header("x-goog-api-key", geminiApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);

            } catch (HttpServerErrorException.ServiceUnavailable e) {
                if (attempt == maxRetries) {
                    throw new RuntimeException("Gemini đang quá tải. Vui lòng thử lại sau.");
                }

                sleepBeforeRetry(attempt);

            } catch (HttpClientErrorException.TooManyRequests e) {
                if (attempt == maxRetries) {
                    throw new RuntimeException("Gemini đang bị giới hạn request. Vui lòng thử lại sau.");
                }

                sleepBeforeRetry(attempt);
            }
        }

        throw new RuntimeException("Không gọi được Gemini");
    }

    private List<DetectedFoodItemResponse> parseFoods(JsonNode foodsNode) {
        List<DetectedFoodItemResponse> foods = new ArrayList<>();

        if (!foodsNode.isArray()) {
            return foods;
        }

        for (JsonNode item : foodsNode) {
            DetectedFoodItemResponse food = new DetectedFoodItemResponse(
                    item.path("foodName").asText("Không xác định"),
                    item.path("calories").asDouble(0.0),
                    item.path("protein").asDouble(0.0),
                    item.path("carbs").asDouble(0.0),
                    item.path("fat").asDouble(0.0),
                    item.path("quantity").asDouble(1.0),
                    item.path("unit").asText("phần")
            );

            foods.add(food);
        }

        return foods;
    }

    private double getDoubleOrCalculate(
            JsonNode root,
            String fieldName,
            List<DetectedFoodItemResponse> foods,
            String foodField
    ) {
        if (root.has(fieldName) && root.path(fieldName).isNumber()) {
            return root.path(fieldName).asDouble(0.0);
        }

        double total = 0.0;

        for (DetectedFoodItemResponse food : foods) {
            switch (foodField) {
                case "calories" -> total += safeDouble(food.getCalories());
                case "protein" -> total += safeDouble(food.getProtein());
                case "carbs" -> total += safeDouble(food.getCarbs());
                case "fat" -> total += safeDouble(food.getFat());
                default -> {
                }
            }
        }

        return total;
    }

    private String buildDetectedFoodText(List<DetectedFoodItemResponse> foods) {
        if (foods == null || foods.isEmpty()) {
            return "Không xác định";
        }

        return foods.stream()
                .map(DetectedFoodItemResponse::getFoodName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("Không xác định");
    }

    private AIScanHistory saveScanHistory(
            Integer userId,
            Integer guestId,
            String detectedFood,
            Double estimatedCalories,
            String nutritionResult,
            SuitabilityStatus suitabilityStatus
    ) {
        User user = null;
        Guest guest = null;

        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        }

        if (guestId != null) {
            guest = guestRepository.findById(guestId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy guest"));
        }

        AIScanHistory history = AIScanHistory.builder()
                .user(user)
                .guest(guest)
                .imageUrl(null)
                .detectedFood(detectedFood)
                .estimatedCalories(estimatedCalories)
                .nutritionResult(nutritionResult)
                .suitabilityStatus(suitabilityStatus)
                .build();

        return aiScanHistoryRepository.save(history);
    }

    private SuitabilityStatus calculateSuitabilityStatus(Double confidence) {
        if (confidence == null) {
            return SuitabilityStatus.UNKNOWN;
        }

        if (confidence >= 0.75) {
            return SuitabilityStatus.SUITABLE;
        }

        if (confidence >= 0.4) {
            return SuitabilityStatus.WARNING;
        }

        return SuitabilityStatus.UNSUITABLE;
    }

    private String extractTextFromGeminiResponse(String geminiRawResponse) {
        try {
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

        } catch (Exception e) {
            throw new RuntimeException("Không đọc được response từ Gemini: " + e.getMessage());
        }
    }

    private String cleanJson(String text) {
        String cleaned = text
                .replace("```json", "")
                .replace("```", "")
                .trim();

        int firstBrace = cleaned.indexOf("{");
        int lastBrace = cleaned.lastIndexOf("}");

        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return cleaned.substring(firstBrace, lastBrace + 1);
        }

        return cleaned;
    }

    private void validateUserOrGuest(Integer userId, Integer guestId) {
        if (userId == null && guestId == null) {
            throw new RuntimeException("Cần truyền userId hoặc guestId");
        }

        if (userId != null && guestId != null) {
            throw new RuntimeException("Chỉ được truyền userId hoặc guestId, không truyền cả hai");
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new RuntimeException("Ảnh không được để trống");
        }

        String contentType = image.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("File upload phải là hình ảnh");
        }
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(2000L * attempt);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry Gemini bị gián đoạn");
        }
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }
}