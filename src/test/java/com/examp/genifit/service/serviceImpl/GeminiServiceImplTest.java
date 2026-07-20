package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.entity.AIChatHistory;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.AIChatHistoryRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.prompt.NutritionSystemPrompt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceImplTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private AIChatHistoryRepository chatMessageRepository;

    @Mock
    private NutritionSystemPrompt systemPrompt;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GeminiServiceImpl geminiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(geminiService, "geminiModel", "test-model");
        ReflectionTestUtils.setField(geminiService, "maxHistory", 5);
    }

    @Test
    void testBuildGeminiRequest() {
        // Arrange
        List<AIChatHistory> histories = new ArrayList<>();
        AIChatHistory history = new AIChatHistory();
        history.setPrompt("Old Prompt");
        history.setResponse("Old Response");
        history.setCreatedAt(LocalDateTime.now().minusDays(1));
        histories.add(history);

        String currentPrompt = "New Prompt";
        String sysPrompt = "System Instruction";

        // Act
        Map<String, Object> request = geminiService.buildGeminiRequest(histories, currentPrompt, sysPrompt);

        // Assert
        assertNotNull(request);
        assertTrue(request.containsKey("contents"));
        assertTrue(request.containsKey("system_instruction"));
        
        List<Map<String, Object>> contents = (List<Map<String, Object>>) request.get("contents");
        assertEquals(3, contents.size()); // 1 user history, 1 model history, 1 current user prompt
        
        assertEquals("user", contents.get(0).get("role"));
        assertEquals("model", contents.get(1).get("role"));
        assertEquals("user", contents.get(2).get("role"));
    }

    @Test
    void testExtractText_Success() {
        // Arrange
        Map<String, Object> part = Map.of("text", "Hello World");
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> candidate = Map.of("content", content);
        Map<String, Object> response = Map.of("candidates", List.of(candidate));

        // Act
        String result = geminiService.extractText(response);

        // Assert
        assertEquals("Hello World", result);
    }

    @Test
    void testExtractText_Failure() {
        // Arrange
        Map<String, Object> invalidResponse = Map.of("invalid", "format");

        // Act
        String result = geminiService.extractText(invalidResponse);

        // Assert
        assertEquals("Xin lỗi, mình gặp sự cố kỹ thuật. Bạn thử lại nhé!", result);
    }
}
