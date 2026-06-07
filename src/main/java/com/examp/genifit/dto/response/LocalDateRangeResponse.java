package com.examp.genifit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalDateRangeResponse {

    private LocalDate startDate;

    private LocalDate endDate;
}