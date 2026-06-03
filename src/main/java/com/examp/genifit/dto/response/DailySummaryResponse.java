package com.examp.genifit.dto.response;

import com.examp.genifit.entity.StatusColor;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
public class DailySummaryResponse {

    private LocalDate date;

    private Double totalCalories;

    private StatusColor statusColor;
}
