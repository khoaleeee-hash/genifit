package com.examp.genifit.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthGoalStats {
    private Long loseWeight;
    private Long gainWeight;
    private Long maintainWeight;
    
    private Double loseWeightPercentage;
    private Double gainWeightPercentage;
    private Double maintainWeightPercentage;
}
