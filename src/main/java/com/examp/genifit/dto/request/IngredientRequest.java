package com.examp.genifit.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IngredientRequest {
    private String ingredient;
    private Double quantity;
    private String unit;

}
