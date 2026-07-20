package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.response.FoodRecommendationResponse;
import com.examp.genifit.entity.DailyLog;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserProfile;
import com.examp.genifit.repository.DailyLogRepository;
import com.examp.genifit.repository.UserProfileRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.prompt.FoodRecommendationPrompt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoodRecommendationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private DailyLogRepository dailyLogRepository;

    @Mock
    private FoodRecommendationPrompt recommendationPrompt;

    @InjectMocks
    private FoodRecommendationServiceImpl foodRecommendationService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testRecommend_UserNotFound() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            foodRecommendationService.recommend(1, "BREAKFAST");
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testRecommend_ProfileNotFound() {
        // Arrange
        User user = new User();
        user.setUserId(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUser(user)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            foodRecommendationService.recommend(1, "BREAKFAST");
        });

        assertEquals("Profile not found", exception.getMessage());
    }

    @Test
    void testRecommend_DailyLogNotFound() {
        // Arrange
        User user = new User();
        user.setUserId(1);
        UserProfile profile = new UserProfile();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(dailyLogRepository.findByUserAndLogDate(any(), any())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            foodRecommendationService.recommend(1, "BREAKFAST");
        });

        assertEquals("Daily log not found", exception.getMessage());
    }

    @Test
    void testRecommend_NoRemainingCalories() {
        // Arrange
        User user = new User();
        user.setUserId(1);
        UserProfile profile = new UserProfile();
        
        DailyLog log = new DailyLog();
        log.setTargetCalories(2000.0);
        log.setTotalCalories(2500.0); // Exceeded

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));
        when(dailyLogRepository.findByUserAndLogDate(any(), any())).thenReturn(Optional.of(log));

        // Act
        FoodRecommendationResponse response = foodRecommendationService.recommend(1, "BREAKFAST");

        // Assert
        assertNotNull(response);
        assertEquals(0.0, response.getRemainingCalories());
        assertTrue(response.getSuggestions().isEmpty());
    }
}
