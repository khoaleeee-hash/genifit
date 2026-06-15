package com.examp.genifit.dto.request;

import com.examp.genifit.entity.MealTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMealReminderRequest {

    private MealTime mealTime;
    private String reminderTime;
    private Boolean enabled;
}