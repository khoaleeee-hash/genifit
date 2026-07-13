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
import com.examp.genifit.service.prompt.FoodScanPrompt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiFoodScanServiceImpl
        implements GeminiFoodScanService {

    private final UserRepository userRepository;
    private final GuestRepository guestRepository;
    private final AIScanHistoryRepository aiScanHistoryRepository;

    private final ObjectMapper objectMapper;
    private final RestClient geminiRestClient;
    private final FoodScanPrompt foodScanPrompt;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.model}")
    private String geminiModel;

    @Override
    public GeminiFoodScanResponse scanFoodImage(
            MultipartFile image,
            Integer userId,
            Integer guestId
    ) {
        validateUserOrGuest(userId, guestId);
        validateImage(image);

        try {
            log.info(
                    "Bắt đầu scan ảnh: name={}, type={}, size={} bytes, model={}",
                    image.getOriginalFilename(),
                    image.getContentType(),
                    image.getSize(),
                    geminiModel
            );

            String base64Image = Base64.getEncoder()
                    .encodeToString(image.getBytes());

            String prompt = foodScanPrompt.build();

            Map<String, Object> requestBody =
                    buildGeminiRequestBody(
                            prompt,
                            base64Image,
                            image.getContentType()
                    );

            String geminiRawResponse =
                    callGeminiWithRetry(requestBody);

            String aiText =
                    extractTextFromGeminiResponse(
                            geminiRawResponse
                    );

            String cleanedJson = cleanJson(aiText);

            JsonNode root =
                    objectMapper.readTree(cleanedJson);

            List<DetectedFoodItemResponse> foods =
                    parseFoods(root.path("foods"));

            double totalCalories =
                    getDoubleOrCalculate(
                            root,
                            "totalCalories",
                            foods,
                            "calories"
                    );

            double totalProtein =
                    getDoubleOrCalculate(
                            root,
                            "totalProtein",
                            foods,
                            "protein"
                    );

            double totalCarbs =
                    getDoubleOrCalculate(
                            root,
                            "totalCarbs",
                            foods,
                            "carbs"
                    );

            double totalFat =
                    getDoubleOrCalculate(
                            root,
                            "totalFat",
                            foods,
                            "fat"
                    );

            double confidence =
                    root.path("confidence")
                            .asDouble(0.0);

            confidence = Math.max(
                    0.0,
                    Math.min(confidence, 1.0)
            );

            String note = root.path("note")
                    .asText(
                            "Calories và dinh dưỡng chỉ là "
                                    + "ước lượng từ hình ảnh."
                    );

            String source = root.path("source")
                    .asText("GEMINI_IMAGE_SCAN");

            String detectedFoodText =
                    buildDetectedFoodText(foods);

            SuitabilityStatus suitabilityStatus =
                    calculateSuitabilityStatus(confidence);

            Map<String, Object> nutritionData =
                    new LinkedHashMap<>();

            nutritionData.put("foods", foods);
            nutritionData.put(
                    "totalCalories",
                    totalCalories
            );
            nutritionData.put(
                    "totalProtein",
                    totalProtein
            );
            nutritionData.put(
                    "totalCarbs",
                    totalCarbs
            );
            nutritionData.put(
                    "totalFat",
                    totalFat
            );
            nutritionData.put(
                    "confidence",
                    confidence
            );
            nutritionData.put("note", note);
            nutritionData.put("source", source);
            nutritionData.put(
                    "rawGeminiText",
                    aiText
            );

            String nutritionResult =
                    objectMapper.writeValueAsString(
                            nutritionData
                    );

            AIScanHistory savedHistory =
                    saveScanHistory(
                            userId,
                            guestId,
                            detectedFoodText,
                            totalCalories,
                            nutritionResult,
                            suitabilityStatus
                    );

            log.info(
                    "Scan món ăn thành công, scanId={}",
                    savedHistory.getScanId()
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

        } catch (RuntimeException e) {
            log.error(
                    "Lỗi khi scan món ăn bằng Gemini: {}",
                    e.getMessage(),
                    e
            );

            throw e;

        } catch (Exception e) {
            log.error(
                    "Lỗi không xác định khi scan món ăn",
                    e
            );

            throw new RuntimeException(
                    "Lỗi khi scan món ăn bằng Gemini: "
                            + getRootErrorMessage(e),
                    e
            );
        }
    }

    private Map<String, Object> buildGeminiRequestBody(
            String prompt,
            String base64Image,
            String contentType
    ) {
        if (contentType == null
                || contentType.isBlank()) {
            contentType =
                    MediaType.IMAGE_JPEG_VALUE;
        }

        Map<String, Object> textPart =
                new LinkedHashMap<>();

        textPart.put("text", prompt);

        Map<String, Object> inlineData =
                new LinkedHashMap<>();

        inlineData.put(
                "mimeType",
                contentType
        );

        inlineData.put(
                "data",
                base64Image
        );

        Map<String, Object> imagePart =
                new LinkedHashMap<>();

        imagePart.put(
                "inlineData",
                inlineData
        );

        Map<String, Object> content =
                new LinkedHashMap<>();

        content.put(
                "role",
                "user"
        );

        content.put(
                "parts",
                List.of(textPart, imagePart)
        );

        Map<String, Object> generationConfig =
                new LinkedHashMap<>();

        generationConfig.put(
                "temperature",
                0.2
        );

        generationConfig.put(
                "responseMimeType",
                "application/json"
        );

        Map<String, Object> requestBody =
                new LinkedHashMap<>();

        requestBody.put(
                "contents",
                List.of(content)
        );

        requestBody.put(
                "generationConfig",
                generationConfig
        );

        return requestBody;
    }

    private String callGeminiWithRetry(
            Map<String, Object> requestBody
    ) {
        int maxAttempts = 3;

        for (
                int attempt = 1;
                attempt <= maxAttempts;
                attempt++
        ) {
            try {
                log.info(
                        "Gọi Gemini API lần {}/{}",
                        attempt,
                        maxAttempts
                );

                String response =
                        geminiRestClient.post()
                                .uri(
                                        "/v1beta/models/{model}:generateContent",
                                        geminiModel
                                )
                                .header(
                                        "x-goog-api-key",
                                        geminiApiKey
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .accept(
                                        MediaType.APPLICATION_JSON
                                )
                                .body(requestBody)
                                .retrieve()
                                .body(String.class);

                if (response == null
                        || response.isBlank()) {
                    throw new RuntimeException(
                            "Gemini trả về response rỗng"
                    );
                }

                return response;

            } catch (
                    HttpClientErrorException
                            .TooManyRequests e
            ) {
                log.warn(
                        "Gemini trả về 429 ở lần gọi {}",
                        attempt
                );

                if (attempt == maxAttempts) {
                    throw new RuntimeException(
                            "Gemini đang bị giới hạn request. "
                                    + "Vui lòng thử lại sau.",
                            e
                    );
                }

                sleepBeforeRetry(attempt);

            } catch (HttpServerErrorException e) {
                log.warn(
                        "Gemini trả về lỗi server {} ở lần gọi {}",
                        e.getStatusCode().value(),
                        attempt
                );

                if (attempt == maxAttempts) {
                    throw new RuntimeException(
                            "Gemini đang gặp lỗi server "
                                    + e.getStatusCode().value(),
                            e
                    );
                }

                sleepBeforeRetry(attempt);

            } catch (ResourceAccessException e) {
                log.warn(
                        "Timeout hoặc lỗi kết nối Gemini ở lần gọi {}: {}",
                        attempt,
                        getRootErrorMessage(e)
                );

                if (attempt == maxAttempts) {
                    throw new RuntimeException(
                            "Gemini phản hồi quá lâu hoặc "
                                    + "không thể kết nối. "
                                    + "Hãy kiểm tra mạng, VPN, "
                                    + "firewall hoặc DNS.",
                            e
                    );
                }

                sleepBeforeRetry(attempt);

            } catch (HttpClientErrorException e) {
                throw new RuntimeException(
                        buildClientErrorMessage(e),
                        e
                );
            }
        }

        throw new RuntimeException(
                "Không gọi được Gemini API"
        );
    }

    private String buildClientErrorMessage(
            HttpClientErrorException e
    ) {
        int status =
                e.getStatusCode().value();

        return switch (status) {
            case 400 ->
                    "Gemini từ chối request vì dữ liệu không hợp lệ: "
                            + e.getResponseBodyAsString();

            case 401 ->
                    "Gemini API key không hợp lệ hoặc bị thiếu.";

            case 403 ->
                    "Gemini API key không có quyền truy cập "
                            + "hoặc API chưa được bật.";

            case 404 ->
                    "Không tìm thấy model Gemini: "
                            + geminiModel;

            default ->
                    "Gemini API trả về lỗi "
                            + status
                            + ": "
                            + e.getResponseBodyAsString();
        };
    }

    private List<DetectedFoodItemResponse> parseFoods(
            JsonNode foodsNode
    ) {
        List<DetectedFoodItemResponse> foods =
                new ArrayList<>();

        if (foodsNode == null
                || !foodsNode.isArray()) {
            return foods;
        }

        for (JsonNode item : foodsNode) {
            String foodName =
                    item.path("foodName")
                            .asText("Không xác định");

            double calories =
                    positiveOrZero(
                            item.path("calories")
                                    .asDouble(0.0)
                    );

            double protein =
                    positiveOrZero(
                            item.path("protein")
                                    .asDouble(0.0)
                    );

            double carbs =
                    positiveOrZero(
                            item.path("carbs")
                                    .asDouble(0.0)
                    );

            double fat =
                    positiveOrZero(
                            item.path("fat")
                                    .asDouble(0.0)
                    );

            double quantity =
                    item.path("quantity")
                            .asDouble(1.0);

            if (quantity <= 0) {
                quantity = 1.0;
            }

            String unit =
                    item.path("unit")
                            .asText("phần");

            DetectedFoodItemResponse food =
                    new DetectedFoodItemResponse(
                            foodName,
                            calories,
                            protein,
                            carbs,
                            fat,
                            quantity,
                            unit
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
        if (root.has(fieldName)
                && root.path(fieldName).isNumber()) {
            return positiveOrZero(
                    root.path(fieldName)
                            .asDouble(0.0)
            );
        }

        double total = 0.0;

        for (DetectedFoodItemResponse food : foods) {
            switch (foodField) {
                case "calories" ->
                        total += safeDouble(
                                food.getCalories()
                        );

                case "protein" ->
                        total += safeDouble(
                                food.getProtein()
                        );

                case "carbs" ->
                        total += safeDouble(
                                food.getCarbs()
                        );

                case "fat" ->
                        total += safeDouble(
                                food.getFat()
                        );

                default -> {
                }
            }
        }

        return positiveOrZero(total);
    }

    private String buildDetectedFoodText(
            List<DetectedFoodItemResponse> foods
    ) {
        if (foods == null || foods.isEmpty()) {
            return "Không xác định";
        }

        return foods.stream()
                .map(
                        DetectedFoodItemResponse::getFoodName
                )
                .filter(
                        name ->
                                name != null
                                        && !name.isBlank()
                )
                .distinct()
                .reduce(
                        (first, second) ->
                                first + ", " + second
                )
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
                    .orElseThrow(
                            () -> new RuntimeException(
                                    "Không tìm thấy user có id: "
                                            + userId
                            )
                    );
        }

        if (guestId != null) {
            guest = guestRepository.findById(guestId)
                    .orElseThrow(
                            () -> new RuntimeException(
                                    "Không tìm thấy guest có id: "
                                            + guestId
                            )
                    );
        }

        AIScanHistory history =
                AIScanHistory.builder()
                        .user(user)
                        .guest(guest)
                        .imageUrl(null)
                        .detectedFood(detectedFood)
                        .estimatedCalories(
                                estimatedCalories
                        )
                        .nutritionResult(
                                nutritionResult
                        )
                        .suitabilityStatus(
                                suitabilityStatus
                        )
                        .build();

        return aiScanHistoryRepository.save(
                history
        );
    }

    private SuitabilityStatus calculateSuitabilityStatus(
            Double confidence
    ) {
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

    private String extractTextFromGeminiResponse(
            String geminiRawResponse
    ) {
        try {
            if (geminiRawResponse == null
                    || geminiRawResponse.isBlank()) {
                throw new RuntimeException(
                        "Response Gemini bị rỗng"
                );
            }

            JsonNode root =
                    objectMapper.readTree(
                            geminiRawResponse
                    );

            JsonNode candidates =
                    root.path("candidates");

            if (!candidates.isArray()
                    || candidates.isEmpty()) {
                throw new RuntimeException(
                        "Gemini không trả về candidates. "
                                + "promptFeedback="
                                + root.path("promptFeedback")
                );
            }

            JsonNode firstCandidate =
                    candidates.path(0);

            String finishReason =
                    firstCandidate.path("finishReason")
                            .asText("");

            JsonNode textNode =
                    firstCandidate
                            .path("content")
                            .path("parts")
                            .path(0)
                            .path("text");

            if (textNode.isMissingNode()
                    || textNode.asText().isBlank()) {
                throw new RuntimeException(
                        "Gemini không trả về nội dung hợp lệ. "
                                + "finishReason="
                                + finishReason
                );
            }

            return textNode.asText();

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Không đọc được response từ Gemini: "
                            + getRootErrorMessage(e),
                    e
            );
        }
    }

    private String cleanJson(
            String text
    ) {
        if (text == null || text.isBlank()) {
            throw new RuntimeException(
                    "Nội dung Gemini trả về bị rỗng"
            );
        }

        String cleaned = text
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```", "")
                .trim();

        int firstBrace =
                cleaned.indexOf("{");

        int lastBrace =
                cleaned.lastIndexOf("}");

        if (firstBrace >= 0
                && lastBrace > firstBrace) {
            return cleaned.substring(
                    firstBrace,
                    lastBrace + 1
            );
        }

        throw new RuntimeException(
                "Gemini không trả về JSON hợp lệ: "
                        + cleaned
        );
    }

    private void validateUserOrGuest(
            Integer userId,
            Integer guestId
    ) {
        if (userId == null && guestId == null) {
            throw new IllegalArgumentException(
                    "Cần truyền userId hoặc guestId"
            );
        }

        if (userId != null && guestId != null) {
            throw new IllegalArgumentException(
                    "Chỉ được truyền userId hoặc guestId, "
                            + "không truyền cả hai"
            );
        }
    }

    private void validateImage(
            MultipartFile image
    ) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ảnh không được để trống"
            );
        }

        long maxSize =
                5L * 1024 * 1024;

        if (image.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    "Ảnh không được lớn hơn 5MB"
            );
        }

        String contentType =
                image.getContentType();

        if (contentType == null
                || contentType.isBlank()) {
            throw new IllegalArgumentException(
                    "Không xác định được định dạng ảnh"
            );
        }

        boolean supported =
                MediaType.IMAGE_JPEG_VALUE
                        .equalsIgnoreCase(contentType)
                        || MediaType.IMAGE_PNG_VALUE
                        .equalsIgnoreCase(contentType)
                        || "image/webp"
                        .equalsIgnoreCase(contentType);

        if (!supported) {
            throw new IllegalArgumentException(
                    "Chỉ hỗ trợ ảnh JPG, JPEG, PNG hoặc WEBP"
            );
        }
    }

    private void sleepBeforeRetry(
            int attempt
    ) {
        long delay =
                2000L * attempt;

        log.info(
                "Chờ {} ms trước khi gọi lại Gemini",
                delay
        );

        try {
            Thread.sleep(delay);

        } catch (InterruptedException e) {
            Thread.currentThread()
                    .interrupt();

            throw new RuntimeException(
                    "Quá trình retry Gemini bị gián đoạn",
                    e
            );
        }
    }

    private double safeDouble(
            Double value
    ) {
        if (value == null || value < 0) {
            return 0.0;
        }

        return value;
    }

    private double positiveOrZero(
            double value
    ) {
        if (Double.isNaN(value)
                || Double.isInfinite(value)
                || value < 0) {
            return 0.0;
        }

        return value;
    }

    private String getRootErrorMessage(
            Throwable throwable
    ) {
        Throwable current = throwable;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        String message = current.getMessage();

        if (message == null || message.isBlank()) {
            return current.getClass()
                    .getSimpleName();
        }

        return message;
    }
}