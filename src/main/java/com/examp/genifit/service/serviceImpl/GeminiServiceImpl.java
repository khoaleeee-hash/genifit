package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.entity.AIChatHistory;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.AIChatHistoryRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.GeminiService;
import com.examp.genifit.service.prompt.NutritionSystemPrompt;
import reactor.util.retry.Retry;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {
    private final WebClient.Builder webClientBuilder;
    private final AIChatHistoryRepository chatMessageRepository;
    private final NutritionSystemPrompt systemPrompt;
    private final UserRepository userRepository;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    @Value("${gemini.max-history}")
    private int maxHistory;

    @Override
    @SuppressWarnings("unchecked")
    public String chat(Integer userId, String prompt, String userName, int dailyCalorieGoal) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<AIChatHistory> history = chatMessageRepository
                .findTopByUserOrderByCreatedAtDesc(user, maxHistory);

        Map<String, Object> requestBody = buildGeminiRequest(
                history,
                prompt,
                systemPrompt.build(userName, dailyCalorieGoal)
        );

        String aiResponse = webClientBuilder.build()
                .post()
                .uri(apiUrl + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(2))  // tự động exponential: 2s → 4s → 8s
                                .filter(e -> e instanceof WebClientResponseException.TooManyRequests)
                                .onRetryExhaustedThrow((spec, signal) ->
                                        new RuntimeException("Gemini API quá tải, thử lại sau nhé!"))
                )
                .map(this::extractText)
                .block();

        saveMessage(user, prompt, aiResponse);
        return aiResponse;
    }

    @Override
    public Map<String, Object> buildGeminiRequest(List<AIChatHistory> histories,
                                                  String currentPrompt,  // thêm param này
                                                  String sysPrompt) {
        List<Map<String, Object>> contents = new ArrayList<>();

        // 1. Đưa lịch sử cũ vào trước
        histories.stream()
                .sorted(Comparator.comparing(AIChatHistory::getCreatedAt))
                .forEach(chat -> {
                    contents.add(Map.of(
                            "role", "user",
                            "parts", List.of(Map.of("text", chat.getPrompt()))
                    ));
                    contents.add(Map.of(
                            "role", "model",
                            "parts", List.of(Map.of("text", chat.getResponse()))
                    ));
                });

        // 2. Thêm tin nhắn hiện tại của user vào cuối — đây là phần đang thiếu
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", currentPrompt))
        ));

        return Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", sysPrompt))
                ),
                "contents", contents
        );
    }

    @Override
    public String extractText(Map<?, ?> response) {
        try {
            var candidates = (List<?>) response.get("candidates");
            var candidate = (Map<?, ?>) candidates.get(0);
            var content = (Map<?, ?>) candidate.get("content");
            var parts = (List<?>) content.get("parts");
            var part = (Map<?, ?>) parts.get(0);
            return (String) part.get("text");
        } catch (Exception e) {
            return "Xin lỗi, mình gặp sự cố kỹ thuật. Bạn thử lại nhé!";
        }
    }

    private void saveMessage(User user, String prompt, String response) {

        AIChatHistory msg = new AIChatHistory();
        msg.setUser(user);
        msg.setPrompt(prompt);
        msg.setResponse(response);
        msg.setCreatedAt(LocalDateTime.now());
        chatMessageRepository.save(msg);
    }
}
