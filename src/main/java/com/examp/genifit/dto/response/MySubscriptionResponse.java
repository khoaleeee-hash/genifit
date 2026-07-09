package com.examp.genifit.dto.response;

import com.examp.genifit.entity.UserSubscription;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MySubscriptionResponse {

    private Integer subscriptionId;

    private Integer planId;

    private String planType;

    private String planName;

    private String description;

    private Boolean active;

    private String status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer aiScanLimitPerMonth;

    private Integer mealSuggestionLimitPerMonth;

    private Integer reminderLimit;

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

}