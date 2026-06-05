package com.examp.genifit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class GeminiFoodScanResponse {
    private String message;
    private List<DetectedFoodItemResponse> foods;
    private Double totalCalories;
    private Double totalProtein;
    private Double totalCarbs;
    private Double totalFat;

    private Double confidence;
    private String code;
    private String source;

}
