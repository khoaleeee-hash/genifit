package com.examp.genifit.dto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer guestId;

    @Column(nullable = false, length = 255)
    private String deviceId;

    private LocalDateTime createdAt;

    private LocalDateTime expiredAt;

    @OneToMany(mappedBy = "guestSession", cascade = CascadeType.ALL)
    private List<DailyLog> dailyLogs = new ArrayList<>();

    @OneToMany(mappedBy = "guestSession", cascade = CascadeType.ALL)
    private List<AIScanHistory> scanHistories = new ArrayList<>();

    @OneToMany(mappedBy = "guestSession", cascade = CascadeType.ALL)
    private List<MealSuggestion> mealSuggestions = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        expiredAt = LocalDateTime.now().plusDays(7);
    }

}
