package com.examp.genifit.service;

import com.examp.genifit.dto.request.CreateFoodRequest;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.entity.FoodItem;

import java.util.List;

public interface FoodService {
    List<FoodResponse> getAllFoods();
    List<FoodResponse> searchFoods(String keyword);
    FoodResponse createFood(CreateFoodRequest request);
}
