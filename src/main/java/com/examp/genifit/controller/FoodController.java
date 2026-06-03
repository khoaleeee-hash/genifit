package com.examp.genifit.controller;

import com.examp.genifit.dto.request.CreateFoodRequest;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.entity.FoodItem;
import com.examp.genifit.service.FoodService;
import com.examp.genifit.service.serviceImpl.FoodServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor

public class FoodController {

    private final FoodService foodService;

    @GetMapping("/get-All")
    public List<FoodResponse> getAllFoods(){
        return foodService.getAllFoods();
    }

    @GetMapping("/search")
    public List<FoodResponse> searFoods(@RequestParam String keyword){
        return foodService.searchFoods(keyword);
    }

    @PostMapping("/create")
    public FoodResponse createFood(@RequestBody CreateFoodRequest request){
        return foodService.createFood(request);
    }

}
