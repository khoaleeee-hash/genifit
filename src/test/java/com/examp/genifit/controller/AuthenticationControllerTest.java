package com.examp.genifit.controller;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.common.exception.ErrorCode;
import com.examp.genifit.common.exception.GlobalExceptionHandler;
import com.examp.genifit.dto.request.AuthenticationRequest;
import com.examp.genifit.dto.request.GuestLoginRequest;
import com.examp.genifit.dto.response.AuthenticationResponse;
import com.examp.genifit.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthenticationController(authenticationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void authenticate_returnsWrappedAuthenticationResponse() throws Exception {
        when(authenticationService.authenticate(any())).thenReturn(authenticationResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"mail@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged in successfully!"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));

        ArgumentCaptor<AuthenticationRequest> request = ArgumentCaptor.forClass(AuthenticationRequest.class);
        verify(authenticationService).authenticate(request.capture());
        assertEquals("mail@example.com", request.getValue().getUsername());
    }

    @Test
    void refresh_delegatesRefreshToken() throws Exception {
        when(authenticationService.refreshToken(any())).thenReturn(authenticationResponse());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Refreshed successfully!"));

        verify(authenticationService).refreshToken(any());
    }

    @Test
    void introspect_delegatesToken() throws Exception {
        mockMvc.perform(post("/api/auth/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"access-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Introspecting successfully!"));

        verify(authenticationService).introspect(any());
    }

    @Test
    void logout_returnsSuccessAfterInvalidation() throws Exception {
        doNothing().when(authenticationService).logout(any());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accessToken\":\"access\",\"refreshToken\":\"refresh\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully!"));

        verify(authenticationService).logout(any());
    }

    @Test
    void googleLogin_delegatesIdToken() throws Exception {
        when(authenticationService.authenticateWithGoogle("google-token")).thenReturn(authenticationResponse());

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"google-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated").value(true));

        verify(authenticationService).authenticateWithGoogle("google-token");
    }

    @Test
    void guestLogin_delegatesRequest() throws Exception {
        when(authenticationService.loginAsGuest(any())).thenReturn(authenticationResponse());

        mockMvc.perform(post("/api/auth/guest-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceId\":\"device-1\"}"))
                .andExpect(status().isOk());

        verify(authenticationService).loginAsGuest(any(GuestLoginRequest.class));
    }

    @Test
    void authenticate_returnsApiErrorWhenServiceThrowsBusinessException() throws Exception {
        when(authenticationService.authenticate(any()))
                .thenThrow(new ApiException(ErrorCode.USER_NOT_FOUND, "Unknown account"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"missing\",\"password\":\"secret\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("Unknown account"));
    }

    private AuthenticationResponse authenticationResponse() {
        return AuthenticationResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .authenticated(true)
                .build();
    }
}
