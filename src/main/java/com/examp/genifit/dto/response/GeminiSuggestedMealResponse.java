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
public class GeminiSuggestedMealResponse {
    private String mealName;
    private List<String> usedIngredients;
    private Double estimatedCalories;
    private Double estimatedProtein;
    private Double estimatedCarbs;
    private Double estimatedFat;
    private String servingSize;
    private String suitabilityStatus;
    private Integer suitabilityScore;
    private List<String> reasons;
    private List<String> suggestions;
}
