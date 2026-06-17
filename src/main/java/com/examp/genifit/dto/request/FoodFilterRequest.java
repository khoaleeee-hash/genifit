package com.examp.genifit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodFilterRequest {

    private SearchCondition searchCondition;

    private PageInfoRequest pageInfo;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchCondition {

        @Schema(example = "")
        private String foodId;

        @Schema(example = "")
        private String keyword;

        @Schema(example = "")
        private String calories;

        @Schema(example = "")
        private String caloriesFrom;

        @Schema(example = "")
        private String caloriesTo;

        @Schema(example = "")
        private String proteinFrom;

        @Schema(example = "")
        private String proteinTo;

        @Schema(example = "")
        private String carbsFrom;

        @Schema(example = "")
        private String carbsTo;

        @Schema(example = "")
        private String fatFrom;

        @Schema(example = "")
        private String fatTo;

        @Schema(example = "")
        private String isPublic;

        @Schema(example = "")
        private String isDeleted;
    }
}