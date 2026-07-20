package com.examp.genifit.controller;

import com.examp.genifit.common.security.JwtUtils;
import com.examp.genifit.dto.request.UpdateWeightProgressRequest;
import com.examp.genifit.dto.response.WeightProgressHistoryResponse;
import com.examp.genifit.service.WeightProgressService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
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
class WeightProgressControllerTest {

    @Mock
    private WeightProgressService weightProgressService;
    @Mock
    private JwtUtils jwtUtils;

    private MockMvc mockMvc;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WeightProgressController(weightProgressService, jwtUtils))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("sub", "user@example.com")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(jwt, null));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateWeightProgress_bindsRequestAndResolvedUser() throws Exception {
        when(jwtUtils.getUserId(jwt)).thenReturn(5);
        when(weightProgressService.updateWeightProgress(eq(5), any())).thenReturn(null);

        mockMvc.perform(post("/api/weight-progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentWeight\":65.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Update weight progress successfully"));

        verify(weightProgressService).updateWeightProgress(eq(5), any(UpdateWeightProgressRequest.class));
    }

    @Test
    void getWeightProgressHistory_usesDefaultPagination() throws Exception {
        when(jwtUtils.getUserId(jwt)).thenReturn(5);
        when(weightProgressService.getWeightProgressHistory(5, 0, 10)).thenReturn(new PageImpl<WeightProgressHistoryResponse>(List.of()));

        mockMvc.perform(get("/api/weight-progress/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Get weight progress history successfully"));

        verify(weightProgressService).getWeightProgressHistory(5, 0, 10);
    }

    @Test
    void getWeightProgressHistory_bindsExplicitPagination() throws Exception {
        when(jwtUtils.getUserId(jwt)).thenReturn(5);
        when(weightProgressService.getWeightProgressHistory(5, 2, 20)).thenReturn(new PageImpl<WeightProgressHistoryResponse>(List.of()));

        mockMvc.perform(get("/api/weight-progress/history").param("pageNum", "2").param("pageSize", "20"))
                .andExpect(status().isOk());

        verify(weightProgressService).getWeightProgressHistory(5, 2, 20);
    }
}
