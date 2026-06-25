package com.examp.genifit.service;

public interface EmailService {
    void sendRegistrationOtpEmail(String toEmail, String otpCode);
    void sendForgotPasswordOtpEmail(String toEmail, String otpCode);
}
