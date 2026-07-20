package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.dto.request.GeminiMealSuggestionRequest;
import com.examp.genifit.dto.request.IngredientRequest;
import com.examp.genifit.repository.DailyLogRepository;
import com.examp.genifit.repository.FoodItemRepository;
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
class GeminiMealSuggestionServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private DailyLogRepository dailyLogRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @InjectMocks
    private GeminiMealSuggestionServiceImpl geminiMealSuggestionService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testSuggestMeals_NullUserAndGuest() {
        // Arrange
        GeminiMealSuggestionRequest request = new GeminiMealSuggestionRequest();
        
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            geminiMealSuggestionService.suggestMealsFromIngredients(request);
        });

        // Validates validateRequest
        assertTrue(exception.getMessage().contains("Cần truyền userId hoặc guestId"));
    }

    @Test
    void testSuggestMeals_BothUserAndGuest() {
        // Arrange
        GeminiMealSuggestionRequest request = new GeminiMealSuggestionRequest();
        request.setUserId(1);
        request.setGuestId(1);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            geminiMealSuggestionService.suggestMealsFromIngredients(request);
        });

        // Validates validateRequest
        assertTrue(exception.getMessage().contains("Chỉ được truyền userId hoặc guestId"));
    }

    @Test
    void testSuggestMeals_NoIngredients() {
        // Arrange
        GeminiMealSuggestionRequest request = new GeminiMealSuggestionRequest();
        request.setUserId(1);
        // ingredients is null/empty

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            geminiMealSuggestionService.suggestMealsFromIngredients(request);
        });

        // Validates validateRequest
        assertTrue(exception.getMessage().contains("Cần nhập ít nhất một nguyên liệu"));
    }

    @Test
    void testSuggestMeals_InvalidIngredient() {
        // Arrange
        GeminiMealSuggestionRequest request = new GeminiMealSuggestionRequest();
        request.setUserId(1);
        IngredientRequest ingredient = new IngredientRequest();
        ingredient.setIngredient(""); // empty name
        request.setIngredients(List.of(ingredient));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            geminiMealSuggestionService.suggestMealsFromIngredients(request);
        });

        // Validates validateRequest
        assertTrue(exception.getMessage().contains("Tên nguyên liệu không được để trống"));
    }
}
