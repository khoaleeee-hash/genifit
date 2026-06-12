package com.examp.genifit.service;

import com.examp.genifit.dto.request.CreateAdminFoodRequest;
import com.examp.genifit.dto.request.UpdateFoodRequest;
import com.examp.genifit.dto.response.FoodResponse;

import java.util.List;

public interface FoodService {
    List<FoodResponse> getAllFoods();
    List<FoodResponse> searchFoods(String keyword);
    FoodResponse createFoodByAdmin(CreateAdminFoodRequest request);
    FoodResponse updateFood(Integer foodId, UpdateFoodRequest request);
    void softDeleteFood(Integer foodId);
}
