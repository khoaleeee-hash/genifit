package com.examp.genifit.dto.response;

import com.examp.genifit.entity.StatusColor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyChartPointResponse {

    private LocalDate date;

    private String label;

    private Double totalCalories;

    private Double targetCalories;

    private Double progressPercent;

    private StatusColor statusColor;
}