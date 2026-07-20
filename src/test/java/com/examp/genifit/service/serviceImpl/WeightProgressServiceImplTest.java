package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.dto.request.UpdateWeightProgressRequest;
import com.examp.genifit.dto.response.WeightProgressResponse;
import com.examp.genifit.entity.ProgressStatus;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserProfile;
import com.examp.genifit.entity.WeightProgress;
import com.examp.genifit.repository.UserProfileRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.repository.WeightProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeightProgressServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private WeightProgressRepository weightProgressRepository;

    @InjectMocks
    private WeightProgressServiceImpl weightProgressService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testUpdateWeightProgress_Success() {
        // Arrange
        Integer userId = 1;
        UpdateWeightProgressRequest request = new UpdateWeightProgressRequest();
        request.setCurrentWeight(65.0);

        User user = new User();
        user.setUserId(userId);

        UserProfile userProfile = new UserProfile();
        userProfile.setUser(user);
        userProfile.setInitialWeight(70.0);
        userProfile.setTargetWeightKg(60.0);
        userProfile.setGoalStartDate(LocalDate.now().minusDays(10));
        userProfile.setTargetDate(LocalDate.now().plusDays(20));
        userProfile.setWeightKg(66.0);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(userProfile));
        when(weightProgressRepository.findByUser_UserIdAndRecordedDate(eq(userId), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(weightProgressRepository.save(any(WeightProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        // Act
        WeightProgressResponse response = weightProgressService.updateWeightProgress(userId, request);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(65.0, response.getCurrentWeight());
        assertEquals(65.0, userProfile.getWeightKg());
        verify(weightProgressRepository, times(1)).save(any(WeightProgress.class));
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void testUpdateWeightProgress_UserNotFound() {
        // Arrange
        Integer userId = 1;
        UpdateWeightProgressRequest request = new UpdateWeightProgressRequest();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApiException.class, () -> weightProgressService.updateWeightProgress(userId, request));
    }
}
