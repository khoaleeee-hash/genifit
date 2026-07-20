package com.examp.genifit.controller;

import com.examp.genifit.service.MoMoService;
import com.examp.genifit.service.PaymentService;
import com.examp.genifit.service.VNPayService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
    @Mock
    private PaymentService paymentService;
    @Mock
    private MoMoService moMoService;
    @Mock
    private VNPayService vnPayService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PaymentController(paymentService, moMoService, vnPayService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void initPayment_usesJwtSubjectAndPaymentRequest() throws Exception {
        authenticate(Jwt.withTokenValue("token").header("alg", "none").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60))
                .claim("sub", "member").build());
        when(paymentService.initPayment(eq("member"), eq(2), any())).thenReturn(null);

        mockMvc.perform(post("/api/payment/init").contentType("application/json")
                        .content("{\"planId\":2,\"paymentMethod\":\"VNPAY\"}"))
                .andExpect(status().isOk());

        verify(paymentService).initPayment(eq("member"), eq(2), any());
    }

    @Test
    void paymentHistory_usesAuthenticatedUsernameAndDefaultPageSize() throws Exception {
        authenticate(User.withUsername("member").password("unused").authorities("USER").build());
        when(paymentService.getHistory("member", null, 10)).thenReturn(null);

        mockMvc.perform(get("/api/payment/history"))
                .andExpect(status().isOk());

        verify(paymentService).getHistory("member", null, 10);
    }

    @Test
    void momoRedirect_handlesSuccessfulCallbackAndRedirects() throws Exception {
        doNothing().when(moMoService).handleIPN(anyMap());

        mockMvc.perform(get("/api/payment/momo/redirect").param("resultCode", "0").param("orderId", "order-1"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://mail.google.com/mail/u/0/#inbox"));

        verify(moMoService).handleIPN(anyMap());
    }

    @Test
    void vnpayRedirect_handlesSuccessAndFailure() throws Exception {
        doNothing().when(vnPayService).handleIPN(anyMap());

        mockMvc.perform(get("/api/payment/vnpay/redirect").param("vnp_ResponseCode", "00"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/payment/vnpay/redirect").param("vnp_ResponseCode", "24"))
                .andExpect(status().isOk());

        verify(vnPayService).handleIPN(anyMap());
    }

    private void authenticate(Object principal) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(principal, null));
    }
}
