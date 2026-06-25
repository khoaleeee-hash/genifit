package com.examp.genifit.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageInfoResponse {
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPage;
    private Long totalItem;
}
