package com.examp.genifit.dto.response;

import com.examp.genifit.entity.PlanType;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({
        "planId",
        "planType",
        "planName",
        "description",
        "price",
        "durationDays",
        "maxAiScansPerDay",
        "maxHistoryViewDays",
        "mealSuggestionLimitPerMonth",
        "reminderLimit",
        "maxMembers",
        "maxClients",
        "trial",
        "familySharingEnabled",
        "coachFeaturesEnabled",
        "mealPlanEnabled",
        "weeklyReportEnabled",
        "monthlyReportEnabled",
        "exportReportEnabled",
        "macroTrackingEnabled",
        "calorieDeficitTrackingEnabled",
        "calorieSurplusTrackingEnabled",
        "bloodSugarControlEnabled",
        "active"
})
public class SubscriptionPlanResponse {

    private Integer planId;
    private PlanType planType;
    private String planName;
    private String description;
    private BigDecimal price;
    private Integer durationDays;

    private Integer maxAiScansPerDay;
    private Integer maxHistoryViewDays;
    private Integer mealSuggestionLimitPerMonth;
    private Integer reminderLimit;

    private Integer maxMembers;
    private Integer maxClients;

    private Boolean trial;
    private Boolean familySharingEnabled;
    private Boolean coachFeaturesEnabled;

    private Boolean mealPlanEnabled;
    private Boolean weeklyReportEnabled;
    private Boolean monthlyReportEnabled;
    private Boolean exportReportEnabled;

    private Boolean macroTrackingEnabled;
    private Boolean calorieDeficitTrackingEnabled;
    private Boolean calorieSurplusTrackingEnabled;
    private Boolean bloodSugarControlEnabled;

    private Boolean active;

}