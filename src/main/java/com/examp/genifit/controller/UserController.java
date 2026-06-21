package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.ChangePasswordRequest;
import com.examp.genifit.dto.request.CreateUserRequest;
import com.examp.genifit.dto.request.GeminiMealSuggestionRequest;
import com.examp.genifit.dto.request.ResetPasswordRequest;
import com.examp.genifit.dto.response.GeminiMealSuggestionResponse;
import com.examp.genifit.dto.response.UserResponse;
import com.examp.genifit.service.GeminiMealSuggestionService;
import com.examp.genifit.service.UserService;
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


    @PostMapping("/from-ingredients")
    public GeminiMealSuggestionResponse suggestMealsFromIngredients(
            @RequestBody GeminiMealSuggestionRequest request
    ) {
        return geminiMealSuggestionService.suggestMealsFromIngredients(request);
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
}