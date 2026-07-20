package com.examp.genifit.service.serviceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceImplTest {

    @InjectMocks
    private GoogleAuthServiceImpl googleAuthService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(googleAuthService, "googleClientId", "test-client-id");
    }

    @Test
    void testVerifyToken_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid_token_string";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            googleAuthService.verifyToken(invalidToken);
        });
    }
}
