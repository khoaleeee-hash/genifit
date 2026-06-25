package com.examp.genifit.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanSearchRequest {
    private String keyword;
    private PageInfoRequest pageInfo;
}
