package com.examp.genifit.service;

import com.examp.genifit.dto.request.CreateAdminFoodRequest;
import com.examp.genifit.dto.request.UpdateFoodRequest;
import com.examp.genifit.dto.response.FoodResponse;
import org.springframework.data.domain.Page;

public interface FoodService {

    Page<FoodResponse> getAllFoods(Integer pageNum, Integer pageSize);
    Page<FoodResponse> searchFoods(String keyword, Integer pageNum, Integer pageSize);
    FoodResponse createFoodByAdmin(CreateAdminFoodRequest request);
    FoodResponse updateFood(Integer foodId, UpdateFoodRequest request);
    void softDeleteFood(Integer foodId);
}