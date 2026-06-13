package com.examp.genifit.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodRecommendationResponse {
    private double remainingCalories;
    private String mealType;
    private List<FoodSuggestion> suggestions;

    @Data
    @NoArgsConstructor
    public static class FoodSuggestion {
        private String name;
        private int calories;
        private String description;
        private List<String> ingredients;
        private String warning;
    }
}
