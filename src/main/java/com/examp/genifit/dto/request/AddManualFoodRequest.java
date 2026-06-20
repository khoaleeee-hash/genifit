package com.examp.genifit.dto.request;

import com.examp.genifit.entity.MealTime;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter

public class AddManualFoodRequest {

    private Integer foodId;

    private Integer scanId;

    private String foodName;

    private Double calories;

    private Double fat;

    private Double carbs;

    private Double protein;

    @Positive(message = "quantity must be greater than 0")
    private Double quantity;

    private MealTime mealTime;

}
