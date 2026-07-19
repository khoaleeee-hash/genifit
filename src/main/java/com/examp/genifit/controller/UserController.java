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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.examp.genifit.dto.response.CancelSubscriptionResponse;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload user avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestPart("image") MultipartFile image,
            Authentication authentication
    ) {
        try {
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail("Authentication is required to upload avatar", null));
            }

            if (image == null || image.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail("Image file is required", null));
            }
            String avatarUrl = userService.uploadAvatar(image);
            return ResponseEntity.ok(ApiResponse.success("Avatar updated successfully", avatarUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(e.getMessage(), null));
        }
    }
}