package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.common.security.JwtUtils;
import com.examp.genifit.dto.request.AddManualFoodRequest;
import com.examp.genifit.dto.response.*;
import com.examp.genifit.service.DailyLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-logs")
@RequiredArgsConstructor
@Tag(name = "Daily Tracking")
public class DailyLogController {
    private final DailyLogService dailyLogService;
    private final JwtUtils jwtUtils;

    @PostMapping("/meals/manual-foods")
    public AddManualFoodResponse addManualFood(
            @RequestBody AddManualFoodRequest request
    ) {
        return dailyLogService.addManualFood(request);
    }

    @GetMapping("/today")
    @Operation(summary = "Get today calories")
    public ResponseEntity<ApiResponse<DailyCaloriesResponse>> getTodayCalories(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = jwtUtils.getUserId(jwt);

        return ResponseEntity.ok(ApiResponse.success("Get today calories successfully",
                dailyLogService.getTodayCalories(userId)));
    }

    @GetMapping
    @Operation(summary = "Get calories by date")
    public ResponseEntity<ApiResponse<DailyLogResponse>> getCaloriesByDate(@AuthenticationPrincipal Jwt jwt,
                                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                           LocalDate date) {
        Integer userId = jwtUtils.getUserId(jwt);

        return ResponseEntity.ok(ApiResponse.success("Get daily log successfully",
                dailyLogService.getCaloriesByDate(userId, date)));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly calorie logs")
    public ResponseEntity<ApiResponse<List<DailySummaryResponse>>> getMonthlyLogs
            (@AuthenticationPrincipal Jwt jwt,
             @RequestParam @NotNull(message = "Year is required") @Min(value = 2000, message = "Year must be greater than or equal to 2000") @Max(value = 2100, message = "Year must be less than or equal to 2100") Integer year,
             @RequestParam @NotNull(message = "Month is required") @Min(value = 1, message = "Month must be between 1 and 12") @Max(value = 12, message = "Month must be between 1 and 12") Integer month) {
        Integer userId = jwtUtils.getUserId(jwt);

        List<DailySummaryResponse> response = dailyLogService.getMonthlyLogs(userId, year, month);

        return ResponseEntity.ok(ApiResponse.success("Get monthly logs successfully", response)
        );
    }

    @GetMapping("/weekly-chart")
    @Operation(summary = "Get 7-day calorie chart")
    public ResponseEntity<ApiResponse<WeeklyChartResponse>> getWeeklyChart(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = jwtUtils.getUserId(jwt);

        WeeklyChartResponse response = dailyLogService.getWeeklyChart(userId);

        return ResponseEntity.ok(ApiResponse.success("Get weekly chart successfully", response)
        );
    }
}
