package com.examp.genifit.common.exception;

import com.examp.genifit.common.response.ApiError;
import com.examp.genifit.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ApiResponse<Object> buildResponse(
            String message,
            ApiError error
    ) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .error(error)
                .timestamp(Instant.now())
                .build();
    }

    /* Custom business exception */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(
            ApiException ex,
            HttpServletRequest request
    ) {

        log.warn(
                "Business exception at [{}]: {}",
                request.getRequestURI(),
                ex.getMessage()
        );

        ApiError error = ApiError.builder()
                .code(ex.getErrorCode().getCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(buildResponse(ex.getMessage(), error));
    }

    /* @Valid request body */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        ApiError error = ApiError.builder()
                .code("VALIDATION_ERROR")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .badRequest()
                .body(buildResponse(message, error));
    }

    /* @PathVariable, @RequestParam validation */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> {
                    String path = v.getPropertyPath().toString();
                    String field = path.contains(".")
                            ? path.substring(path.lastIndexOf('.') + 1)
                            : path;

                    return field + ": " + v.getMessage();
                })
                .distinct()
                .collect(Collectors.joining("; "));

        ApiError error = ApiError.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .badRequest()
                .body(buildResponse(message, error));
    }

    /* Wrong endpoint */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {

        ApiError error = ApiError.builder()
                .code("NOT_FOUND")
                .message("Resource not found")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildResponse("Resource not found", error));
    }

    /* Unexpected exception */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error(
                "Unexpected error at [{}]",
                request.getRequestURI(),
                ex
        );

        ApiError error = ApiError.builder()
                .code(ErrorCode.INTERNAL_ERROR.getCode())
                .message(ErrorCode.INTERNAL_ERROR.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        buildResponse(
                                ErrorCode.INTERNAL_ERROR.getMessage(),
                                error
                        )
                );
    }
}
