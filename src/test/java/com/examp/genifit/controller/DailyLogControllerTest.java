package com.examp.genifit.controller;

import com.examp.genifit.common.exception.GlobalExceptionHandler;
import com.examp.genifit.common.security.JwtUtils;
import com.examp.genifit.dto.request.AddManualFoodRequest;
import com.examp.genifit.service.DailyLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DailyLogControllerTest {

    @Mock
    private DailyLogService dailyLogService;
    @Mock
    private JwtUtils jwtUtils;

    private MockMvc mockMvc;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DailyLogController(dailyLogService, jwtUtils))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authenticationResolver(), new AuthenticationPrincipalArgumentResolver())
                .build();
        jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("sub", "user@example.com")
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addManualFood_returnsUnauthorizedBusinessErrorWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/daily-logs/meals/manual-foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":1,\"mealTime\":\"BREAKFAST\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void addManualFood_delegatesAuthenticatedUsername() throws Exception {
        authenticate("user@example.com");
        when(dailyLogService.addManualFood(eq("user@example.com"), any())).thenReturn(null);

        AddManualFoodRequest request = new AddManualFoodRequest();
        request.setFoodName("Rice");
        request.setQuantity(1.0);
        request.setMealTime(com.examp.genifit.entity.MealTime.BREAKFAST);

        new DailyLogController(dailyLogService, jwtUtils).addManualFood(
                request,
                new TestingAuthenticationToken("user@example.com", null, "ROLE_USER")
        );

        verify(dailyLogService).addManualFood(eq("user@example.com"), any(AddManualFoodRequest.class));
    }

    @Test
    void getMealHistory_bindsOptionalDateAndAuthenticatedUsername() throws Exception {
        authenticate("user@example.com");
        when(dailyLogService.getMealHistory(eq("user@example.com"), any())).thenReturn(null);

        new DailyLogController(dailyLogService, jwtUtils).getMealHistory(
                java.time.LocalDate.of(2026, 7, 20),
                new TestingAuthenticationToken("user@example.com", null, "ROLE_USER")
        );

        verify(dailyLogService).getMealHistory(eq("user@example.com"), eq(java.time.LocalDate.of(2026, 7, 20)));
    }

    @Test
    void getTodayCalories_usesUserIdResolvedFromJwt() throws Exception {
        authenticate(jwt);
        when(jwtUtils.getUserId(jwt)).thenReturn(5);
        when(dailyLogService.getTodayCalories(5)).thenReturn(null);

        mockMvc.perform(get("/api/daily-logs/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Get today calories successfully"));

        verify(dailyLogService).getTodayCalories(5);
    }

    @Test
    void getCaloriesByDate_bindsRequiredDate() throws Exception {
        authenticate(jwt);
        when(jwtUtils.getUserId(jwt)).thenReturn(5);
        when(dailyLogService.getCaloriesByDate(eq(5), any())).thenReturn(null);

        mockMvc.perform(get("/api/daily-logs").param("date", "2026-07-20"))
                .andExpect(status().isOk());

        verify(dailyLogService).getCaloriesByDate(5, java.time.LocalDate.of(2026, 7, 20));
    }

    @Test
    void getMonthlyLogs_bindsYearAndMonth() throws Exception {
        authenticate(jwt);
        when(jwtUtils.getUserId(jwt)).thenReturn(5);
        when(dailyLogService.getMonthlyLogs(5, 2026, 7)).thenReturn(List.of());

        mockMvc.perform(get("/api/daily-logs/monthly").param("year", "2026").param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Get monthly logs successfully"));

        verify(dailyLogService).getMonthlyLogs(5, 2026, 7);
    }

    @Test
    void getWeeklyChart_usesUserIdResolvedFromJwt() throws Exception {
        authenticate(jwt);
        when(jwtUtils.getUserId(jwt)).thenReturn(5);
        when(dailyLogService.getWeeklyChart(5)).thenReturn(null);

        mockMvc.perform(get("/api/daily-logs/weekly-chart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Get weekly chart successfully"));

        verify(dailyLogService).getWeeklyChart(5);
    }

    private void authenticate(Object principal) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(principal, null, "ROLE_USER"));
    }

    private HandlerMethodArgumentResolver authenticationResolver() {
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                return org.springframework.security.core.Authentication.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(
                    org.springframework.core.MethodParameter parameter,
                    org.springframework.web.method.support.ModelAndViewContainer container,
                    org.springframework.web.context.request.NativeWebRequest request,
                    org.springframework.web.bind.support.WebDataBinderFactory binderFactory
            ) {
                return SecurityContextHolder.getContext().getAuthentication();
            }
        };
    }
}
