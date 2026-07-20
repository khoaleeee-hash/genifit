package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.dto.request.CreateUserRequest;
import com.examp.genifit.dto.response.UserResponse;
import com.examp.genifit.entity.OtpToken;
import com.examp.genifit.entity.PlanType;
import com.examp.genifit.entity.SubscriptionPlan;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserRole;
import com.examp.genifit.mapper.UserMapper;
import com.examp.genifit.repository.OtpTokenRepository;
import com.examp.genifit.repository.SubscriptionPlanRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.repository.UserSubscriptionRepository;
import com.examp.genifit.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OtpTokenRepository otpTokenRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // Mockito tự động inject mock vào UserServiceImpl
    }

    @Test
    void testGenerateAndSendOtp_Success() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // Act
        userService.generateAndSendOtp(email);

        // Assert
        verify(otpTokenRepository, times(1)).deleteByEmail(email);
        verify(otpTokenRepository, times(1)).save(any(OtpToken.class));
        verify(emailService, times(1)).sendRegistrationOtpEmail(eq(email), anyString());
    }

    @Test
    void testGenerateAndSendOtp_UserExisted() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        assertThrows(ApiException.class, () -> userService.generateAndSendOtp(email));
        verify(otpTokenRepository, never()).save(any());
    }

    @Test
    void testCreateUser_Success() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setOtpCode("123456");
        request.setPasswordHash("password");

        OtpToken otpToken = new OtpToken();
        otpToken.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        User mappedUser = new User();
        SubscriptionPlan freePlan = new SubscriptionPlan();
        UserResponse response = new UserResponse();
        response.setUsername("testuser");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(otpTokenRepository.findByEmailAndOtpCode("test@example.com", "123456")).thenReturn(Optional.of(otpToken));
        when(userMapper.toUser(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode("password")).thenReturn("encoded_pass");
        when(subscriptionPlanRepository.findFirstByPlanType(PlanType.FREE)).thenReturn(Optional.of(freePlan));
        when(userMapper.toUserResponse(mappedUser)).thenReturn(response);

        // Act
        UserResponse result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(mappedUser);
        verify(userSubscriptionRepository, times(1)).save(any());
        verify(otpTokenRepository, times(1)).delete(otpToken);
    }
    
    @Test
    void testGetUser_Success() {
        User user = new User();
        user.setUserId(1);
        UserResponse response = new UserResponse();
        response.setUserId(1);
        
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(response);
        
        UserResponse result = userService.getUser(1);
        assertNotNull(result);
        assertEquals(1, result.getUserId());
    }
}
