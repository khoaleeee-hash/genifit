package com.examp.genifit.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationMeta {

    private Integer pageNum;

    private Integer pageSize;

    private Long totalItems;

    private Integer totalPages;
}