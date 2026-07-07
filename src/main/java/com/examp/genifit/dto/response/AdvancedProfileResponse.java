package com.examp.genifit.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
public class AdvancedProfileResponse {

    private Integer advancedProfileId;
    private Integer userId;

    private Double initialWeight;
    private Double targetWeight;
    private LocalDate targetDate;
    private Double dailyTargetCalorie;

    private List<String> medicalConditions;
    private List<String> allergies;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}