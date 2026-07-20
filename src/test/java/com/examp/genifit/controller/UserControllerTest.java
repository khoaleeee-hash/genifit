package com.examp.genifit.controller;

import com.examp.genifit.dto.request.UpdateAvatarRequest;
import com.examp.genifit.service.AuthenticationService;
import com.examp.genifit.service.GeminiMealSuggestionService;
import com.examp.genifit.service.SubscriptionPlanService;
import com.examp.genifit.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @Mock private GeminiMealSuggestionService geminiMealSuggestionService;
    @Mock private SubscriptionPlanService subscriptionPlanService;
    @Mock private AuthenticationService authenticationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(
                userService, geminiMealSuggestionService, subscriptionPlanService, authenticationService)).build();
    }

    @Test
    void otpAndRegistrationEndpoints_delegateToUserService() throws Exception {
        doNothing().when(userService).generateAndSendOtp("mail@example.com");
        when(userService.createUser(any())).thenReturn(null);
        doNothing().when(userService).generateAndSendOtpForForgotPassword("mail@example.com");

        mockMvc.perform(post("/api/users/send-otp").param("email", "mail@example.com"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value("mail@example.com"));
        mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"username1\",\"passwordHash\":\"password1\",\"email\":\"mail@example.com\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/users/forgot-password/send-otp").param("email", "mail@example.com"))
                .andExpect(status().isOk());

        verify(userService).generateAndSendOtp("mail@example.com");
        verify(userService).createUser(any());
        verify(userService).generateAndSendOtpForForgotPassword("mail@example.com");
    }

    @Test
    void userReadEndpoints_delegateWithCorrectParameters() throws Exception {
        when(userService.getMyInfo()).thenReturn(null);
        when(userService.getUser(8)).thenReturn(null);
        when(userService.getUsers()).thenReturn(List.of());
        when(userService.searchUsers("ann")).thenReturn(List.of());

        mockMvc.perform(get("/api/users/me")).andExpect(status().isOk());
        mockMvc.perform(get("/api/users/{id}", 8)).andExpect(status().isOk());
        mockMvc.perform(get("/api/users")).andExpect(status().isOk());
        mockMvc.perform(get("/api/users/search").param("keyword", "ann")).andExpect(status().isOk());

        verify(userService).getMyInfo();
        verify(userService).getUser(8);
        verify(userService).getUsers();
        verify(userService).searchUsers("ann");
    }

    @Test
    void suggestMeals_delegatesRequestToGeminiService() throws Exception {
        when(geminiMealSuggestionService.suggestMealsFromIngredients(any())).thenReturn(null);

        mockMvc.perform(post("/api/users/from-ingredients").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"ingredients\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Suggest meals successfully"));

        verify(geminiMealSuggestionService).suggestMealsFromIngredients(any());
    }

    @Test
    void passwordAndDeletionEndpoints_delegateToUserService() throws Exception {
        doNothing().when(userService).changePassword(any());
        doNothing().when(userService).resetPassword(any());
        doNothing().when(userService).deleteMe();
        doNothing().when(userService).deleteUserById(8);

        mockMvc.perform(put("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"old\",\"newPassword\":\"new\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/users/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"mail@example.com\",\"otpCode\":\"123456\",\"newPassword\":\"new\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/users/me")).andExpect(status().isOk());
        mockMvc.perform(delete("/api/users/{id}", 8)).andExpect(status().isOk());

        verify(userService).changePassword(any());
        verify(userService).resetPassword(any());
        verify(userService).deleteMe();
        verify(userService).deleteUserById(8);
    }

    @Test
    void updateProfileAndUpgradeGuest_delegateToTheirServices() throws Exception {
        when(userService.updateMyProfile(any())).thenReturn(null);
        when(authenticationService.upgradeGuestToMember(any())).thenReturn(null);

        mockMvc.perform(put("/api/users/me/profile").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"heightCm\":170,\"weightKg\":65,\"age\":30,\"gender\":\"MALE\",\"goal\":\"MAINTAIN\",\"activityLevel\":\"MODERATELY_ACTIVE\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/users/me/upgrade").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"username1\",\"passwordHash\":\"password1\",\"email\":\"mail@example.com\"}"))
                .andExpect(status().isOk());

        verify(userService).updateMyProfile(any());
        verify(authenticationService).upgradeGuestToMember(any());
    }

    @Test
    void avatarUpdate_returnsUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(put("/api/users/me/avatar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatarUrl\":\"https://example.com/avatar.png\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void avatarUpdate_delegatesUrlForAuthenticatedUser() throws Exception {
        UpdateAvatarRequest request = new UpdateAvatarRequest();
        request.setAvatarUrl("https://example.com/avatar.png");
        doNothing().when(userService).updateAvatarUrl("https://example.com/avatar.png");

        org.springframework.http.ResponseEntity<?> response = new UserController(
                userService, geminiMealSuggestionService, subscriptionPlanService, authenticationService
        ).saveAvatarUrl(request, new TestingAuthenticationToken("user@example.com", null, "ROLE_USER"));

        org.junit.jupiter.api.Assertions.assertEquals(200, response.getStatusCode().value());
        verify(userService).updateAvatarUrl("https://example.com/avatar.png");
    }
}
