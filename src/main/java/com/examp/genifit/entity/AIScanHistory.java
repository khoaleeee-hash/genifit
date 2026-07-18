package com.examp.genifit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_scan_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer scanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private Guest guest;

    @Column(length = 255)
    private String imageUrl;

    @Column(length = 500)
    private String detectedFood;

    private Double estimatedCalories;

    private Double protein;

    private Double carbs;

    private Double fat;

    private Double quantity;

    @Column(length = 50)
    private String unit;

    @Column(columnDefinition = "TEXT")
    private String nutritionResult;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private SuitabilityStatus suitabilityStatus;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if(createdAt == null){
            createdAt = LocalDateTime.now();
        }
    }
}