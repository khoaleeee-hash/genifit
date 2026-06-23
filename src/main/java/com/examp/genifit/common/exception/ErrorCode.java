package com.examp.genifit.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED,"AU_001", "Unauthenticated"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "User not found"),

    USER_EXISTED(HttpStatus.BAD_REQUEST, "USER_002", "User already existed"),

    USER_BANNED(HttpStatus.BAD_REQUEST, "USER_003", "User is banned"),

    FOOD_NOT_FOUND(HttpStatus.NOT_FOUND, "FOOD_001", "Food not found"),

    DAILY_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "DL_001", "Daily log not found"),

    GUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "GST_001", "Guest session not found"),

    WEIGHT_PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "WP_001", "Weight progress not found"),

    INVALID_CALORIE_VALUE(HttpStatus.BAD_REQUEST, "CAL_001", "Invalid calorie value"),

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON_001", "Validation failed"),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "Internal server error"),

    USER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "PROFILE_001", "User profile not found"),

    ADVANCED_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "ADV_PROFILE_001", "Advanced profile not found"),

    INVALID_WEIGHT_PROGRESS_CONFIG(HttpStatus.BAD_REQUEST, "WP_002", "Invalid weight progress configuration"),

    INVALID_WEIGHT_VALUE(HttpStatus.BAD_REQUEST, "WP_003", "Invalid weight value"),

    INVALID_OTP(HttpStatus.BAD_REQUEST, "OTP_001", "Invalid OTP code"),

    OTP_EXPIRED(HttpStatus.BAD_REQUEST, "OTP_002", "OTP code has expired");

    private final HttpStatus status;
    private final String code;
    private final String message;
}