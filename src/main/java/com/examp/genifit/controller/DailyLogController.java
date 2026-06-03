package com.examp.genifit.controller;

import com.examp.genifit.common.response.ApiResponse;
import com.examp.genifit.dto.request.AddManualFoodRequest;
import com.examp.genifit.dto.response.AddManualFoodResponse;
import com.examp.genifit.dto.response.DailyCaloriesResponse;
import com.examp.genifit.dto.response.DailyLogResponse;
import com.examp.genifit.dto.response.DailySummaryResponse;
import com.examp.genifit.service.DailyLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            ){
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

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly logs")
    public ResponseEntity<ApiResponse<List<DailySummaryResponse>>> getMonthlyLogs(@RequestParam Integer userId,
                                                                                  @RequestParam Integer year,
                                                                                  @RequestParam Integer month) {

        return ResponseEntity.ok(ApiResponse.success("Get monthly logs successfully",
                        dailyLogService.getMonthlyLogs(userId, year, month)));
    }
}
