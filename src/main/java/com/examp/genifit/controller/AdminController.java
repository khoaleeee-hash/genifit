package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.AssignSubscriptionRequest;
import com.examp.genifit.dto.request.CreateAdminFoodRequest;
import com.examp.genifit.dto.request.UpdateFoodRequest;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.dto.response.UserSubscriptionResponse;
import com.examp.genifit.service.FoodService;
import com.examp.genifit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {

    private final FoodService foodService;
    private final UserService userService;

    @Operation(
            summary = "Admin thêm thức ăn"
    )
    @PostMapping("/foods")
    public FoodResponse createFoodByAdmin(@RequestBody CreateAdminFoodRequest request) {
        return foodService.createFoodByAdmin(request);
    }

    @Operation(
            summary = "Admin gán gói đăng ký cho user"
    )
    @PostMapping("/admin/assign")
    public ApiResponse<UserSubscriptionResponse> assignSubscription(
            @RequestBody AssignSubscriptionRequest request
    ) {
        return ApiResponse.success(
                "Assign subscription successfully",
                userService.assignSubscription(request)
        );
    }

    @Operation(
            summary = "Admin cập nhật/chỉnh sửa thức ăn"
    )
    @PutMapping("/foods/{foodId}")
    public FoodResponse updateFood(
            @PathVariable Integer foodId,
            @RequestBody UpdateFoodRequest request
    ) {
        return foodService.updateFood(foodId, request);
    }

    @Operation(
            summary = "Admin xoá mềm thức ăn"
    )
    @DeleteMapping("/foods/{foodId}")
    public String deleteFood(@PathVariable Integer foodId) {
        foodService.softDeleteFood(foodId);
        return "Xoá món ăn thành công";
    }
}