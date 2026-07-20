package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.dto.request.CreateAdminFoodRequest;
import com.examp.genifit.dto.request.UpdateFoodRequest;
import com.examp.genifit.dto.response.FoodResponse;
import com.examp.genifit.entity.FoodApprovalStatus;
import com.examp.genifit.entity.FoodItem;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserRole;
import com.examp.genifit.repository.FoodItemRepository;
import com.examp.genifit.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodServiceImplTest {

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FoodServiceImpl foodService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testCreateFoodByAdmin_Success() {
        // Arrange
        CreateAdminFoodRequest request = new CreateAdminFoodRequest();
        request.setAdminId(1);
        request.setFoodName("Apple");
        request.setCalories(52.0);

        User admin = new User();
        admin.setUserId(1);
        admin.setRole(UserRole.ADMIN);

        FoodItem savedFood = new FoodItem();
        savedFood.setFoodId(1);
        savedFood.setFoodName("Apple");
        savedFood.setCalories(52.0);

        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(foodItemRepository.findByFoodNameIgnoreCaseAndIsPublicTrueAndApprovalStatusAndDeletedFalse(
                "Apple", FoodApprovalStatus.APPROVED)).thenReturn(Optional.empty());
        when(foodItemRepository.save(any(FoodItem.class))).thenReturn(savedFood);

        // Act
        FoodResponse response = foodService.createFoodByAdmin(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getFoodId());
        assertEquals("Apple", response.getFoodName());
        verify(foodItemRepository, times(1)).save(any(FoodItem.class));
    }

    @Test
    void testCreateFoodByAdmin_NotAdmin() {
        // Arrange
        CreateAdminFoodRequest request = new CreateAdminFoodRequest();
        request.setAdminId(1);
        request.setFoodName("Apple");
        request.setCalories(52.0);

        User user = new User();
        user.setUserId(1);
        user.setRole(UserRole.MEMBER); // Not admin

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(ApiException.class, () -> foodService.createFoodByAdmin(request));
    }

    @Test
    void testUpdateFood_Success() {
        // Arrange
        Integer foodId = 1;
        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setFoodName("Banana");
        request.setCalories(89.0);

        FoodItem existingFood = new FoodItem();
        existingFood.setFoodId(foodId);
        existingFood.setFoodName("Apple");

        when(foodItemRepository.findByFoodIdAndDeletedFalse(foodId)).thenReturn(Optional.of(existingFood));
        when(foodItemRepository.save(any(FoodItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FoodResponse response = foodService.updateFood(foodId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Banana", response.getFoodName());
        assertEquals(89.0, response.getCalories());
    }

    @Test
    void testSoftDeleteFood_Success() {
        // Arrange
        Integer foodId = 1;
        FoodItem existingFood = new FoodItem();
        existingFood.setFoodId(foodId);
        existingFood.setDeleted(false);

        when(foodItemRepository.findById(foodId)).thenReturn(Optional.of(existingFood));

        // Act
        foodService.softDeleteFood(foodId);

        // Assert
        assertTrue(existingFood.getDeleted());
        assertFalse(existingFood.getIsPublic());
        verify(foodItemRepository, times(1)).save(existingFood);
    }
}
