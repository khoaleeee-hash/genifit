package com.examp.genifit.dto.response;

import com.examp.genifit.entity.ProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightProgressResponse {

    private Integer progressId;

    private Integer userId;

    private LocalDate recordedDate;

    private Double startWeight;

    private Double currentWeight;

    private Double targetWeight;

    private LocalDate targetDate;

    private Double expectedProgressPercent;

    private Double actualProgressPercent;

    private Double differencePercent;

    private ProgressStatus progressStatus;

    private String message;
}