package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.response.FoodRecommendationResponse;
import com.examp.genifit.entity.DailyLog;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserProfile;
import com.examp.genifit.repository.DailyLogRepository;
import com.examp.genifit.repository.UserProfileRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.FoodRecommendationService;
import com.examp.genifit.service.prompt.FoodRecommendationPrompt;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FoodRecommendationServiceImpl implements FoodRecommendationService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DailyLogRepository dailyLogRepository;
    private final FoodRecommendationPrompt recommendationPrompt;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    @Override
    public FoodRecommendationResponse recommend(Integer userId, String mealType) {
        // 1. Lấy thông tin user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        // 2. Lấy DailyLog hôm nay để tính calo còn lại
        DailyLog todayLog = dailyLogRepository
                .findByUserAndLogDate(user, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("Daily log not found"));

        double remaining = todayLog.getTargetCalories() -
                (todayLog.getTotalCalories() != null ? todayLog.getTotalCalories() : 0);

        if (remaining <= 0) {
            // Đã đạt hoặc vượt mục tiêu calo, không cần gợi ý thêm
            return new FoodRecommendationResponse(0, mealType, List.of());
        }

        // 3. Build prompt
        String prompt = recommendationPrompt.build(
                user.getUsername(),
                remaining,
                mealType,
                profile.getWeightKg(),
                profile.getHeightCm(),
                profile.getGoal().name(),
                profile.getMedicalConditions(),
                profile.getAllergies()
        );

        // 4. Gọi Gemini
        String rawJson = callGemini(prompt);

        // 5. Parse JSON response
        List<FoodRecommendationResponse.FoodSuggestion> suggestions = parseResponse(rawJson);

        return new FoodRecommendationResponse(remaining, mealType, suggestions);
    }

    private String callGemini(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("role", "user",
                                "parts", List.of(Map.of("text", prompt)))
                )
        );

        return webClientBuilder.build()
                .post()
                .uri(apiUrl + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(10))
                                .filter(e -> e instanceof WebClientResponseException.TooManyRequests)
                )
                .map(this::extractText)
                .block();
    }

    private String extractText(Map<?, ?> response) {
        try {
            var candidates = (List<?>) response.get("candidates");
            var candidate = (Map<?, ?>) candidates.get(0);
            var content = (Map<?, ?>) candidate.get("content");
            var parts = (List<?>) content.get("parts");
            var part = (Map<?, ?>) parts.get(0);
            return (String) part.get("text");
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract Gemini response");
        }
    }

    private List<FoodRecommendationResponse.FoodSuggestion> parseResponse(String rawJson) {
        try {
            // Gemini đôi khi wrap JSON trong ```json ... ```, cần strip ra
            String cleaned = rawJson
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return objectMapper.readValue(cleaned,
                    objectMapper.getTypeFactory().constructCollectionType(
                            List.class, FoodRecommendationResponse.FoodSuggestion.class));
        } catch (Exception e) {
            return List.of(); // parse lỗi thì trả về rỗng, không crash
        }
    }
}
