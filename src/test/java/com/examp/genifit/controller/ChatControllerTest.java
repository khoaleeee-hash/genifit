package com.examp.genifit.controller;

import com.examp.genifit.entity.User;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.ChatHistoryService;
import com.examp.genifit.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {
    @Mock private GeminiService geminiService;
    @Mock private ChatHistoryService chatHistoryService;
    @Mock private UserRepository userRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(geminiService, chatHistoryService, userRepository)).build();
    }

    @Test
    void sendMessage_bindsAllChatArguments() throws Exception {
        when(geminiService.chat(4, "Hello", "An", 2000)).thenReturn("Hi An");

        mockMvc.perform(post("/api/chat/message/{userId}", 4).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Hello\",\"userName\":\"An\",\"dailyCalorieGoal\":2000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hi An"));

        verify(geminiService).chat(4, "Hello", "An", 2000);
    }

    @Test
    void getHistory_usesUserAndDefaultPageSize() throws Exception {
        User user = User.builder().userId(4).username("an").build();
        when(userRepository.findById(4)).thenReturn(Optional.of(user));
        when(chatHistoryService.getHistory(user, null, 20)).thenReturn(null);

        mockMvc.perform(get("/api/chat/history/{userId}", 4))
                .andExpect(status().isOk());

        verify(chatHistoryService).getHistory(user, null, 20);
    }
}
