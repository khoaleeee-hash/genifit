package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.common.exception.ApiException;
import com.examp.genifit.entity.PaymentTransaction;
import com.examp.genifit.entity.SubscriptionPlan;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.PaymentTransactionRepository;
import com.examp.genifit.repository.UserSubscriptionRepository;
import com.examp.genifit.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VNPayServiceImplTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private UserSubscriptionRepository subscriptionRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VNPayServiceImpl vnPayService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(vnPayService, "hashSecret", "dummySecretKey");
    }

    @Test
    void testHandleIPN_TransactionNotFound() {
        // Arrange
        Map<String, String> ipnData = new HashMap<>();
        ipnData.put("vnp_TxnRef", "ORDER123");
        
        when(transactionRepository.findByOrderCode("ORDER123")).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> {
            vnPayService.handleIPN(ipnData);
        });
        
        assertEquals("Không tìm thấy giao dịch: ORDER123", exception.getMessage());
    }

    @Test
    void testHandleIPN_FailedPayment() {
        // Arrange
        Map<String, String> ipnData = new HashMap<>();
        ipnData.put("vnp_TxnRef", "ORDER123");
        ipnData.put("vnp_ResponseCode", "24"); // Failed code
        ipnData.put("vnp_TransactionNo", "TRANS123");

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderCode("ORDER123");

        when(transactionRepository.findByOrderCode("ORDER123")).thenReturn(Optional.of(transaction));

        // Act
        vnPayService.handleIPN(ipnData);

        // Assert
        assertEquals(PaymentTransaction.PaymentStatus.FAILED, transaction.getStatus());
        verify(transactionRepository, times(1)).save(transaction);
        verify(subscriptionRepository, never()).findFirstByUserOrderByCreatedAtDesc(any());
    }

    @Test
    void testHandleIPN_SuccessPayment() {
        // Arrange
        Map<String, String> ipnData = new HashMap<>();
        ipnData.put("vnp_TxnRef", "ORDER123");
        ipnData.put("vnp_ResponseCode", "00"); // Success code
        ipnData.put("vnp_TransactionNo", "TRANS123");

        User user = new User();
        user.setUserId(1);

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setPlanId(1);
        plan.setDurationDays(30);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderCode("ORDER123");
        transaction.setUser(user);
        transaction.setPlan(plan);

        when(transactionRepository.findByOrderCode("ORDER123")).thenReturn(Optional.of(transaction));
        when(subscriptionRepository.findFirstByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());

        // Act
        vnPayService.handleIPN(ipnData);

        // Assert
        assertEquals(PaymentTransaction.PaymentStatus.SUCCESS, transaction.getStatus());
        verify(transactionRepository, times(1)).save(transaction);
        verify(subscriptionRepository, times(1)).save(any());
        verify(emailService, times(1)).sendInvoice(eq(user), eq(transaction), any());
    }
}
