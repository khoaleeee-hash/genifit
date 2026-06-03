package com.examp.genifit.dto.request;

import com.examp.genifit.entity.MealTime;
import lombok.*;

@Getter
@Setter

public class AddManualFoodRequest {
    private Integer userId;
    private Integer guestId;
    private Integer foodId;
    private String foodName;
    private Double quantity;
    private MealTime mealTime;

}
