package com.examp.genifit.service;

import com.examp.genifit.dto.response.MoMoRefundResponse;
import com.examp.genifit.entity.SubscriptionPlan;
import com.examp.genifit.entity.User;

import java.math.BigDecimal;
import java.util.Map;

public interface MoMoService {
    String createPayment(User user, SubscriptionPlan plan, String orderCode);
    void handleIPN(Map<String, String> ipnData);
    MoMoRefundResponse refund(String originalOrderId, String originalTransId, BigDecimal amount, String reason);
}
