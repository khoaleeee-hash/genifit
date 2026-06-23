package com.examp.genifit.dto.response;

import com.examp.genifit.entity.MealTime;
import com.examp.genifit.entity.StatusColor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyLogResponse {

    private LocalDate date;

    private Double totalCalories;

    private Double totalProtein;

    private Double totalCarbs;

    private Double totalFat;

    private Double targetCalories;

    private Double progressPercent;

    private StatusColor statusColor;

    private List<FoodDetail> foods;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodDetail {

        private String foodName;

        private Double quantity;

        private Double calories;

        private Double protein;

        private Double carbs;

        private Double fat;

        private MealTime mealTime;
    }
}