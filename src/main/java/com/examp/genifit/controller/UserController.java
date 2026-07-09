package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.*;
import com.examp.genifit.dto.response.*;
import com.examp.genifit.dto.request.CreateUserRequest;
import com.examp.genifit.dto.request.GeminiMealSuggestionRequest;
import com.examp.genifit.dto.response.GeminiMealSuggestionResponse;
import com.examp.genifit.dto.response.MySubscriptionResponse;
import com.examp.genifit.dto.response.UserResponse;
import com.examp.genifit.dto.response.UserSubscriptionResponse;
import com.examp.genifit.entity.UserSubscription;
import com.examp.genifit.service.AuthenticationService;
import com.examp.genifit.service.GeminiMealSuggestionService;
import com.examp.genifit.service.SubscriptionPlanService;
import com.examp.genifit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.examp.genifit.dto.response.CancelSubscriptionResponse;

import java.time.Instant;
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
    AuthenticationService authenticationService;

    @PostMapping("/send-otp")
    public ApiResponse<String> sendOtp(@RequestParam String email) {
        userService.generateAndSendOtp(email);
        return ApiResponse.success(
                "OTP sent successfully! Check your mailbox!",
                email);
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ApiResponse.success(
                "Registered successfully",
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

    @Operation
            (summary = "User đăng kí gói")
    @PostMapping("/subscribe")
    public ApiResponse<UserSubscriptionResponse> subscribePlan(
            @Valid @RequestBody SubscribePlanRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();

        UserSubscriptionResponse response =
                subscriptionPlanService.subscribePlan(username, request);

        return ApiResponse.<UserSubscriptionResponse>builder()
                .success(true)
                .message("Đăng kí gói thành công")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @Operation(
            summary = "Get my active subscription"
    )
    @GetMapping("/my-active")
    public ApiResponse<UserSubscriptionResponse> getMyActiveSubscription() {
        return ApiResponse.success(
                "Get active subscription successfully",
                userService.getMyActiveSubscription()
        );
    }

    @Operation(
            summary = "Get my subscription history"
    )
    @GetMapping("/my-history")
    public ApiResponse<List<UserSubscriptionResponse>> getMySubscriptionHistory() {
        return ApiResponse.success(
                "Get subscription history successfully",
                userService.getMySubscriptionHistory()
        );
    }

    @Operation(
            summary = "View my subscription plan"
    )
    @GetMapping("/my-plan")
    public MySubscriptionResponse getMySubscription(Authentication authentication) {
        String username = authentication.getName();

        return subscriptionPlanService.getMySubscription(username);
    }

    @Operation(
            summary = "Cancel current subscription"
    )
    @PatchMapping("/cancel-my-subscription")
    public ApiResponse<CancelSubscriptionResponse> cancelMySubscription() {
        CancelSubscriptionResponse response = userService.cancelMySubscription();

        return ApiResponse.success(
                "Cancel subscription successfully",
                response
        );
    }

    @PostMapping("/forgot-password/send-otp")
    public ApiResponse<String> sendOtpForForgotPassword(@RequestParam String email) {
        userService.generateAndSendOtpForForgotPassword(email);
        return ApiResponse.success(
                "Password reset OTP has been sent to your email!",
                email
        );
    }

    @PutMapping("/me/password")
    public ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.success(
                "Password changed successfully",
                "Success"
        );
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ApiResponse.success(
                "Password reset successfully",
                "Success"
        );
    }

    @DeleteMapping("/me")
    public ApiResponse<String> deleteMyAccount() {
        userService.deleteMe();
        return ApiResponse.success(
                "Your account has been deleted",
                "Success"
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUserByAdmin(@PathVariable Integer id) {
        userService.deleteUserById(id);
        return ApiResponse.success(
                "Admin deleted the account successfully",
                "Success"
        );
    }

    @PutMapping("/me/profile")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserProfileResponse> updateMyProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        return ApiResponse.success(
                "Health profile updated successfully",
                userService.updateMyProfile(request)
        );
    }

    @PostMapping("/me/upgrade")
    @PreAuthorize("hasAuthority('GUEST')")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AuthenticationResponse> upgradeGuestToMember(@RequestBody @Valid CreateUserFromGuestRequest request) {
        return ApiResponse.success(
                "Account upgraded successfully! Legacy data has been synchronized.",
                authenticationService.upgradeGuestToMember(request)
        );
    }
}