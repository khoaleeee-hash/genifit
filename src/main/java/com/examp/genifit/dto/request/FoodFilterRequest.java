package com.examp.genifit.dto.request;

import com.examp.genifit.dto.response.PageInfoResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodFilterRequest {

    private SearchCondition searchCondition;

    private PageInfoResponse pageInfo;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchCondition {

        private Integer foodId;

        private String keyword;

        private Double calories;

        private Double caloriesFrom;

        private Double caloriesTo;

        private Double proteinFrom;

        private Double proteinTo;

        private Double carbsFrom;

        private Double carbsTo;

        private Double fatFrom;

        private Double fatTo;

        private Boolean isPublic;

        private Boolean isDeleted;
    }
}