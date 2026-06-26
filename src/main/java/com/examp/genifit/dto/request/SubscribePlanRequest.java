package com.examp.genifit.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscribePlanRequest {
    @NotNull (message = "PlanId không được để trống")
    private Integer planId;

    private Boolean autoRenew;
}
