package com.examp.genifit.controller;

import com.examp.genifit.dto.response.GeminiFoodScanResponse;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.FoodEvaluationService;
import com.examp.genifit.service.GeminiFoodScanService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FoodEvaluationControllerTest {
    @Mock private GeminiFoodScanService geminiFoodScanService;
    @Mock private FoodEvaluationService foodEvaluationService;
    @Mock private UserRepository userRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        mockMvc = MockMvcBuilders.standaloneSetup(new FoodEvaluationController(
                geminiFoodScanService, foodEvaluationService, userRepository)).build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void scanAndEvaluate_requiresGuestOrAuthenticatedUser() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "meal.jpg", "image/jpeg", new byte[]{1});

        mockMvc.perform(multipart("/api/food-evaluation/scan-and-evaluate").file(image))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Please login or provide guestId"));
    }

    @Test
    void scanAndEvaluate_forwardsGuestAndMapsScanResultToEvaluationRequest() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "meal.jpg", "image/jpeg", new byte[]{1});
        GeminiFoodScanResponse scan = new GeminiFoodScanResponse(9, "ok", List.of(), 500.0, 20.0, 60.0, 10.0, 0.9, "note", "gemini");
        when(geminiFoodScanService.scanFoodImage(any(), eq(null), eq(7))).thenReturn(scan);
        when(foodEvaluationService.evaluateScannedFood(any())).thenReturn(null);

        mockMvc.perform(multipart("/api/food-evaluation/scan-and-evaluate").file(image).param("guestId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<com.examp.genifit.dto.request.FoodEvaluationRequest> request =
                ArgumentCaptor.forClass(com.examp.genifit.dto.request.FoodEvaluationRequest.class);
        verify(foodEvaluationService).evaluateScannedFood(request.capture());
        assertEquals(7, request.getValue().getGuestId());
        assertEquals(500.0, request.getValue().getTotalCalories());
    }

    @Test
    void evaluateScannedFood_acceptsGuestRequest() throws Exception {
        when(foodEvaluationService.evaluateScannedFood(any())).thenReturn(null);

        mockMvc.perform(post("/api/food-evaluation/evaluate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"guestId\":7,\"foods\":[]}"))
                .andExpect(status().isOk());

        verify(foodEvaluationService).evaluateScannedFood(any());
    }

    @Test
    void evaluateScannedFood_overridesGuestWithAuthenticatedUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("member", null, "ROLE_USER"));
        when(userRepository.findByUsernameAndIsActiveTrue("member"))
                .thenReturn(Optional.of(User.builder().userId(3).username("member").build()));
        when(foodEvaluationService.evaluateScannedFood(any())).thenReturn(null);

        com.examp.genifit.dto.request.FoodEvaluationRequest input = new com.examp.genifit.dto.request.FoodEvaluationRequest();
        input.setGuestId(7);
        new FoodEvaluationController(geminiFoodScanService, foodEvaluationService, userRepository)
                .evaluateScannedFood(input);

        ArgumentCaptor<com.examp.genifit.dto.request.FoodEvaluationRequest> request =
                ArgumentCaptor.forClass(com.examp.genifit.dto.request.FoodEvaluationRequest.class);
        verify(foodEvaluationService).evaluateScannedFood(request.capture());
        assertEquals(3, request.getValue().getUserId());
        org.junit.jupiter.api.Assertions.assertNull(request.getValue().getGuestId());
    }
}
