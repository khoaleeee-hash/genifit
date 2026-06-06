package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-logs")
@RequiredArgsConstructor
@Tag(name = "Daily Tracking")
public class DailyLogController {
    private final DailyLogService dailyLogService;

    @PostMapping("/meals/manual-foods")
    public AddManualFoodResponse addManualFood(
            @RequestBody AddManualFoodRequest request
    ) {
        return dailyLogService.addManualFood(request);
    }

    @GetMapping("/today")
    @Operation(summary = "Get today calories")
    public ResponseEntity<ApiResponse<DailyCaloriesResponse>> getTodayCalories(@RequestParam Integer userId) {

        return ResponseEntity.ok(ApiResponse.success("Get today calories successfully",
                dailyLogService.getTodayCalories(userId)));
    }

    @GetMapping
    @Operation(summary = "Get calories by date")
    public ResponseEntity<ApiResponse<DailyLogResponse>> getCaloriesByDate(@RequestParam Integer userId,
                                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                           LocalDate date) {

        return ResponseEntity.ok(ApiResponse.success("Get daily log successfully",
                dailyLogService.getCaloriesByDate(userId, date)));
    }

    @GetMapping("/home-status")
    @Operation(summary = "Get home page calorie status")
    public ResponseEntity<ApiResponse<HomeStatusResponse>>
    getHomeStatus(@RequestParam Integer userId) {

        HomeStatusResponse response = dailyLogService.getHomeStatus(userId);

        return ResponseEntity.ok(ApiResponse.success("Home status fetched successfully", response));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly calorie logs")
    public ResponseEntity<ApiResponse<List<DailySummaryResponse>>> getMonthlyLogs
            (@RequestParam @NotNull(message = "User id is required") Integer userId,
             @RequestParam @NotNull(message = "Year is required") @Min(value = 2000, message = "Year must be greater than or equal to 2000") @Max(value = 2100, message = "Year must be less than or equal to 2100") Integer year,
             @RequestParam @NotNull(message = "Month is required") @Min(value = 1, message = "Month must be between 1 and 12") @Max(value = 12, message = "Month must be between 1 and 12") Integer month) {

        List<DailySummaryResponse> response = dailyLogService.getMonthlyLogs(userId, year, month);

        return ResponseEntity.ok(ApiResponse.success("Get monthly logs successfully", response)
        );
    }

    @GetMapping("/weekly-chart")
    @Operation(summary = "Get 7-day calorie chart")
    public ResponseEntity<ApiResponse<WeeklyChartResponse>> getWeeklyChart(@RequestParam @NotNull(message = "User id is required") Integer userId) {

        WeeklyChartResponse response = dailyLogService.getWeeklyChart(userId);

        return ResponseEntity.ok(ApiResponse.success("Get weekly chart successfully", response)
        );
    }
}
