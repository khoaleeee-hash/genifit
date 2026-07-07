package com.examp.genifit.dto.response;

import com.examp.genifit.entity.FoodItem;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonPropertyOrder({
        "foodId",
        "foodName",
        "calories",
        "protein",
        "carbs",
        "fat",
        "nutritionInfo",
        "isPublic",
        "isDeleted",
        "approvalStatus",
        "createdByUserId"
})
public class FoodResponse {

    private Integer foodId;
    private String foodName;

    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;

    private String nutritionInfo;
    private Boolean isPublic;
    private Boolean isDeleted;
    private String approvalStatus;
    private Integer createdByUserId;

    public FoodResponse(FoodItem foodItem) {
        this.foodId = foodItem.getFoodId();
        this.foodName = foodItem.getFoodName();

        this.calories = foodItem.getCalories();
        this.protein = foodItem.getProtein();
        this.carbs = foodItem.getCarbs();
        this.fat = foodItem.getFat();

        this.nutritionInfo = foodItem.getNutritionInfo();
        this.isPublic = foodItem.getIsPublic();
        this.isDeleted = foodItem.getDeleted();

        this.approvalStatus = foodItem.getApprovalStatus() == null
                ? null
                : foodItem.getApprovalStatus().name();

        this.createdByUserId = foodItem.getCreatedBy() == null
                ? null
                : foodItem.getCreatedBy().getUserId();
    }
}