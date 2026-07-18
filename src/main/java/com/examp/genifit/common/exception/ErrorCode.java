package com.examp.genifit.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // =========================================================================
    // AUTH
    // =========================================================================
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "AU_001", "Unauthenticated"),

    // =========================================================================
    // USER
    // =========================================================================
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "User not found"),
    USER_EXISTED(HttpStatus.BAD_REQUEST, "USER_002", "User already existed"),
    USER_BANNED(HttpStatus.BAD_REQUEST, "USER_003", "User is banned"),

    // =========================================================================
    // ADMIN
    // =========================================================================
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN_001", "ADMIN not found"),
    ADMIN_REQUIRED(HttpStatus.FORBIDDEN, "ADM_001", "Chỉ admin mới được tạo món ăn chuẩn"),
    ADMIN_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ADM_002", "AdminId không được để trống"),

    // =========================================================================
    // USER / GUEST CONTEXT
    // =========================================================================
    USER_OR_GUEST_REQUIRED(HttpStatus.BAD_REQUEST, "CTX_001", "Cần truyền userId hoặc guestId"),
    USER_GUEST_CONFLICT(HttpStatus.BAD_REQUEST, "CTX_002", "Chỉ được truyền userId hoặc guestId, không truyền cả hai"),

    // =========================================================================
    // USER PROFILE
    // =========================================================================
    USER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "PROFILE_001", "User profile not found"),

    // =========================================================================
    // NUTRITION / DAILY LOG / WEIGHT PROGRESS
    // =========================================================================
    DAILY_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "DL_001", "Daily log not found"),
    INVALID_CALORIE_VALUE(HttpStatus.BAD_REQUEST, "CAL_001", "Invalid calorie value"),
    WEIGHT_PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "WP_001", "Weight progress not found"),
    INVALID_WEIGHT_PROGRESS_CONFIG(HttpStatus.BAD_REQUEST, "WP_002", "Invalid weight progress configuration"),
    INVALID_WEIGHT_VALUE(HttpStatus.BAD_REQUEST, "WP_003", "Invalid weight value"),

    // =========================================================================
    // GUEST SESSION
    // =========================================================================
    GUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "GST_001", "Guest session not found"),

    // =========================================================================
    // OTP
    // =========================================================================
    INVALID_OTP(HttpStatus.BAD_REQUEST, "OTP_001", "Invalid OTP code"),
    OTP_EXPIRED(HttpStatus.BAD_REQUEST, "OTP_002", "OTP code has expired"),

    // =========================================================================
    // SUBSCRIPTION PLAN (quản trị gói)
    // =========================================================================
    SUBSCRIPTION_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "SUB_PLAN_001", "Subscription plan not found"),
    SUBSCRIPTION_PLAN_EXISTED(HttpStatus.BAD_REQUEST, "SUB_PLAN_002", "Subscription plan already existed"),
    SUBSCRIPTION_PLAN_INACTIVE(HttpStatus.BAD_REQUEST, "SUB_PLAN_003", "Subscription plan is inactive"),

    // =========================================================================
    // USER SUBSCRIPTION (đăng ký của user)
    // =========================================================================
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUB_001", "Subscription not found"),
    ACTIVE_SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUB_002", "Active subscription not found"),
    INVALID_SUBSCRIPTION_REQUEST(HttpStatus.BAD_REQUEST, "SUB_003", "Invalid subscription request"),

    // =========================================================================
    // PAYMENT (VNPay / MoMo)
    // =========================================================================
    PAYMENT_REQUIRED(HttpStatus.BAD_REQUEST, "PAY_001", "Payment required for paid plan"),
    PAYMENT_TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY_002", "Payment transaction not found"),
    INVALID_PAYMENT_SIGNATURE(HttpStatus.BAD_REQUEST, "PAY_003", "Invalid payment gateway signature"),
    UNSUPPORTED_PAYMENT_METHOD(HttpStatus.BAD_REQUEST, "PAY_004", "Unsupported payment method"),
    PAYMENT_GATEWAY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PAY_005", "Payment gateway error"),

    // =========================================================================
    // COMMON
    // =========================================================================
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON_001", "Validation failed"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "Internal server error"),

    // =========================================================================
    // AI FOOD SCAN
    // =========================================================================
    SCAN_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "SCAN_001", "Scan history not found"),
    SCAN_NOT_OWNED_BY_USER(HttpStatus.FORBIDDEN, "SCAN_002", "You do not have permission to use this scan"),
    SCAN_ALREADY_ADDED(HttpStatus.CONFLICT, "SCAN_003", "This scan has already been added to meal history"),
    INVALID_FOOD_SOURCE(HttpStatus.BAD_REQUEST, "SCAN_004", "Only one of foodId, scanId or foodName may be provided"),
    INVALID_SCAN_DATA(HttpStatus.BAD_REQUEST, "SCAN_005", "Scan result does not contain valid nutrition data"),
    INVALID_FOOD_SCAN(HttpStatus.NOT_FOUND, "SCAN_006", "Food scan not found"),
    // =========================================================================
    // FOOD / MEAL
    // =========================================================================
    FOOD_NOT_FOUND(HttpStatus.NOT_FOUND, "FOOD_001", "Food not found"),
    FOOD_ALREADY_EXISTS(HttpStatus.CONFLICT, "FOOD_002", "Food already exists"),
    INVALID_FOOD_DATA(HttpStatus.BAD_REQUEST, "FOOD_003", "Invalid food data"),
    FOOD_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "FOOD_005", "Food name is required"),
    INVALID_FOOD_QUANTITY(HttpStatus.BAD_REQUEST, "FOOD_006", "Food quantity must be greater than zero"),
    INVALID_FOOD_CALORIES(HttpStatus.BAD_REQUEST, "FOOD_007", "Food calories must be greater than zero"),
    INVALID_FOOD_PROTEIN(HttpStatus.BAD_REQUEST, "FOOD_008", "Food protein must be greater than or equal to zero"),
    INVALID_FOOD_CARBS(HttpStatus.BAD_REQUEST, "FOOD_009", "Food carbs must be greater than or equal to zero"),
    INVALID_FOOD_FAT(HttpStatus.BAD_REQUEST, "FOOD_010", "Food fat must be greater than or equal to zero"),
    MEAL_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "FOOD_011", "Meal time is required"),
    FOOD_ALREADY_ADDED_TO_MEAL(HttpStatus.CONFLICT, "FOOD_012", "Food has already been added to this meal"),
    FOOD_DATA_REQUIRED(HttpStatus.BAD_REQUEST, "FOOD_013", "Không có dữ liệu món ăn để đánh giá"),

    // =========================================================================
    // INGREDIENT
    // =========================================================================
    INGREDIENT_REQUIRED(HttpStatus.BAD_REQUEST, "ING_001", "Cần nhập ít nhất một nguyên liệu"),
    INGREDIENT_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "ING_002", "Tên nguyên liệu không được để trống"),
    INVALID_INGREDIENT_QUANTITY(HttpStatus.BAD_REQUEST, "ING_003", "Số lượng nguyên liệu không hợp lệ"),

    // =========================================================================
    // GEMINI / AI SERVICE
    // =========================================================================
    GEMINI_INVALID_JSON(HttpStatus.BAD_GATEWAY, "GEMINI_001", "Gemini trả về JSON không hợp lệ"),
    GEMINI_OVERLOADED(HttpStatus.SERVICE_UNAVAILABLE, "GEMINI_002", "Gemini đang quá tải. Vui lòng thử lại sau"),
    GEMINI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "GEMINI_003", "Gemini đang bị giới hạn request. Vui lòng thử lại sau"),
    GEMINI_CALL_FAILED(HttpStatus.BAD_GATEWAY, "GEMINI_004", "Không gọi được Gemini"),
    GEMINI_RETRY_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "GEMINI_005", "Retry Gemini bị gián đoạn"),
    GEMINI_EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "GEMINI_006", "Gemini không trả về nội dung hợp lệ"),
    GEMINI_RESPONSE_READ_FAILED(HttpStatus.BAD_GATEWAY, "GEMINI_007", "Không đọc được response từ Gemini"),

    // =========================================================================
    // IMAGE / FILE UPLOAD
    // =========================================================================
    IMAGE_FILE_REQUIRED(HttpStatus.BAD_REQUEST, "IMG_001", "Image file is required"),
    IMAGE_FILE_EMPTY(HttpStatus.BAD_REQUEST, "IMG_002", "Image file must not be empty"),
    IMAGE_FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "IMG_003", "Image file exceeds the maximum allowed size"),
    UNSUPPORTED_IMAGE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "IMG_004", "Unsupported image file type"),

    // =========================================================================
    // PARSING / DATA TYPE
    // =========================================================================
    INVALID_INTEGER_VALUE(HttpStatus.BAD_REQUEST, "PARSE_001", "Giá trị số nguyên không hợp lệ"),
    INVALID_DECIMAL_VALUE(HttpStatus.BAD_REQUEST, "PARSE_002", "Giá trị số thực không hợp lệ"),
    INVALID_BOOLEAN_VALUE(HttpStatus.BAD_REQUEST, "PARSE_003", "Giá trị boolean không hợp lệ, chỉ được nhập true hoặc false");

    private final HttpStatus status;
    private final String code;
    private final String message;
}