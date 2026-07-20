package com.examp.genifit.controller;

import com.examp.genifit.service.GeminiFoodScanService;
import com.examp.genifit.service.GeminiMealSuggestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GeminiControllerTest {
    @Mock private GeminiFoodScanService geminiFoodScanService;
    @Mock private GeminiMealSuggestionService geminiMealSuggestionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new GeminiController(geminiFoodScanService, geminiMealSuggestionService)).build();
    }

    @Test
    void scanFoodImage_forwardsMultipartFileAndOptionalOwners() throws Exception {
        when(geminiFoodScanService.scanFoodImage(any(), eq(3), eq(8))).thenReturn(null);
        MockMultipartFile image = new MockMultipartFile("image", "meal.jpg", "image/jpeg", new byte[]{1, 2});

        mockMvc.perform(multipart("/api/gemini/scan-food-image").file(image).param("userId", "3").param("guestId", "8"))
                .andExpect(status().isOk());

        verify(geminiFoodScanService).scanFoodImage(any(), eq(3), eq(8));
    }
}
