package com.examp.genifit.dto;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class DailyLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer logId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    private Guest guestSession;

    @Column(nullable = false)
    private LocalDate logDate;

    private Double totalCalories;

    private Double targetCalories;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusColor statusColor;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "dailyLog", cascade = CascadeType.ALL)
    private List<LogDetail> logDetails = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();

        if (totalCalories == null) {
            totalCalories = 0.0;
        }
    }
}
