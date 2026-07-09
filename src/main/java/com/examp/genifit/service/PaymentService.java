package com.examp.genifit.service;

import com.examp.genifit.dto.response.PaymentHistoryResponse;
import com.examp.genifit.dto.response.PaymentResponseDto;

public interface PaymentService {
    PaymentResponseDto initPayment(Integer userId, Integer planId, String paymentMethod);
    PaymentHistoryResponse getHistory(Integer userId, Integer cursorId, int pageSize);
}