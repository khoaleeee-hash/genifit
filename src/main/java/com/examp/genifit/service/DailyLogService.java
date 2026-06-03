package com.examp.genifit.service;

import com.examp.genifit.dto.request.AddManualFoodRequest;
import com.examp.genifit.dto.response.AddManualFoodResponse;
import com.examp.genifit.dto.DailyCaloriesResponse;
import com.examp.genifit.dto.DailyLogResponse;
import com.examp.genifit.dto.DailySummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface DailyLogService {
    AddManualFoodResponse addManualFood(AddManualFoodRequest request);

    DailyCaloriesResponse getTodayCalories(Integer userId);

    DailyLogResponse getCaloriesByDate(Integer userId, LocalDate date);

    List<DailySummaryResponse> getMonthlyLogs(Integer userId, Integer year, Integer month);

}
