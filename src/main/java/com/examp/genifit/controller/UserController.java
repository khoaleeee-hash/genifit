package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.AssignSubscriptionRequest;
import com.examp.genifit.dto.request.CreateUserRequest;
import com.examp.genifit.dto.request.GeminiMealSuggestionRequest;
import com.examp.genifit.dto.response.GeminiMealSuggestionResponse;
import com.examp.genifit.dto.response.UserResponse;
import com.examp.genifit.dto.response.UserSubscriptionResponse;
import com.examp.genifit.service.GeminiMealSuggestionService;
import com.examp.genifit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
}