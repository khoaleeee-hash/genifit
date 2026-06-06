package com.examp.genifit.service;

import com.examp.genifit.dto.request.GeminiMealSuggestionRequest;
import com.examp.genifit.dto.response.GeminiMealSuggestionResponse;
import com.examp.genifit.dto.response.GeminiSuggestedMealResponse;

public interface GeminiMealSuggestionService {
    GeminiMealSuggestionResponse suggestMealsFromIngredients(GeminiMealSuggestionRequest request);
}
