package com.examp.genifit.dto.request;

import com.examp.genifit.dto.response.DetectedFoodItemResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter

public class FoodEvaluationRequest {
    private Integer userId;
    private Integer guestId;
    private List<DetectedFoodItemResponse> foods;
    private Double totalCalories;
    private Double totalProtein;
    private Double totalCarbs;
    private Double totalFat;
    private Double confidence;
    private String source;
    private String note;
}
