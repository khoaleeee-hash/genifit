package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.service.FoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
@Tag(name = "Food")
public class FoodController {

    private final FoodService foodService;

    @Operation(
            summary = "Lấy danh sách tất cả food"
    )
    @GetMapping("/get-all")
    public ResponseEntity<ApiResponse<List<FoodResponse>>> getAllFoods(
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<FoodResponse> page = foodService.getAllFoods(pageNum, pageSize);

        return ResponseEntity.ok(
                ApiResponse.successPage("Get all foods successfully", page)
        );
    }

    @Operation(
            summary = "Tìm kiếm thức ăn"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FoodResponse>>> searchFoods(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<FoodResponse> page = foodService.searchFoods(keyword, pageNum, pageSize);

        return ResponseEntity.ok(
                ApiResponse.successPage("Search foods successfully", page)
        );
    }

}