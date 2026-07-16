package com.examp.genifit.dto.request;

import com.examp.genifit.entity.PlanType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateSubscriptionPlanRequest {

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