package com.examp.genifit.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminPaymentHistoryResponse(
        List<AdminPaymentHistoryDto> transactions,
        Integer nextCursor,
        boolean hasMore
) {
    public record AdminPaymentHistoryDto(
            Integer transactionId,
            String orderCode,
            String planName,
            BigDecimal amount,
            String paymentMethod,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Integer userId,
            String username,
            String email,
            String avatarUrl
    ) {}
}
