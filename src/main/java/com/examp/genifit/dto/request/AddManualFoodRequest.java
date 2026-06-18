package com.examp.genifit.dto.request;

import com.examp.genifit.entity.MealTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter

public class AddManualFoodRequest {

    @NotBlank(message = "foodName is required")
    private String foodName;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be greater than 0")
    private Double quantity;

    private MealTime mealTime;

}
