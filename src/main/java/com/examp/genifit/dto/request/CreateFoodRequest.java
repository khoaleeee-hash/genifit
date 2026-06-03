package com.examp.genifit.dto.request;

import lombok.*;

@Setter
@Getter

public class CreateFoodRequest {
    private String foodName;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private String nutritionInfo;
}
