package com.examp.genifit.controller;

import com.examp.genifit.dto.request.FoodFilterRequest;
import com.examp.genifit.dto.request.FoodPagingRequest;
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

//    @PostMapping("/get-all")
//    public PageResponse<FoodResponse> getAllFoods(
//            @RequestBody(required = false) FoodPagingRequest request
//    ) {
//        int pageNum = 1;
//        int pageSize = 10;
//
//        if (request != null && request.getPageInfo() != null) {
//            if (request.getPageInfo().getPageNum() != null) {
//                pageNum = request.getPageInfo().getPageNum();
//            }
//
//            if (request.getPageInfo().getPageSize() != null) {
//                pageSize = request.getPageInfo().getPageSize();
//            }
//        }
//
//        return foodService.getAllFoods(pageNum, pageSize);
//    }
    @PostMapping("/search")
    public PageResponse<FoodResponse> filterFoods(
            @RequestBody(required = false) FoodFilterRequest request
    ) {
        return foodService.filterFoods(request);
    }
}