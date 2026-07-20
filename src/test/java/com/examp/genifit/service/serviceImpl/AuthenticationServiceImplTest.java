package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.dto.request.AuthenticationRequest;
import com.examp.genifit.dto.response.AuthenticationResponse;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.*;
import com.examp.genifit.service.GoogleAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private InvalidatedTokenRepository invalidatedTokenRepository;
    @Mock
    private GoogleAuthService googleAuthService;
    @Mock
    private OtpTokenRepository otpTokenRepository;
    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationServiceImpl(
                userRepository, passwordEncoder, invalidatedTokenRepository,
                googleAuthService, otpTokenRepository, subscriptionPlanRepository, userSubscriptionRepository
        );
        // Khởi tạo SIGNER_KEY cho test sinh JWT
        authenticationService.SIGNER_KEY = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";
    }

    @Test
    void testAuthenticate_Success() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("testuser", "password123");
        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("hashedPassword");

        when(userRepository.findByUsernameAndIsActiveTrue("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

        // Act
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isAuthenticated());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    void testAuthenticate_UserNotFound() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("unknown", "password123");

        when(userRepository.findByUsernameAndIsActiveTrue("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApiException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    void testAuthenticate_WrongPassword() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("testuser", "wrongpassword");
        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("hashedPassword");

        when(userRepository.findByUsernameAndIsActiveTrue("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(ApiException.class, () -> authenticationService.authenticate(request));
    }
}
