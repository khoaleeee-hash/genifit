package com.examp.genifit.dto;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LogDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer detailId;

    @ManyToOne
    @JoinColumn(name = "log_id", nullable = false)
    private DailyLog dailyLog;

    @ManyToOne
    @JoinColumn(name = "food_id", nullable = false)
    private FoodItem foodItem;

    private Double quantity;

    private Double calories;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FoodSource source;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MealTime mealTime;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();

        if (quantity == null) {
            quantity = 1.0;
        }
    }
}
