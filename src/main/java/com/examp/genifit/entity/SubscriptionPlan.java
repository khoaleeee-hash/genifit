package com.examp.genifit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer planId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private PlanType planType;

    @Column(nullable = false, length = 100)
    private String planName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer durationDays;

    @Column(nullable = false)
    private Integer aiScanLimitPerMonth;

    @Column(nullable = false)
    private Integer mealSuggestionLimitPerMonth;

    @Column(nullable = false)
    private Integer reminderLimit;

    @Column(nullable = false)
    private Integer maxMembers;

    @Column(nullable = false)
    private Integer maxClients;

    @Column(nullable = false)
    private Boolean trial;

    @Column(nullable = false)
    private Boolean familySharingEnabled;

    @Column(nullable = false)
    private Boolean coachFeaturesEnabled;

    @Column(nullable = false)
    private Boolean mealPlanEnabled;

    @Column(nullable = false)
    private Boolean weeklyReportEnabled;

    @Column(nullable = false)
    private Boolean monthlyReportEnabled;

    @Column(nullable = false)
    private Boolean exportReportEnabled;

    @Column(nullable = false)
    private Boolean macroTrackingEnabled;

    @Column(nullable = false)
    private Boolean calorieDeficitTrackingEnabled;

    @Column(nullable = false)
    private Boolean calorieSurplusTrackingEnabled;

    @Column(nullable = false)
    private Boolean bloodSugarControlEnabled;

    @Column(nullable = false)
    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (price == null) {
            price = BigDecimal.ZERO;
        }

        if (durationDays == null) {
            durationDays = 30;
        }

        if (aiScanLimitPerMonth == null) {
            aiScanLimitPerMonth = 0;
        }

        if (mealSuggestionLimitPerMonth == null) {
            mealSuggestionLimitPerMonth = 0;
        }

        if (reminderLimit == null) {
            reminderLimit = 0;
        }

        if (maxMembers == null) {
            maxMembers = 1;
        }

        if (maxClients == null) {
            maxClients = 0;
        }

        if (trial == null) {
            trial = false;
        }

        if (familySharingEnabled == null) {
            familySharingEnabled = false;
        }

        if (coachFeaturesEnabled == null) {
            coachFeaturesEnabled = false;
        }

        if (mealPlanEnabled == null) {
            mealPlanEnabled = false;
        }

        if (weeklyReportEnabled == null) {
            weeklyReportEnabled = false;
        }

        if (monthlyReportEnabled == null) {
            monthlyReportEnabled = false;
        }

        if (exportReportEnabled == null) {
            exportReportEnabled = false;
        }

        if (macroTrackingEnabled == null) {
            macroTrackingEnabled = false;
        }

        if (calorieDeficitTrackingEnabled == null) {
            calorieDeficitTrackingEnabled = false;
        }

        if (calorieSurplusTrackingEnabled == null) {
            calorieSurplusTrackingEnabled = false;
        }

        if (bloodSugarControlEnabled == null) {
            bloodSugarControlEnabled = false;
        }

        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}