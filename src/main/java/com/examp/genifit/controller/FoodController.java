package com.examp.genifit.controller;

import com.examp.genifit.dto.request.FoodSearchRequest;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.dto.response.PageResponse;
import com.examp.genifit.service.FoodService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
@Tag(name = "Food")
public class FoodController {

    private final FoodService foodService;

    @PostMapping("/get-all")
    public PageResponse<FoodResponse> getAllFoods(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return foodService.getAllFoods(pageNum, pageSize);
    }

    @PostMapping("/search")
    public PageResponse<FoodResponse> searchFoods(
            @RequestBody(required = false) FoodSearchRequest request,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        String keyword = null;

        if (request != null) {
            keyword = request.getKeyword();
        }

        return foodService.searchFoods(keyword, pageNum, pageSize);
    }
}