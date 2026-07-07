package com.examp.genifit.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAdvancedProfileRequest {

    private List<String> medicalConditions;
    private List<String> allergies;

    @NotNull(message = "Target weight is required")
    @DecimalMin(value = "20.0", message = "Target weight must be at least 20kg")
    @DecimalMax(value = "300.0", message = "Target weight must not exceed 300kg")
    private Double targetWeight;

    @NotNull(message = "Target date is required")
    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;

}