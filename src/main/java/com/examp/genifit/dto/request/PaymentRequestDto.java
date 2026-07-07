package com.examp.genifit.dto.request;

import lombok.Data;

@Data
public class PaymentRequestDto {
    private Integer userId;
    private Integer planId;
    private String paymentMethod;
}
