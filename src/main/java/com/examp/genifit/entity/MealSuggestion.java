package com.examp.genifit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meal_suggestions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class MealSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer suggestionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private Guest guest;

    @Column(length = 150)
    private String suggestedFood;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private Double estimatedCalories;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
