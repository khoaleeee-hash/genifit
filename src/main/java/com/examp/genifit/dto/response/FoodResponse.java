package com.examp.genifit.dto.response;

import com.examp.genifit.entity.FoodItem;
import lombok.*;

@Setter
@Getter

public class FoodResponse {
    private Integer foodId;
    private String foodName;
    private Double calories;
    private Double carbs;
    private Double fat;
    private String nutritionInfo;
    private Boolean isPublic;
    private String approvalStatus;
    private Integer createdByUserId;

    public FoodResponse(FoodItem foodItem) {
        this.foodId = foodItem.getFoodId();
        this.foodName = foodItem.getFoodName();
        this.calories = foodItem.getCalories();
        this.carbs = foodItem.getCarbs();
        this.fat = foodItem.getFat();
        this.nutritionInfo = foodItem.getNutritionInfo();
        this.isPublic = foodItem.getIsPublic();
        this.approvalStatus = foodItem.getApprovalStatus() == null
                ? null
                : foodItem.getApprovalStatus().name();
        this.createdByUserId = foodItem.getCreatedBy() == null
                ? null
                : foodItem.getCreatedBy().getUserId();
    }
}
