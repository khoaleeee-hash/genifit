package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.response.ChatHistoryResponse;
import com.examp.genifit.entity.AIChatHistory;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.AIChatHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatHistoryServiceImplTest {

    @Mock
    private AIChatHistoryRepository chatHistoryRepository;

    @InjectMocks
    private ChatHistoryServiceImpl chatHistoryService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetHistory_NoCursor() {
        // Arrange
        User user = new User();
        user.setUserId(1);

        List<AIChatHistory> historyList = new ArrayList<>();
        AIChatHistory msg1 = new AIChatHistory();
        msg1.setChatId(1);
        msg1.setPrompt("Hello");
        msg1.setResponse("Hi");
        historyList.add(msg1);

        when(chatHistoryRepository.findByUserOrderByChatIdDesc(eq(user), any(Pageable.class)))
                .thenReturn(historyList);

        // Act
        ChatHistoryResponse response = chatHistoryService.getHistory(user, null, 10);

        // Assert
        assertNotNull(response);
        assertFalse(response.isHasMore());
        assertEquals(1, response.getMessages().size());
        assertEquals("Hello", response.getMessages().get(0).getPrompt());
    }

    @Test
    void testGetHistory_WithCursor_HasMore() {
        // Arrange
        User user = new User();
        user.setUserId(1);
        int cursorId = 5;
        int pageSize = 2;

        List<AIChatHistory> historyList = new ArrayList<>();
        for (int i = 4; i >= 2; i--) {
            AIChatHistory msg = new AIChatHistory();
            msg.setChatId(i);
            msg.setPrompt("Prompt " + i);
            msg.setResponse("Response " + i);
            historyList.add(msg);
        }

        when(chatHistoryRepository.findByUserAndChatIdLessThanOrderByChatIdDesc(eq(user), eq(cursorId), any(Pageable.class)))
                .thenReturn(historyList);

        // Act
        ChatHistoryResponse response = chatHistoryService.getHistory(user, cursorId, pageSize);

        // Assert
        assertNotNull(response);
        assertTrue(response.isHasMore());
        assertEquals(2, response.getMessages().size());
        // Reverse order check: 3 should be first, 4 should be second
        assertEquals("Prompt 3", response.getMessages().get(0).getPrompt());
        assertEquals("Prompt 4", response.getMessages().get(1).getPrompt());
        assertEquals(3, response.getNextCursor());
    }
}
