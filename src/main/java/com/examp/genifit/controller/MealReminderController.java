package com.examp.genifit.controller;

import com.examp.genifit.dto.request.CreateMealReminderRequest;
import com.examp.genifit.dto.request.ToggleMealReminderRequest;
import com.examp.genifit.dto.request.UpdateMealReminderRequest;
import com.examp.genifit.dto.response.MealReminderResponse;
import com.examp.genifit.service.MealReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meal-reminders")
@RequiredArgsConstructor
@Tag(name = "Meal Reminder")
public class MealReminderController {

    private final MealReminderService mealReminderService;

    @Operation(
            summary = "Tạo lịch nhắc nhở"
    )
    @PostMapping
    public MealReminderResponse createReminder(
            @RequestBody CreateMealReminderRequest request
    ) {
        return mealReminderService.createReminder(request);
    }

    @Operation(
            summary = "Danh sách những lịch nhắn nhở"
    )
    @GetMapping
    public List<MealReminderResponse> getMyReminders() {
        return mealReminderService.getMyReminders();
    }

    @Operation(
            summary = "Chỉnh sửa lịch nhắn nhở"
    )
    @PutMapping("/{reminderId}")
    public MealReminderResponse updateReminder(
            @PathVariable Integer reminderId,
            @RequestBody UpdateMealReminderRequest request
    ) {
        return mealReminderService.updateReminder(reminderId, request);
    }

    @PatchMapping("/{reminderId}/toggle")
    public MealReminderResponse toggleReminder(
            @PathVariable Integer reminderId,
            @RequestBody ToggleMealReminderRequest request
    ) {
        return mealReminderService.toggleReminder(reminderId, request);
    }

    @Operation(
            summary = "Xoá mềm lịch nhắc nhỏ"
    )
    @DeleteMapping("/{reminderId}")
    public String deleteReminder(
            @PathVariable Integer reminderId
    ) {
        mealReminderService.deleteReminder(reminderId);
        return "Xoá lịch nhắc thành công";
    }
}