package com.examp.genifit.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private DailyLog dailyLog;

    /*
     * Có thể null vì:
     * - Nếu user chọn món có sẵn bằng foodId thì có foodItem
     * - Nếu user nhập tay thì không có foodItem
     * - Nếu user lấy từ scanId thì không có foodItem
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = true)
    private FoodItem foodItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id", nullable = true)
    private AIScanHistory scanHistory;

    @Column(name = "food_name_snapshot", length = 255)
    private String foodNameSnapshot;

    private Double quantity;

    private Double calories;

    private Double fat;

    private Double carbs;

    private Double protein;

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