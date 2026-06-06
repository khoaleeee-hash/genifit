package com.examp.genifit.controller;

import com.examp.genifit.dto.request.UpdateFoodRequest;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.service.FoodService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
@Tag(name = "Food")
public class FoodController {

    private final FoodService foodService;

    @GetMapping("/get-all")
    public List<FoodResponse> getAllFoods() {
        return foodService.getAllFoods();
    }

    @GetMapping("/search")
    public List<FoodResponse> searchFoods(@RequestParam String keyword) {
        return foodService.searchFoods(keyword);
    }

}