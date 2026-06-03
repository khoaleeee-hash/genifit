package com.examp.genifit.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "User not found"),

    FOOD_NOT_FOUND(HttpStatus.NOT_FOUND, "FOOD_001", "Food not found"),

    DAILY_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "DL_001", "Daily log not found"),

    GUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "GST_001", "Guest session not found"),

    WEIGHT_PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "WP_001", "Weight progress not found"),

    INVALID_CALORIE_VALUE(HttpStatus.BAD_REQUEST, "CAL_001", "Invalid calorie value"),

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON_001", "Validation failed"),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "Internal server error");

    private final HttpStatus status;
    private final String code;
    private final String message;
}