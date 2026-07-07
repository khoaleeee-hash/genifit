package com.examp.genifit.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWeightProgressRequest {

    @NotNull(message = "Current weight is required")
    @DecimalMin(value = "20")
    @DecimalMax(value = "300")
    private Double currentWeight;

}