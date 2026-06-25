package com.examp.genifit.dto.response;

import com.examp.genifit.entity.MealReminder;
import com.examp.genifit.entity.MealTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@JsonPropertyOrder({
        "reminderId",
        "userId",
        "mealTime",
        "reminderTime",
        "enabled",
        "createdAt",
        "updatedAt"
})
public class MealReminderResponse {

    private Integer reminderId;
    private Integer userId;
    private MealTime mealTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime reminderTime;

    private Boolean enabled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MealReminderResponse(MealReminder reminder) {
        this.reminderId = reminder.getReminderId();
        this.userId = reminder.getUser().getUserId();
        this.mealTime = reminder.getMealTime();
        this.reminderTime = reminder.getReminderTime();
        this.enabled = reminder.getEnabled();
        this.createdAt = reminder.getCreatedAt();
        this.updatedAt = reminder.getUpdatedAt();
    }
}