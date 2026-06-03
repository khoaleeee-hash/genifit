package com.examp.genifit.dto;

import com.examp.genifit.entity.StatusColor;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyLogResponse {

    private Integer logId;

    private LocalDate date;

    private Double totalCalories;

    private Double targetCalories;

    private StatusColor statusColor;

}
