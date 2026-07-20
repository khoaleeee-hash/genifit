package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.entity.PaymentTransaction;
import com.examp.genifit.entity.SubscriptionPlan;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserSubscription;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testSendRegistrationOtpEmail() {
        // Arrange
        String toEmail = "test@example.com";
        String otpCode = "123456";

        // Act
        emailService.sendRegistrationOtpEmail(toEmail, otpCode);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendForgotPasswordOtpEmail() {
        // Arrange
        String toEmail = "test@example.com";
        String otpCode = "654321";

        // Act
        emailService.sendForgotPasswordOtpEmail(toEmail, otpCode);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendInvoice() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setPlanName("Premium");

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderCode("ORD123");
        transaction.setAmount(java.math.BigDecimal.valueOf(100000.0));
        transaction.setPaymentMethod(PaymentTransaction.PaymentMethod.MOMO);
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setPlan(plan);

        UserSubscription subscription = new UserSubscription();
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.sendInvoice(user, transaction, subscription);

        // Assert
        verify(mailSender, times(1)).send(mimeMessage);
    }
}
