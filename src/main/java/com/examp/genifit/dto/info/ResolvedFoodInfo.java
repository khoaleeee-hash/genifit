package com.examp.genifit.dto.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResolvedFoodInfo {
    private Integer foodId;
    private Integer scanId;

    private String foodName;

    private Double calories;
    private Double fat;
    private Double carbs;
    private Double protein;
}
