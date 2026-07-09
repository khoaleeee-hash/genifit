package com.examp.genifit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class PaymentHistoryResponse {
    private List<PaymentHistoryDto> transactions;
    private Integer nextCursor;  // null = hết data
    private boolean hasMore;

    @Data
    @AllArgsConstructor
    public static class PaymentHistoryDto {
        private Integer transactionId;
        private String orderCode;
        private String planName;
        private BigDecimal amount;
        private String paymentMethod;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
