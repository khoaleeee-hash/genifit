package com.examp.genifit.dto.response;

import com.examp.genifit.entity.PlanType;
import com.examp.genifit.entity.SubscriptionStatus;
import com.examp.genifit.entity.UserSubscription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonPropertyOrder({
        "subscriptionId",
        "userId",
        "username",
        "planId",
        "planType",
        "planName",
        "startDate",
        "endDate",
        "status",
        "autoRenew",
        "cancelledAt",
        "createdAt",
        "updatedAt"
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

    public UserSubscriptionResponse(UserSubscription subscription) {
        this.subscriptionId = subscription.getSubscriptionId();

        this.userId = subscription.getUser().getUserId();
        this.username = subscription.getUser().getUsername();

        this.planId = subscription.getSubscriptionPlan().getPlanId();
        this.planType = subscription.getSubscriptionPlan().getPlanType();
        this.planName = subscription.getSubscriptionPlan().getPlanName();

        this.startDate = subscription.getStartDate();
        this.endDate = subscription.getEndDate();

        this.status = subscription.getStatus();
        this.autoRenew = subscription.getAutoRenew();

        this.cancelledAt = subscription.getCancelledAt();
        this.createdAt = subscription.getCreatedAt();
        this.updatedAt = subscription.getUpdatedAt();
    }
}