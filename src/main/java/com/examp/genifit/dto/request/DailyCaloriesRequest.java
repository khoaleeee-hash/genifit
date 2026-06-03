package com.examp.genifit.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DailyCaloriesRequest {

    @NotNull(message = "User id is required")
    private Integer userId;

    @NotNull(message = "Calories is required")
    @Min(value = 0, message = "Calories must be greater than or equal to 0")
    private Double calories;
}