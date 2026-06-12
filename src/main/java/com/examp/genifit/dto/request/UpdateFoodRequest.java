package com.examp.genifit.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateFoodRequest {
    private String foodName;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private String nutritionInfo;
    private Boolean isPublic;
}