package com.examp.genifit.service;

import com.examp.genifit.dto.request.CreateAdminFoodRequest;
import com.examp.genifit.dto.request.UpdateFoodRequest;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.dto.response.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FoodService {
    PageResponse<FoodResponse> getAllFoods(int pageNum, int pageSize);
    PageResponse<FoodResponse> searchFoods(String keyword, int pageNum, int pageSize);
    FoodResponse createFoodByAdmin(CreateAdminFoodRequest request);
    FoodResponse updateFood(Integer foodId, UpdateFoodRequest request);
    void softDeleteFood(Integer foodId);
}
