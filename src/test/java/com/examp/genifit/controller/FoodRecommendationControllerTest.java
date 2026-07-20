package com.examp.genifit.controller;

import com.examp.genifit.service.FoodRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FoodRecommendationControllerTest {
    @Mock private FoodRecommendationService recommendationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FoodRecommendationController(recommendationService)).build();
    }

    @Test
    void getRecommendation_usesDefaultMealType() throws Exception {
        when(recommendationService.recommend(2, "bữa chính")).thenReturn(null);
        mockMvc.perform(get("/api/recommendations/{userId}", 2)).andExpect(status().isOk());
        verify(recommendationService).recommend(2, "bữa chính");
    }

    @Test
    void getAfterLog_requiresAndForwardsMealType() throws Exception {
        when(recommendationService.recommend(2, "LUNCH")).thenReturn(null);
        mockMvc.perform(get("/api/recommendations/{userId}/after-log", 2).param("mealType", "LUNCH"))
                .andExpect(status().isOk());
        verify(recommendationService).recommend(2, "LUNCH");
    }
}
