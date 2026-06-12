package com.examp.genifit.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otpCode);
}
