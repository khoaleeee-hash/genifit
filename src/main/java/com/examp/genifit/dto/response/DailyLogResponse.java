package com.examp.genifit.dto.response;

import com.examp.genifit.entity.MealTime;
import com.examp.genifit.entity.StatusColor;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DailyLogResponse {

    private LocalDate date;

    private Double totalCalories;

    private Double targetCalories;

    private StatusColor statusColor;

    private List<FoodDetail> foods;

    @Data
    @Builder
    public static class FoodDetail {

        private String foodName;

        private Double quantity;

        private Double calories;

        private MealTime mealTime;
    }
}
