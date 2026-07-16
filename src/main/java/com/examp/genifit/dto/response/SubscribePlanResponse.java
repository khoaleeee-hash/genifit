package com.examp.genifit.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscribePlanResponse {
    private boolean requiresPayment;
    private String paymentUrl;              // null nếu không cần thanh toán
    private UserSubscriptionResponse subscription; // null nếu đang chờ thanh toán
}