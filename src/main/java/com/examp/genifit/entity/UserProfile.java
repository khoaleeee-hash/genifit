package com.examp.genifit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer profileId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private Double heightCm;

    private Double weightKg;

    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private GoalType goal;

    @Column(length = 50)
    private String activityLevel;

    private Double baseTargetCalorie;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "user_medical_conditions",
            joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "condition_name")
    private List<String> medicalConditions = new ArrayList<>(); // bệnh nền

    @ElementCollection
    @CollectionTable(name = "user_allergies",
            joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "allergy")
    private List<String> allergies = new ArrayList<>(); // dị ứng

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
