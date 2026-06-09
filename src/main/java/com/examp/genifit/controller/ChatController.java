package com.examp.genifit.controller;

import com.examp.genifit.dto.request.ChatRequest;
import com.examp.genifit.dto.response.ChatHistoryResponse;
import com.examp.genifit.dto.response.ChatResponse;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.ChatHistoryService;
import com.examp.genifit.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final GeminiService geminiService;
    private final ChatHistoryService chatHistoryService;
    private final UserRepository userRepository;

    @PostMapping("/message/{userId}")
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestBody ChatRequest request,
            @PathVariable() Integer userId) {

        String response = geminiService.chat(
                userId,
                request.getMessage(),
                request.getUserName(),
                request.getDailyCalorieGoal()
        );

        return ResponseEntity.ok(new ChatResponse(response));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<ChatHistoryResponse> getHistory(
            @PathVariable() Integer userId,
//            @AuthenticationPrincipal User user,  // lấy từ Security Context
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "20") int pageSize) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return ResponseEntity.ok(chatHistoryService.getHistory(user, cursor, pageSize));
    }
}
