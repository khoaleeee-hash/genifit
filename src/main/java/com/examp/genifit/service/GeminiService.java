package com.examp.genifit.service;

import com.examp.genifit.entity.AIChatHistory;

import java.util.List;
import java.util.Map;

public interface GeminiService {
    String chat(Integer userId, String prompt, String userName, int dailyCalorieGoal);

    // Interface
    Map<String, Object> buildGeminiRequest(List<AIChatHistory> histories, String currentPrompt, String sysPrompt);

    String extractText(Map<?, ?> response);
}
