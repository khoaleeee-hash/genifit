package com.examp.genifit.dto.response;

import com.examp.genifit.entity.MealTime;
import com.examp.genifit.entity.StatusColor;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealHistoryResponse {

    private LocalDate date;
    private Double totalCalories;
    private Double targetCalories;
    private StatusColor statusColor;
    private List<MealItem> meals;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealItem {

        private Integer detailId;

        private Integer foodId;
        private Integer scanId;

        private String foodName;

        private Double quantity;
        private Double calories;
        private Double fat;
        private Double carbs;
        private Double protein;

        private MealTime mealTime;
        private String source;
    }
}