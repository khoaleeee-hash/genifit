package com.examp.genifit.controller;

import com.examp.genifit.dto.request.AssignSubscriptionRequest;
import com.examp.genifit.dto.request.CreateAdminFoodRequest;
import com.examp.genifit.dto.request.UpdateFoodRequest;
import com.examp.genifit.service.FoodService;
import com.examp.genifit.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private FoodService foodService;
    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(foodService, userService)).build();
    }

    @Test
    void createFoodByAdmin_delegatesRequest() throws Exception {
        when(foodService.createFoodByAdmin(any())).thenReturn(null);

        mockMvc.perform(post("/api/admin/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"adminId\":1,\"foodName\":\"Rice\",\"calories\":130}"))
                .andExpect(status().isOk());

        verify(foodService).createFoodByAdmin(any(CreateAdminFoodRequest.class));
    }

    @Test
    void assignSubscription_returnsSuccessWrapper() throws Exception {
        when(userService.assignSubscription(any())).thenReturn(null);

        mockMvc.perform(post("/api/admin/admin/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"planId\":2,\"autoRenew\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Assign subscription successfully"));

        verify(userService).assignSubscription(any(AssignSubscriptionRequest.class));
    }

    @Test
    void updateFood_bindsFoodIdAndRequest() throws Exception {
        when(foodService.updateFood(eq(9), any())).thenReturn(null);

        mockMvc.perform(put("/api/admin/foods/{foodId}", 9)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"foodName\":\"Brown rice\",\"isPublic\":true}"))
                .andExpect(status().isOk());

        verify(foodService).updateFood(eq(9), any(UpdateFoodRequest.class));
    }

    @Test
    void deleteFood_delegatesSoftDelete() throws Exception {
        doNothing().when(foodService).softDeleteFood(9);

        mockMvc.perform(delete("/api/admin/foods/{foodId}", 9))
                .andExpect(status().isOk());

        verify(foodService).softDeleteFood(9);
    }
}
