package com.examp.genifit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponseDto {
    private String orderCode;
    private String payUrl;  // frontend mở URL này để thanh toán
}
