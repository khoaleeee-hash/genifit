package com.examp.genifit.dto;

import com.examp.genifit.entity.StatusColor;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryResponse {

    private LocalDate date;

    private Double totalCalories;

    private StatusColor statusColor;

}
