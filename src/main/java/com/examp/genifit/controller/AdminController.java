package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.AssignSubscriptionRequest;
import com.examp.genifit.dto.request.CreateAdminFoodRequest;
import com.examp.genifit.dto.request.UpdateFoodRequest;
import com.examp.genifit.dto.request.UpdateUserProfileRequest;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.dto.response.UserProfileResponse;
import com.examp.genifit.dto.response.UserSubscriptionResponse;
import com.examp.genifit.dto.response.dashboard.DashboardResponse;
import com.examp.genifit.service.DashboardService;
import com.examp.genifit.service.FoodService;
import com.examp.genifit.service.UserService;
import com.examp.genifit.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {

        private final FoodService foodService;
        private final UserService userService;
        private final DashboardService dashboardService;
        private final com.examp.genifit.service.PaymentService paymentService;

        @Operation(summary = "Lấy dữ liệu Dashboard cho Admin")
        @GetMapping("/dashboard")
        public ApiResponse<DashboardResponse> getDashboardData() {
                return ApiResponse.success(
                                "Dashboard data retrieved successfully",
                                dashboardService.getDashboardData());
        }

        @Operation(summary = "Admin thêm thức ăn")
        @PostMapping("/foods")
        public FoodResponse createFoodByAdmin(@RequestBody CreateAdminFoodRequest request) {
                return foodService.createFoodByAdmin(request);
        }

        @Operation(summary = "Admin gán gói đăng ký cho user")
        @PostMapping("/admin/assign")
        public ApiResponse<UserSubscriptionResponse> assignSubscription(
                        @RequestBody AssignSubscriptionRequest request) {
                return ApiResponse.success(
                                "Assign subscription successfully",
                                userService.assignSubscription(request));
        }

        @Operation(summary = "Admin cập nhật/chỉnh sửa thức ăn")
        @PutMapping("/foods/{foodId}")
        public FoodResponse updateFood(
                        @PathVariable Integer foodId,
                        @RequestBody UpdateFoodRequest request) {
                return foodService.updateFood(foodId, request);
        }

        @Operation(summary = "Admin xoá mềm thức ăn")
        @DeleteMapping("/foods/{foodId}")
        public String deleteFood(@PathVariable Integer foodId) {
                foodService.softDeleteFood(foodId);
                return "Xoá món ăn thành công";
        }

        @Operation(summary = "Admin cập nhật hồ sơ sức khoẻ của user")
        @PutMapping("/users/{userId}/profile")
        public ApiResponse<UserProfileResponse> updateUserProfileByAdmin(
                        @PathVariable Integer userId,
                        @RequestBody @Valid UpdateUserProfileRequest request) {
                return ApiResponse.success(
                                "User profile updated successfully",
                                userService.updateUserProfileByAdmin(userId, request));
        }

        @Operation(summary = "Admin khôi phục tài khoản người dùng")
        @PutMapping("/users/{userId}/restore")
        public ApiResponse<Void> restoreUser(@PathVariable Integer userId) {
                userService.restoreUserById(userId);
                return ApiResponse.success("Khôi phục tài khoản thành công", null);
        }

        @Operation(summary = "Admin cập nhật ảnh đại diện cho người dùng")
        @PutMapping("/users/{userId}/avatar")
        public ApiResponse<String> updateUserAvatarByAdmin(
                        @PathVariable Integer userId,
                        @RequestBody @Valid com.examp.genifit.dto.request.UpdateAvatarRequest request) {
                userService.updateAvatarUrlByAdmin(userId, request.getAvatarUrl());
                return ApiResponse.success("Cập nhật ảnh đại diện thành công", request.getAvatarUrl());
        }

        @Operation(summary = "Lấy lịch sử thanh toán của tất cả người dùng (Admin)")
        @GetMapping("/payments/history")
        public ApiResponse<com.examp.genifit.dto.response.AdminPaymentHistoryResponse> getAdminPaymentHistory(
                        @RequestParam(required = false) Integer cursor,
                        @RequestParam(defaultValue = "10") int pageSize) {
                return ApiResponse.success(
                                "Lấy lịch sử thanh toán thành công",
                                paymentService.getAdminHistory(cursor, pageSize));
        }
}