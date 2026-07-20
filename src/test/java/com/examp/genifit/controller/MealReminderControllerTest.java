package com.examp.genifit.controller;

import com.examp.genifit.dto.request.CreateMealReminderRequest;
import com.examp.genifit.dto.request.ToggleMealReminderRequest;
import com.examp.genifit.dto.request.UpdateMealReminderRequest;
import com.examp.genifit.dto.response.MealReminderResponse;
import com.examp.genifit.service.MealReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MealReminderControllerTest {

    @Mock
    private MealReminderService mealReminderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MealReminderController(mealReminderService)).build();
    }

    @Test
    void createReminder_delegatesRequest() throws Exception {
        when(mealReminderService.createReminder(any())).thenReturn(null);

        mockMvc.perform(post("/api/meal-reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mealTime\":\"BREAKFAST\",\"reminderTime\":\"08:00\",\"enabled\":true}"))
                .andExpect(status().isOk());

        verify(mealReminderService).createReminder(any(CreateMealReminderRequest.class));
    }

    @Test
    void getMyReminders_returnsServiceResult() throws Exception {
        when(mealReminderService.getMyReminders()).thenReturn(List.of());

        mockMvc.perform(get("/api/meal-reminders"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(mealReminderService).getMyReminders();
    }

    @Test
    void updateReminder_bindsPathVariableAndRequest() throws Exception {
        when(mealReminderService.updateReminder(eq(7), any())).thenReturn(null);

        mockMvc.perform(put("/api/meal-reminders/{reminderId}", 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reminderTime\":\"09:00\",\"enabled\":false}"))
                .andExpect(status().isOk());

        verify(mealReminderService).updateReminder(eq(7), any(UpdateMealReminderRequest.class));
    }

    @Test
    void toggleReminder_bindsPathVariableAndRequest() throws Exception {
        when(mealReminderService.toggleReminder(eq(7), any())).thenReturn(null);

        mockMvc.perform(patch("/api/meal-reminders/{reminderId}/toggle", 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isOk());

        verify(mealReminderService).toggleReminder(eq(7), any(ToggleMealReminderRequest.class));
    }

    @Test
    void deleteReminder_returnsConfirmation() throws Exception {
        doNothing().when(mealReminderService).deleteReminder(7);

        mockMvc.perform(delete("/api/meal-reminders/{reminderId}", 7))
                .andExpect(status().isOk());

        verify(mealReminderService).deleteReminder(7);
    }
}
