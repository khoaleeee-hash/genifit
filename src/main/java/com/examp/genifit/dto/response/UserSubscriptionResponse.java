package com.examp.genifit.dto.response;

import com.examp.genifit.entity.PlanType;
import com.examp.genifit.entity.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder 
@JsonPropertyOrder({
        "subscriptionId", "userId", "username", "planId", "planType", "planName",
        "startDate", "endDate", "status", "autoRenew", "cancelledAt", "createdAt", "updatedAt"
})
public class UserSubscriptionResponse {
    private Integer subscriptionId;
    private Integer userId;
    private String username;
    private Integer planId;
    private PlanType planType;
    private String planName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SubscriptionStatus status;
    private Boolean autoRenew;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}