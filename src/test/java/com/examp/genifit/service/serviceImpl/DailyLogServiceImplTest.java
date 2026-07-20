package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.dto.response.DailyCaloriesResponse;
import com.examp.genifit.dto.response.MealHistoryResponse;
import com.examp.genifit.entity.DailyLog;
import com.examp.genifit.entity.StatusColor;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.DailyLogRepository;
import com.examp.genifit.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyLogServiceImplTest {

    @Mock
    private DailyLogRepository dailyLogRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DailyLogServiceImpl dailyLogService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetTodayCalories_Success() {
        // Arrange
        Integer userId = 1;
        LocalDate today = LocalDate.now();
        DailyLog log = new DailyLog();
        log.setLogId(1);
        log.setLogDate(today);
        log.setTotalCalories(1500.0);
        log.setTargetCalories(2000.0);
        log.setStatusColor(StatusColor.GREEN);
        log.setLogDetails(new ArrayList<>());

        when(dailyLogRepository.findByUser_UserIdAndLogDate(eq(userId), any(LocalDate.class))).thenReturn(Optional.of(log));

        // Act
        DailyCaloriesResponse response = dailyLogService.getTodayCalories(userId);

        // Assert
        assertNotNull(response);
        assertEquals(today, response.getDate());
        assertEquals(2000.0, response.getTargetCalories());
        // Since there are no log details, totals will be 0.0
        assertEquals(0.0, response.getTotalCalories());
    }

    @Test
    void testGetTodayCalories_NotFound() {
        // Arrange
        Integer userId = 1;
        when(dailyLogRepository.findByUser_UserIdAndLogDate(eq(userId), any(LocalDate.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApiException.class, () -> dailyLogService.getTodayCalories(userId));
    }

    @Test
    void testGetMealHistory_Success() {
        // Arrange
        String username = "testuser";
        LocalDate date = LocalDate.now();
        
        User user = new User();
        user.setUserId(1);
        
        DailyLog log = new DailyLog();
        log.setLogId(1);
        log.setLogDate(date);
        log.setTotalCalories(1000.0);
        log.setTargetCalories(2000.0);
        log.setLogDetails(new ArrayList<>());

        when(userRepository.findByUsernameAndIsActiveTrue(username)).thenReturn(Optional.of(user));
        when(dailyLogRepository.findByUser_UserIdAndLogDate(1, date)).thenReturn(Optional.of(log));

        // Act
        MealHistoryResponse response = dailyLogService.getMealHistory(username, date);

        // Assert
        assertNotNull(response);
        assertEquals(date, response.getDate());
        assertEquals(1000.0, response.getTotalCalories());
    }
}
