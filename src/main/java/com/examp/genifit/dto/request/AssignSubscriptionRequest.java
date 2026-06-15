package com.examp.genifit.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignSubscriptionRequest {
    private Integer userId;
    private Integer planId;
    private Boolean autoRenew;
}