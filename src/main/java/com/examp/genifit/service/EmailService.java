package com.examp.genifit.service;

import com.examp.genifit.entity.PaymentTransaction;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserSubscription;

public interface EmailService {
    void sendRegistrationOtpEmail(String toEmail, String otpCode);
    void sendForgotPasswordOtpEmail(String toEmail, String otpCode);
    void sendInvoice(User user, PaymentTransaction transaction, UserSubscription subscription);
}
