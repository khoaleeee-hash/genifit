package com.examp.genifit.service;

import com.examp.genifit.dto.request.AddManualFoodRequest;
import com.examp.genifit.dto.response.*;

import java.time.LocalDate;
import java.util.List;

public interface DailyLogService {
    AddManualFoodResponse addManualFood(String username, AddManualFoodRequest request);

    MealHistoryResponse getMealHistory(String username, LocalDate date);

    DailyCaloriesResponse getTodayCalories(Integer userId);

    DailyLogResponse getCaloriesByDate(Integer userId, LocalDate date);

    List<DailySummaryResponse> getMonthlyLogs(Integer userId, Integer year, Integer month);

    WeeklyChartResponse getWeeklyChart(Integer userId);

}
