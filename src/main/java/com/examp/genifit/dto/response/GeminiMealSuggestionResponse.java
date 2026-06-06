package com.examp.genifit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeminiMealSuggestionResponse {
    private String message;
    private Integer userId;
    private Integer guestId;
    private String userGoal;
    private String healthCondition;
    private Double currentCaloriesToday;
    private Double targetCalories;
    private Double remainingCaloriesToday;
    private List<GeminiSuggestedMealResponse> suggestedMeals;
    private String note;
}