package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.CreateUserRequest;
import com.examp.genifit.dto.request.GeminiMealSuggestionRequest;
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

    @PostMapping("/from-ingredients")
    public GeminiMealSuggestionResponse suggestMealsFromIngredients(
            @RequestBody GeminiMealSuggestionRequest request
    ) {
        return geminiMealSuggestionService.suggestMealsFromIngredients(request);
    }
}