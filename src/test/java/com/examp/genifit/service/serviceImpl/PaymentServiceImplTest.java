package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.dto.response.PaymentHistoryResponse;
import com.examp.genifit.dto.response.PaymentResponseDto;
import com.examp.genifit.entity.PaymentTransaction;
import com.examp.genifit.entity.SubscriptionPlan;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.PaymentTransactionRepository;
import com.examp.genifit.repository.SubscriptionPlanRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.MoMoService;
import com.examp.genifit.service.VNPayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionPlanRepository planRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private MoMoService moMoService;

    @Mock
    private VNPayService vnPayService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User user;
    private SubscriptionPlan plan;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1);
        user.setUsername("testuser");

        plan = new SubscriptionPlan();
        plan.setPlanId(1);
        plan.setPrice(new BigDecimal("100000"));
        plan.setDeleted(false);
        plan.setActive(true);
    }

    @Test
    void testInitPayment_MoMo() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(planRepository.findById(1)).thenReturn(Optional.of(plan));
        when(moMoService.createPayment(any(), any(), any())).thenReturn("momoUrl");

        // Act
        PaymentResponseDto response = paymentService.initPayment("testuser", 1, PaymentTransaction.PaymentMethod.MOMO);

        // Assert
        assertNotNull(response);
        assertEquals("momoUrl", response.getPayUrl());
        verify(transactionRepository, times(1)).save(any(PaymentTransaction.class));
    }

    @Test
    void testInitPayment_VNPay() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(planRepository.findById(1)).thenReturn(Optional.of(plan));
        when(vnPayService.createPayment(any(), any(), any())).thenReturn("vnpayUrl");

        // Act
        PaymentResponseDto response = paymentService.initPayment("testuser", 1, PaymentTransaction.PaymentMethod.VNPAY);

        // Assert
        assertNotNull(response);
        assertEquals("vnpayUrl", response.getPayUrl());
        verify(transactionRepository, times(1)).save(any(PaymentTransaction.class));
    }

    @Test
    void testInitPayment_PlanDeleted() {
        // Arrange
        plan.setDeleted(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(planRepository.findById(1)).thenReturn(Optional.of(plan));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            paymentService.initPayment("testuser", 1, PaymentTransaction.PaymentMethod.MOMO);
        });

        assertEquals("Gói đăng ký đã bị xóa", exception.getMessage());
    }

    @Test
    void testInitPayment_FreePlan() {
        // Arrange
        plan.setPrice(BigDecimal.ZERO);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(planRepository.findById(1)).thenReturn(Optional.of(plan));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            paymentService.initPayment("testuser", 1, PaymentTransaction.PaymentMethod.MOMO);
        });

        assertEquals("Gói miễn phí không cần thanh toán", exception.getMessage());
    }

    @Test
    void testGetHistory_FirstPage() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        PaymentTransaction tx = new PaymentTransaction();
        tx.setTransactionId(1);
        tx.setPlan(plan);
        tx.setPaymentMethod(PaymentTransaction.PaymentMethod.MOMO);
        tx.setStatus(PaymentTransaction.PaymentStatus.SUCCESS);

        when(transactionRepository.findByUserOrderByTransactionIdDesc(eq(user), any(Pageable.class)))
                .thenReturn(List.of(tx));

        // Act
        PaymentHistoryResponse response = paymentService.getHistory("testuser", null, 10);

        // Assert
        assertNotNull(response);
        assertFalse(response.isHasMore());
        assertEquals(1, response.getTransactions().size());
    }
}
