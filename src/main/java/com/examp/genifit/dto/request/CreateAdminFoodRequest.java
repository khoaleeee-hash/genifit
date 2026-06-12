package com.examp.genifit.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateAdminFoodRequest {
    private Integer adminId;
    private String foodName;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private String nutritionInfo;
}
