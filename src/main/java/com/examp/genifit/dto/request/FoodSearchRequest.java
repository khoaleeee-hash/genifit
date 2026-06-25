package com.examp.genifit.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FoodSearchRequest {
    private String keyword;
    private PageInfoRequest pageInfo;
}
