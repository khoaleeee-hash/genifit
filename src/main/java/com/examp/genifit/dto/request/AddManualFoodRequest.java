package com.examp.genifit.dto.request;

import com.examp.genifit.entity.MealTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter

public class AddManualFoodRequest {

    @Schema(example = "0")
    private Integer foodId;

    @Schema(example = "0")
    private Integer scanId;

    @Schema(example = "string")
    private String foodName;

    private Double calories;

    private Double fat;

    private Double carbs;

    private Double protein;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be greater than 0")
    @Schema(example = "1")
    private Double quantity;

    @NotNull(message = "mealTime is required")
    @Schema(
            example = "BREAKFAST",
            allowableValues = {
                    "BREAKFAST",
                    "LUNCH",
                    "DINNER",
                    "SNACK"
            }
    )
    private MealTime mealTime;

}
