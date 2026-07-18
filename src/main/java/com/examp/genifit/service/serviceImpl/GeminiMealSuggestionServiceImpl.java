package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.dto.request.GeminiMealSuggestionRequest;
import com.examp.genifit.dto.request.IngredientRequest;
import com.examp.genifit.dto.response.GeminiMealSuggestionResponse;
import com.examp.genifit.dto.response.GeminiSuggestedMealResponse;
import com.examp.genifit.entity.DailyLog;
import com.examp.genifit.entity.FoodApprovalStatus;
import com.examp.genifit.entity.FoodItem;
import com.examp.genifit.entity.HealthCondition;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.DailyLogRepository;
import com.examp.genifit.repository.FoodItemRepository;
import com.examp.genifit.repository.GuestRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.GeminiMealSuggestionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiMealSuggestionServiceImpl implements GeminiMealSuggestionService {

    private final UserRepository userRepository;
    private final GuestRepository guestRepository;
    private final DailyLogRepository dailyLogRepository;
    private final FoodItemRepository foodItemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.model}")
    private String geminiModel;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    @Override
    public GeminiMealSuggestionResponse suggestMealsFromIngredients(GeminiMealSuggestionRequest request) {
        validateRequest(request);

        LocalDate today = LocalDate.now();

        double currentCaloriesToday = 0.0;
        double targetCalories = 2000.0;
        String userGoal = "UNKNOWN";

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            userGoal = getUserGoal(user);

            DailyLog dailyLog = dailyLogRepository
                    .findByUser_UserIdAndLogDate(request.getUserId(), today)
                    .orElse(null);

            if (dailyLog != null) {
                currentCaloriesToday = safeDouble(dailyLog.getTotalCalories());

                if (dailyLog.getTargetCalories() != null && dailyLog.getTargetCalories() > 0) {
                    targetCalories = dailyLog.getTargetCalories();
                }
            }

        } else {
            guestRepository.findById(request.getGuestId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy guest"));

            DailyLog dailyLog = dailyLogRepository
                    .findByGuest_GuestIdAndLogDate(request.getGuestId(), today)
                    .orElse(null);

            if (dailyLog != null) {
                currentCaloriesToday = safeDouble(dailyLog.getTotalCalories());

                if (dailyLog.getTargetCalories() != null && dailyLog.getTargetCalories() > 0) {
                    targetCalories = dailyLog.getTargetCalories();
                }
            }

            userGoal = "GUEST";
        }

        double remainingCaloriesToday = targetCalories - currentCaloriesToday;

        List<FoodItem> approvedFoods = foodItemRepository
                .findByIsPublicTrueAndApprovalStatusAndDeletedFalse(FoodApprovalStatus.APPROVED);

        if (approvedFoods.isEmpty()) {
            throw new ApiException(
                    ErrorCode.FOOD_NOT_FOUND,
                    "Không tìm thấy món ăn nào được duyệt"
            );
        }

        String prompt = buildPrompt(
                request,
                approvedFoods,
                userGoal,
                currentCaloriesToday,
                targetCalories,
                remainingCaloriesToday
        );

        String geminiRawResponse = callGeminiTextWithRetry(prompt);

        String aiText = extractTextFromGeminiResponse(geminiRawResponse);
        String cleanJson = cleanJson(aiText);

        try {
            JsonNode root = objectMapper.readTree(cleanJson);

            List<GeminiSuggestedMealResponse> meals =
                    parseSuggestedMeals(root.path("suggestedMeals"));

            String healthCondition = request.getHealthCondition() == null
                    ? HealthCondition.NONE.name()
                    : request.getHealthCondition().name();

            return new GeminiMealSuggestionResponse(
                    root.path("message").asText("Đề xuất món ăn thành công"),
                    request.getUserId(),
                    request.getGuestId(),
                    userGoal,
                    healthCondition,
                    currentCaloriesToday,
                    targetCalories,
                    remainingCaloriesToday,
                    meals,
                    root.path("note").asText("Gợi ý chỉ mang tính tham khảo, không thay thế tư vấn y tế.")
            );

        } catch (Exception e) {
            throw new ApiException(
                    ErrorCode.GEMINI_INVALID_JSON,
                    "Gemini trả về JSON không hợp lệ: " + e.getMessage());
        }
    }

    private String buildPrompt(
            GeminiMealSuggestionRequest request,
            List<FoodItem> approvedFoods,
            String userGoal,
            double currentCaloriesToday,
            double targetCalories,
            double remainingCaloriesToday
    ) {
        StringBuilder ingredientsText = new StringBuilder();

        for (IngredientRequest ingredient : request.getIngredients()) {
            ingredientsText.append("- ")
                    .append(nullToEmpty(ingredient.getIngredient()))
                    .append(", quantity: ")
                    .append(ingredient.getQuantity())
                    .append(", unit: ")
                    .append(nullToEmpty(ingredient.getUnit()))
                    .append("\n");
        }

        StringBuilder foodDatabaseText = new StringBuilder();

        for (FoodItem food : approvedFoods) {
            foodDatabaseText.append("- ")
                    .append("foodId: ").append(food.getFoodId())
                    .append(", foodName: ").append(food.getFoodName())
                    .append(", calories: ").append(safeDouble(food.getCalories()))
                    .append(", protein: ").append(safeDouble(food.getProtein()))
                    .append(", carbs: ").append(safeDouble(food.getCarbs()))
                    .append(", fat: ").append(safeDouble(food.getFat()))
                    .append(", nutritionInfo: ").append(nullToEmpty(food.getNutritionInfo()))
                    .append("\n");
        }

        String healthCondition = request.getHealthCondition() == null
                ? HealthCondition.NONE.name()
                : request.getHealthCondition().name();

        return """
                Bạn là chuyên gia gợi ý bữa ăn cho app GENIFIT.

                Nhiệm vụ:
                - Người dùng nhập nguyên liệu đang có.
                - Bạn phải dựa trên danh sách món ăn chuẩn trong database do admin tạo.
                - Database bên dưới chỉ gồm các món đã public và được admin duyệt.
                - Không được bịa món nằm ngoài database.
                - Chỉ đề xuất món có thể tạo từ nguyên liệu người dùng cung cấp.
                - Phải xét mục tiêu người dùng: LOSE_WEIGHT, GAIN_WEIGHT, MAINTAIN.
                - Nếu healthCondition là DIABETES, cần ưu tiên khẩu phần hợp lý, hạn chế món quá nhiều đường hoặc tinh bột nhanh.
                - Đây chỉ là gợi ý tham khảo, không thay thế tư vấn bác sĩ.

                Profile người dùng:
                - userGoal: %s
                - healthCondition: %s
                - currentCaloriesToday: %.2f
                - targetCalories: %.2f
                - remainingCaloriesToday: %.2f

                Nguyên liệu người dùng đang có:
                %s

                Database món ăn chuẩn:
                %s

                Trả về DUY NHẤT JSON hợp lệ.
                Không markdown.
                Không ```json.
                Không giải thích ngoài JSON.

                Format JSON bắt buộc:
                {
                  "message": "Đề xuất món ăn thành công",
                  "suggestedMeals": [
                    {
                      "mealName": "Tên món ăn",
                      "usedIngredients": ["nguyên liệu 1", "nguyên liệu 2"],
                      "estimatedCalories": 0,
                      "estimatedProtein": 0,
                      "estimatedCarbs": 0,
                      "estimatedFat": 0,
                      "servingSize": "Khẩu phần đề xuất",
                      "suitabilityStatus": "SUITABLE hoặc UNSUITABLE",
                      "suitabilityScore": 0,
                      "reasons": ["Lý do món phù hợp hoặc không phù hợp"],
                      "suggestions": ["Gợi ý điều chỉnh khẩu phần hoặc cách ăn"]
                    }
                  ],
                  "note": "Gợi ý chỉ mang tính tham khảo, không thay thế tư vấn y tế."
                }

                Quy tắc đánh giá:
                - Nếu userGoal là LOSE_WEIGHT: ưu tiên calories vừa phải, protein tốt, hạn chế chất béo cao.
                - Nếu userGoal là GAIN_WEIGHT: ưu tiên đủ calories và protein.
                - Nếu userGoal là MAINTAIN: ưu tiên cân bằng và không vượt quá remainingCaloriesToday quá nhiều.
                - Nếu healthCondition là DIABETES: ưu tiên carbs thấp hoặc vừa phải, khẩu phần nhỏ, hạn chế cơm trắng, nước ngọt, món nhiều đường.
                - suitabilityScore từ 0 đến 100.
                - Nếu món vượt quá remainingCaloriesToday nhiều thì giảm score.
                - Nếu không có món phù hợp, trả suggestedMeals là mảng rỗng và giải thích trong message.
                """.formatted(
                userGoal,
                healthCondition,
                currentCaloriesToday,
                targetCalories,
                remainingCaloriesToday,
                ingredientsText,
                foodDatabaseText
        );
    }

    private String callGeminiTextWithRetry(String prompt) {
        int maxRetries = 3;

        Map<String, Object> textPart = new LinkedHashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("parts", List.of(textPart));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("contents", List.of(content));

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
                    throw new ApiException(
                            ErrorCode.GEMINI_OVERLOADED,
                            "Gemini đang quá tải. Vui lòng thử lại sau.");
                }

                sleepBeforeRetry(attempt);

            } catch (HttpClientErrorException.TooManyRequests e) {
                if (attempt == maxRetries) {
                    throw new ApiException(
                            ErrorCode.GEMINI_RATE_LIMITED,
                            "Gemini đang bị giới hạn request. Vui lòng thử lại sau.");
                }

                sleepBeforeRetry(attempt);
            }
        }

        throw new ApiException(
                ErrorCode.GEMINI_CALL_FAILED,
                "Không gọi được Gemini");
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(2000L * attempt);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new ApiException(
                    ErrorCode.GEMINI_RETRY_INTERRUPTED,
                    "Retry Gemini bị gián đoạn");
        }
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
                throw new ApiException(
                        ErrorCode.GEMINI_INVALID_JSON,
                        "Gemini không trả về nội dung hợp lệ");
            }

            return textNode.asText();

        } catch (Exception e) {
            throw new ApiException(
                    ErrorCode.GEMINI_INVALID_JSON,
                    "Không đọc được response từ Gemini: " + e.getMessage());
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

    private List<GeminiSuggestedMealResponse> parseSuggestedMeals(JsonNode mealsNode) {
        List<GeminiSuggestedMealResponse> meals = new ArrayList<>();

        if (!mealsNode.isArray()) {
            return meals;
        }

        for (JsonNode item : mealsNode) {
            List<String> usedIngredients = new ArrayList<>();
            for (JsonNode ingredient : item.path("usedIngredients")) {
                usedIngredients.add(ingredient.asText());
            }

            List<String> reasons = new ArrayList<>();
            for (JsonNode reason : item.path("reasons")) {
                reasons.add(reason.asText());
            }

            List<String> suggestions = new ArrayList<>();
            for (JsonNode suggestion : item.path("suggestions")) {
                suggestions.add(suggestion.asText());
            }

            GeminiSuggestedMealResponse meal = new GeminiSuggestedMealResponse(
                    item.path("mealName").asText(),
                    usedIngredients,
                    item.path("estimatedCalories").asDouble(0.0),
                    item.path("estimatedProtein").asDouble(0.0),
                    item.path("estimatedCarbs").asDouble(0.0),
                    item.path("estimatedFat").asDouble(0.0),
                    item.path("servingSize").asText(),
                    item.path("suitabilityStatus").asText("UNSUITABLE"),
                    item.path("suitabilityScore").asInt(0),
                    reasons,
                    suggestions
            );

            meals.add(meal);
        }

        return meals;
    }

    private void validateRequest(GeminiMealSuggestionRequest request) {
        if (request.getUserId() == null && request.getGuestId() == null) {
            throw new ApiException(
                    ErrorCode.USER_OR_GUEST_REQUIRED,
                    "Cần truyền userId hoặc guestId");
        }

        if (request.getUserId() != null && request.getGuestId() != null) {
            throw new ApiException(
                    ErrorCode.USER_GUEST_CONFLICT,
                    "Chỉ được truyền userId hoặc guestId, không truyền cả hai");
        }

        if (request.getIngredients() == null || request.getIngredients().isEmpty()) {
            throw new ApiException(
                    ErrorCode.INGREDIENT_REQUIRED,
                    "Cần nhập ít nhất một nguyên liệu");
        }

        for (IngredientRequest ingredient : request.getIngredients()) {
            if (ingredient.getIngredient() == null || ingredient.getIngredient().trim().isEmpty()) {
                throw new ApiException(
                        ErrorCode.INGREDIENT_NAME_REQUIRED,
                        "Tên nguyên liệu không được để trống");
            }

            if (ingredient.getQuantity() != null && ingredient.getQuantity() < 0) {
                throw new ApiException(
                        ErrorCode.INVALID_INGREDIENT_QUANTITY,
                        "Số lượng nguyên liệu không hợp lệ");
            }
        }
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String getUserGoal(User user) {
        if (user.getUserProfile() == null || user.getUserProfile().getGoal() == null) {
            return "UNKNOWN";
        }

        return user.getUserProfile().getGoal().name();
    }
}