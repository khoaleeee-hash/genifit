package com.examp.genifit.dto.response;

import com.examp.genifit.entity.StatusColor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DailyCaloriesResponse {

    private LocalDate date;

    private Double totalCalories;

    private Double totalProtein;

    private Double totalCarbs;

    private Double totalFat;

    private Double targetCalories;

    private Double progressPercent;

    private StatusColor statusColor;
}