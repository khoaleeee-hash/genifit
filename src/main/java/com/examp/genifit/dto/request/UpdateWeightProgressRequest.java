package com.examp.genifit.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateWeightProgressRequest {

    @NotNull(message = "User id is required")
    private Integer userId;

    @NotNull(message = "Current weight is required")
    @DecimalMin(value = "20.0", message = "Current weight must be at least 20 kg")
    @DecimalMax(value = "300.0", message = "Current weight must be less than or equal to 300 kg")
    private Double currentWeight;
}