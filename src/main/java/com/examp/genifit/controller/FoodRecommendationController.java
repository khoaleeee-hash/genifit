package com.examp.genifit.controller;

import com.examp.genifit.dto.response.FoodRecommendationResponse;
import com.examp.genifit.service.FoodRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendation")
@RequiredArgsConstructor
public class FoodRecommendationController {

    private final FoodRecommendationService recommendationService;

    // User chủ động bấm nút gợi ý
    @Operation(summary = "Get food recommendation when user clicks the recommendation button")
    @GetMapping("/{userId}")
    public ResponseEntity<FoodRecommendationResponse> getRecommendation(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "bữa chính") String mealType) {

        return ResponseEntity.ok(recommendationService.recommend(userId, mealType));
    }

    // Tự động gọi sau khi log bữa ăn — gọi nội bộ từ LogService
    // hoặc expose endpoint cho frontend gọi ngay sau khi log xong
    @Operation(summary = "Get food recommendation immediately after logging a meal (auto)")
    @GetMapping("/{userId}/after-log")
    public ResponseEntity<FoodRecommendationResponse> getAfterLog(
            @PathVariable Integer userId,
            @RequestParam String mealType) {

        return ResponseEntity.ok(recommendationService.recommend(userId, mealType));
    }
}
