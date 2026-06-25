package com.examp.genifit.dto.response;

import com.examp.genifit.entity.ProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightProgressHistoryResponse {

    private Integer progressId;

    private LocalDate recordedDate;

    private Double currentWeight;

    private Double progressPercent;

    private ProgressStatus progressStatus;

    private LocalDateTime createdAt;
}