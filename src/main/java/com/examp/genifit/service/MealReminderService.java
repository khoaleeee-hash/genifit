package com.examp.genifit.service;

import com.examp.genifit.dto.request.CreateMealReminderRequest;
import com.examp.genifit.dto.request.ToggleMealReminderRequest;
import com.examp.genifit.dto.request.UpdateMealReminderRequest;
import com.examp.genifit.dto.response.MealReminderResponse;

import java.util.List;

public interface MealReminderService {

    MealReminderResponse createReminder(CreateMealReminderRequest request);

    List<MealReminderResponse> getMyReminders();

    MealReminderResponse updateReminder(Integer reminderId, UpdateMealReminderRequest request);

    MealReminderResponse toggleReminder(Integer reminderId, ToggleMealReminderRequest request);

    void deleteReminder(Integer reminderId);
}