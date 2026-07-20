package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.dto.request.CreateSubscriptionPlanRequest;
import com.examp.genifit.dto.response.SubscriptionPlanResponse;
import com.examp.genifit.entity.PlanType;
import com.examp.genifit.entity.SubscriptionPlan;
import com.examp.genifit.mapper.SubscriptionPlanMapper;
import com.examp.genifit.repository.SubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanServiceImplTest {

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private SubscriptionPlanMapper subscriptionPlanMapper;

    @InjectMocks
    private SubscriptionPlanServiceImpl subscriptionPlanService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetPlanById_Success() {
        // Arrange
        Integer planId = 1;
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setPlanId(planId);

        SubscriptionPlanResponse response = new SubscriptionPlanResponse();
        response.setPlanId(planId);

        when(subscriptionPlanRepository.findByPlanIdAndDeletedFalse(planId)).thenReturn(Optional.of(plan));
        when(subscriptionPlanMapper.toResponse(plan)).thenReturn(response);

        // Act
        SubscriptionPlanResponse result = subscriptionPlanService.getPlanById(planId);

        // Assert
        assertNotNull(result);
        assertEquals(planId, result.getPlanId());
    }

    @Test
    void testGetPlanById_NotFound() {
        // Arrange
        Integer planId = 1;
        when(subscriptionPlanRepository.findByPlanIdAndDeletedFalse(planId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApiException.class, () -> subscriptionPlanService.getPlanById(planId));
    }

    @Test
    void testCreatePlan_Success() {
        // Arrange
        CreateSubscriptionPlanRequest request = new CreateSubscriptionPlanRequest();
        request.setPlanType(PlanType.FREE);
        request.setPlanName("Free Plan");
        request.setPrice(BigDecimal.ZERO);
        request.setDurationDays(30);

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setPlanType(PlanType.FREE);

        SubscriptionPlanResponse response = new SubscriptionPlanResponse();
        response.setPlanName("Free Plan");

        when(subscriptionPlanRepository.existsByPlanTypeAndDeletedFalse(PlanType.FREE)).thenReturn(false);
        when(subscriptionPlanRepository.existsByPlanNameIgnoreCaseAndDeletedFalse("Free Plan")).thenReturn(false);
        when(subscriptionPlanMapper.toEntity(request)).thenReturn(plan);
        when(subscriptionPlanRepository.save(any(SubscriptionPlan.class))).thenReturn(plan);
        when(subscriptionPlanMapper.toResponse(plan)).thenReturn(response);

        // Act
        SubscriptionPlanResponse result = subscriptionPlanService.createPlan(request);

        // Assert
        assertNotNull(result);
        assertEquals("Free Plan", result.getPlanName());
        verify(subscriptionPlanRepository, times(1)).save(plan);
    }
}
