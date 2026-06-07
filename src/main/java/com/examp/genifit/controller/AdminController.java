package com.examp.genifit.controller;

import com.examp.genifit.dto.request.CreateAdminFoodRequest;
import com.examp.genifit.dto.request.UpdateFoodRequest;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.service.FoodService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/foods")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {

    private final FoodService foodService;

    @PostMapping
    public FoodResponse createFoodByAdmin(@RequestBody CreateAdminFoodRequest request) {
        return foodService.createFoodByAdmin(request);
    }

    @PutMapping("/{foodId}")
    public FoodResponse updateFood(
            @PathVariable Integer foodId,
            @RequestBody UpdateFoodRequest request
    ) {
        return foodService.updateFood(foodId, request);
    }

    @DeleteMapping("/{foodId}")
    public String deleteFood(@PathVariable Integer foodId) {
        foodService.softDeleteFood(foodId);
        return "Xoá món ăn thành công";
    }
}