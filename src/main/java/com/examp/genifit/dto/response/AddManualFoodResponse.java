package com.examp.genifit.dto.response;

import lombok.*;

@Getter
@Setter

public class AddManualFoodResponse {

    private String message;
    private String foodName;
    private Double quantity;
    private Double addedCalories;
    private Double totalCaloriesToday;
    private String statusColor;

    public AddManualFoodResponse(
            String message,
            String foodName,
            Double quantity,
            Double addCalories,
            Double totalCaloriesToday,
            String statusColor
    ){
        this.message = message;
        this.foodName = foodName;
        this.quantity = quantity;
        this.addedCalories = addCalories;
        this.totalCaloriesToday = totalCaloriesToday;
        this.statusColor = statusColor;
    }

}
