package com.examp.genifit.dto.request;

import com.examp.genifit.entity.HealthCondition;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeminiMealSuggestionRequest {
    private Integer userId;
    private Integer guestId;
    private List<IngredientRequest> ingredients;
    private HealthCondition healthCondition;
}
