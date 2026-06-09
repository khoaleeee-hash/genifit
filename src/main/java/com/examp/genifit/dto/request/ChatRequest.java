package com.examp.genifit.dto.request;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String userName;
    private int dailyCalorieGoal;
}
