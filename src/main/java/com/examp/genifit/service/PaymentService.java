package com.examp.genifit.service;

import com.examp.genifit.dto.response.PaymentHistoryResponse;
import com.examp.genifit.dto.response.PaymentResponseDto;
import com.examp.genifit.entity.PaymentTransaction;

public interface PaymentService {

    PaymentResponseDto initPayment(String username, Integer planId, PaymentTransaction.PaymentMethod paymentMethod);

    PaymentHistoryResponse getHistory(String username, Integer cursorId, int pageSize);

    com.examp.genifit.dto.response.AdminPaymentHistoryResponse getAdminHistory(Integer cursorId, int pageSize);
}