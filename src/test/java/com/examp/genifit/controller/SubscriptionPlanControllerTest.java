package com.examp.genifit.controller;

import com.examp.genifit.dto.response.SubscribePlanResponse;
import com.examp.genifit.dto.response.SubscriptionPlanResponse;
import com.examp.genifit.service.SubscriptionPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanControllerTest {

    @Mock
    private SubscriptionPlanService subscriptionPlanService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SubscriptionPlanController(subscriptionPlanService)).build();
    }

    @Test
    void planListEndpoints_forwardOptionalPagination() throws Exception {
        when(subscriptionPlanService.getAllPlans(1, 10)).thenReturn(new PageImpl<SubscriptionPlanResponse>(List.of()));
        when(subscriptionPlanService.getActivePlans(null, null)).thenReturn(new PageImpl<SubscriptionPlanResponse>(List.of()));

        mockMvc.perform(get("/api/subscriptions/plans").param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/subscriptions/plans/active"))
                .andExpect(status().isOk());

        verify(subscriptionPlanService).getAllPlans(1, 10);
        verify(subscriptionPlanService).getActivePlans(null, null);
    }

    @Test
    void planManagementEndpoints_delegateRequests() throws Exception {
        when(subscriptionPlanService.getPlanById(3)).thenReturn(null);
        when(subscriptionPlanService.createPlan(any())).thenReturn(null);
        when(subscriptionPlanService.updatePlan(eq(3), any())).thenReturn(null);
        doNothing().when(subscriptionPlanService).deletePlan(3);

        mockMvc.perform(get("/api/subscriptions/plans/{planId}", 3)).andExpect(status().isOk());
        mockMvc.perform(post("/api/subscriptions/plans").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planType\":\"FREE\",\"planName\":\"Free\",\"price\":0}"))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/subscriptions/plans/{planId}", 3).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planName\":\"Updated\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/subscriptions/plans/{planId}", 3)).andExpect(status().isOk());

        verify(subscriptionPlanService).getPlanById(3);
        verify(subscriptionPlanService).createPlan(any());
        verify(subscriptionPlanService).updatePlan(eq(3), any());
        verify(subscriptionPlanService).deletePlan(3);
    }

    @Test
    void currentSubscriptionEndpoints_usePrincipalName() throws Exception {
        Principal principal = () -> "user@example.com";
        when(subscriptionPlanService.getMySubscription("user@example.com")).thenReturn(null);
        when(subscriptionPlanService.cancelSubscription("user@example.com")).thenReturn(null);
        when(subscriptionPlanService.renewSubscription("user@example.com"))
                .thenReturn(SubscribePlanResponse.builder().requiresPayment(false).build());
        when(subscriptionPlanService.getMySubscriptionHistory("user@example.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/subscriptions/me").principal(principal)).andExpect(status().isOk());
        mockMvc.perform(post("/api/subscriptions/cancel").principal(principal)).andExpect(status().isOk());
        mockMvc.perform(post("/api/subscriptions/renew").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Gia hạn gói thành công"));
        mockMvc.perform(get("/api/subscriptions/history").principal(principal)).andExpect(status().isOk());

        verify(subscriptionPlanService).getMySubscription("user@example.com");
        verify(subscriptionPlanService).cancelSubscription("user@example.com");
        verify(subscriptionPlanService).renewSubscription("user@example.com");
        verify(subscriptionPlanService).getMySubscriptionHistory("user@example.com");
    }
}
