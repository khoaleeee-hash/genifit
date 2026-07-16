package com.examp.genifit.dto.response;

import com.examp.genifit.entity.RefundStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CancelSubscriptionResponse {

    private Integer subscriptionId;

    private String subscriptionStatus;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime cancelledAt;

    private Long usedDays;

    private Boolean refundEligible;

    private RefundStatus refundStatus;

    private Integer refundPercent;

    private BigDecimal refundAmount;

    private String refundMessage;
}