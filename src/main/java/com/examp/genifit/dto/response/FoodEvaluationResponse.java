package com.examp.genifit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FoodEvaluationResponse {
    private String message;
    private String suitabilityStatus;
    private Integer suitabilityScore;
    private List<String> reasons;
    private List<String> suggestions;
    private List<DetectedFoodItemResponse> foods;
    private Double mealCalories;
    private Double currentCaloriesToday;
    private Double targetCalories;
    private Double remainingCaloriesBeforeMeal;
    private Double totalCaloriesAfterMeal;
    private Double percentOfTargetAfterMeal;
    private String userGoal;
    private Double confidence;
    private String note;
}
