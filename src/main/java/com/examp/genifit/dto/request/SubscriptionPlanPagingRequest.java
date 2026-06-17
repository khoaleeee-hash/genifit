package com.examp.genifit.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanPagingRequest {
    private PageInfoRequest pageInfo;
}
