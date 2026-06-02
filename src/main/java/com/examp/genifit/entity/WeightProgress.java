package com.examp.genifit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weight_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class WeightProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer progressId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double currentWeight;

    private Double progressPercent;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ProgressStatus progressStatus;

    private LocalDate recordedDate;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();

        if (recordedDate == null) {
            recordedDate = LocalDate.now();
        }
    }
}
