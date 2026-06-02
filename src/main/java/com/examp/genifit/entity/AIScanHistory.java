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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private Guest guestSession;

    @Column(length = 255)
    private String imageUrl;

    @Column(length = 150)
    private String detectedFood;

    private Double estimatedCalories;

    @Column(columnDefinition = "TEXT")
    private String nutritionResult;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private SuitabilityStatus suitabilityStatus;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
