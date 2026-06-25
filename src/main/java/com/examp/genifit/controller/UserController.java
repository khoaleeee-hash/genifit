package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.*;
import com.examp.genifit.dto.response.GeminiMealSuggestionResponse;
import com.examp.genifit.dto.response.UserProfileResponse;
import com.examp.genifit.dto.request.AssignSubscriptionRequest;
import com.examp.genifit.dto.request.CreateUserRequest;
import com.examp.genifit.dto.request.GeminiMealSuggestionRequest;
import com.examp.genifit.dto.response.GeminiMealSuggestionResponse;
import com.examp.genifit.dto.response.MySubscriptionResponse;
import com.examp.genifit.dto.response.UserResponse;
import com.examp.genifit.dto.response.UserSubscriptionResponse;
import com.examp.genifit.service.GeminiMealSuggestionService;
import com.examp.genifit.service.SubscriptionPlanService;
import com.examp.genifit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {

    UserService userService;
    GeminiMealSuggestionService geminiMealSuggestionService;
    SubscriptionPlanService subscriptionPlanService;

    @PostMapping("/send-otp")
    public ApiResponse<String> sendOtp(@RequestParam String email) {
        userService.generateAndSendOtp(email);
        return ApiResponse.success(
                "OTP send successfully! Check your mailbox!",
                email);
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ApiResponse.success(
                "Register successfully",
                userService.createUser(request)
        );
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.success(
                "Get my info successfully",
                userService.getMyInfo()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Integer id) {
        return ApiResponse.success(
                "Get user successfully",
                userService.getUser(id)
        );
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.success(
                "Get all users successfully",
                userService.getUsers()
        );
    }

    @GetMapping("/search")
    public ApiResponse<List<UserResponse>> searchUsers(@RequestParam String keyword) {
        return ApiResponse.success(
                "Search users successfully",
                userService.searchUsers(keyword)
        );
    }


    @Operation(
            summary = "Gợi ý món ăn từ nguyên liệu"
    )
    @PostMapping("/from-ingredients")
    public ApiResponse<GeminiMealSuggestionResponse> suggestMealsFromIngredients(
            @RequestBody GeminiMealSuggestionRequest request
    ) {
        return ApiResponse.success(
                "Suggest meals successfully",
                geminiMealSuggestionService.suggestMealsFromIngredients(request)
        );
    }

    @Operation(
            summary = "Lấy gói đăng ký hiện tại của tôi"
    )
    @GetMapping("/my-active")
    public ApiResponse<UserSubscriptionResponse> getMyActiveSubscription() {
        return ApiResponse.success(
                "Get active subscription successfully",
                userService.getMyActiveSubscription()
        );
    }

    @Operation(
            summary = "Lấy lịch sử gói đăng ký của tôi"
    )
    @GetMapping("/my-history")
    public ApiResponse<List<UserSubscriptionResponse>> getMySubscriptionHistory() {
        return ApiResponse.success(
                "Get subscription history successfully",
                userService.getMySubscriptionHistory()
        );
    }

    @Operation(
            summary = "Xem gói đăng ký của tôi"
    )
    @GetMapping("/my-plan")
    public MySubscriptionResponse getMySubscription(Authentication authentication) {
        String username = authentication.getName();

        return subscriptionPlanService.getMySubscription(username);
    }

    @Operation(
            summary = "Huỷ gói đăng ký hiện tại"
    )
    @PatchMapping("/cancel-my-subscription")
    public ApiResponse<String> cancelMySubscription() {
        userService.cancelMySubscription();

        return ApiResponse.success(
                "Cancel subscription successfully",
                "Huỷ gói đăng ký thành công"
        );
    }

    @PostMapping("/forgot-password/send-otp")
    public ApiResponse<String> sendOtpForForgotPassword(@RequestParam String email) {
        userService.generateAndSendOtpForForgotPassword(email);
        return ApiResponse.success(
                "Mã OTP khôi phục mật khẩu đã được gửi vào email của bạn!",
                email
        );
    }

    @PutMapping("/me/password")
    public ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.success(
                "Đổi mật khẩu thành công",
                "Success"
        );
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ApiResponse.success(
                "Khôi phục mật khẩu thành công",
                "Success"
        );
    }

    @DeleteMapping("/me")
    public ApiResponse<String> deleteMyAccount() {
        userService.deleteMe();
        return ApiResponse.success(
                "Tài khoản của bạn đã được xóa",
                "Success"
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUserByAdmin(@PathVariable Integer id) {
        userService.deleteUserById(id);
        return ApiResponse.success(
                "Admin đã xóa tài khoản thành công",
                "Success"
        );
    }

    @PutMapping("/me/profile")
    public ApiResponse<UserProfileResponse> updateMyProfile(@RequestBody UpdateUserProfileRequest request) {
        return ApiResponse.success(
                "Cập nhật hồ sơ sức khỏe thành công",
                userService.updateMyProfile(request)
        );
    }
}