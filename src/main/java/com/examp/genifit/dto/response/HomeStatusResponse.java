package com.examp.genifit.dto.response;

import com.examp.genifit.entity.StatusColor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HomeStatusResponse {

    private Double totalCalories;

    private Double targetCalories;

    private Double progressPercent;

    private StatusColor statusColor;
}
