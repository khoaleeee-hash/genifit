package com.examp.genifit.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetectedFoodItemResponse {
    private String foodName;
    private String estimatedQuantity;

    private Double calo;
    private Double protein;
    private Double carbs;
    private Double fat;

}
