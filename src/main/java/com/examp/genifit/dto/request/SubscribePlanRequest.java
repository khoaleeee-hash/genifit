package com.examp.genifit.dto.request;

import com.examp.genifit.entity.PaymentTransaction;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscribePlanRequest {
    @NotNull(message = "PlanId không được để trống")
    private Integer planId;

    private Boolean autoRenew;

    // Bắt buộc nếu gói là trả phí, null nếu gói free
    private PaymentTransaction.PaymentMethod paymentMethod;
}