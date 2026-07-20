package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.dto.request.FoodEvaluationRequest;
import com.examp.genifit.repository.DailyLogRepository;
import com.examp.genifit.repository.GuestRepository;
import com.examp.genifit.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FoodEvaluationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private DailyLogRepository dailyLogRepository;

    @InjectMocks
    private FoodEvaluationServiceImpl foodEvaluationService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testEvaluateScannedFood_NullUserAndGuest() {
        // Arrange
        FoodEvaluationRequest request = new FoodEvaluationRequest();

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            foodEvaluationService.evaluateScannedFood(request);
        });

        // Validates validateRequest
        assertTrue(exception.getMessage().contains("Cần truyền userId hoặc guestId"));
    }

    @Test
    void testEvaluateScannedFood_BothUserAndGuest() {
        // Arrange
        FoodEvaluationRequest request = new FoodEvaluationRequest();
        request.setUserId(1);
        request.setGuestId(1);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            foodEvaluationService.evaluateScannedFood(request);
        });

        // Validates validateRequest
        assertTrue(exception.getMessage().contains("Chỉ được truyền userId hoặc guestId"));
    }

    @Test
    void testEvaluateScannedFood_NoFoods() {
        // Arrange
        FoodEvaluationRequest request = new FoodEvaluationRequest();
        request.setUserId(1);
        request.setFoods(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            foodEvaluationService.evaluateScannedFood(request);
        });

        // Validates validateRequest
        assertTrue(exception.getMessage().contains("Không có dữ liệu món ăn để đánh giá"));
    }
}
