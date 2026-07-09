package com.examp.genifit.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRefundResponse {

    private boolean success;

    private boolean pending;

    private String refundTransactionId;

    private String message;
}