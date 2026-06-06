package com.examp.genifit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FoodItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer foodId;

    @Column(nullable = false, length = 150)
    private String foodName;

    private Double calories;

    private Double protein;

    private Double carbs;

    private Double fat;

    @Column(columnDefinition = "TEXT")
    private String nutritionInfo;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private Boolean isPublic;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FoodApprovalStatus approvalStatus;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "foodItem")
    private List<LogDetail> logDetails = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();

        if (isPublic == null) {
            isPublic = false;
        }

        if (approvalStatus == null) {
            approvalStatus = FoodApprovalStatus.PENDING;
        }
    }

}
