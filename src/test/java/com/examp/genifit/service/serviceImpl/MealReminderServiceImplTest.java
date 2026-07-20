package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.request.CreateMealReminderRequest;
import com.examp.genifit.entity.MealReminder;
import com.examp.genifit.entity.MealTime;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.MealReminderRepository;
import com.examp.genifit.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealReminderServiceImplTest {

    @Mock
    private MealReminderRepository mealReminderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MealReminderServiceImpl mealReminderService;

    @BeforeEach
    void setUp() {
        // Setup SecurityContext for getting current user
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("testuser");
        when(authentication.getName()).thenReturn("testuser");
        
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreateReminder_UserNotFound() {
        // Arrange
        CreateMealReminderRequest request = new CreateMealReminderRequest();
        when(userRepository.findByUsernameAndIsActiveTrue("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            mealReminderService.createReminder(request);
        });

        assertEquals("Không tìm thấy user hiện tại", exception.getMessage());
    }

    @Test
    void testCreateReminder_NullRequest() {
        // Arrange
        User user = new User();
        when(userRepository.findByUsernameAndIsActiveTrue("testuser")).thenReturn(Optional.of(user));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            mealReminderService.createReminder(null);
        });

        assertEquals("Request không được để trống", exception.getMessage());
    }

    @Test
    void testCreateReminder_NullMealTime() {
        // Arrange
        User user = new User();
        when(userRepository.findByUsernameAndIsActiveTrue("testuser")).thenReturn(Optional.of(user));
        
        CreateMealReminderRequest request = new CreateMealReminderRequest();

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            mealReminderService.createReminder(request);
        });

        assertEquals("mealTime không được để trống", exception.getMessage());
    }

    @Test
    void testCreateReminder_InvalidTimeFormat() {
        // Arrange
        User user = new User();
        when(userRepository.findByUsernameAndIsActiveTrue("testuser")).thenReturn(Optional.of(user));
        
        CreateMealReminderRequest request = new CreateMealReminderRequest();
        request.setMealTime(MealTime.BREAKFAST);
        request.setReminderTime("25:00"); // invalid time

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            mealReminderService.createReminder(request);
        });

        assertEquals("reminderTime không hợp lệ. Format đúng là HH:mm, ví dụ 07:30", exception.getMessage());
    }

    @Test
    void testCreateReminder_DuplicateTime() {
        // Arrange
        User user = new User();
        when(userRepository.findByUsernameAndIsActiveTrue("testuser")).thenReturn(Optional.of(user));
        
        CreateMealReminderRequest request = new CreateMealReminderRequest();
        request.setMealTime(MealTime.BREAKFAST);
        request.setReminderTime("07:30");

        when(mealReminderRepository.existsByUserAndReminderTime(any(), any())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            mealReminderService.createReminder(request);
        });

        assertEquals("Bạn đã có lịch nhắc tại thời gian này", exception.getMessage());
    }
}
