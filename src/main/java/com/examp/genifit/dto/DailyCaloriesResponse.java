package com.examp.genifit.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCaloriesResponse {

    private LocalDate date;

    private Double totalCalories;

    private Double targetCalories;

}
