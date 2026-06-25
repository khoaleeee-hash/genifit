package com.examp.genifit.service;

import com.examp.genifit.dto.response.FoodRecommendationResponse;

public interface FoodRecommendationService {
    FoodRecommendationResponse recommend(Integer userId, String mealType);
}
