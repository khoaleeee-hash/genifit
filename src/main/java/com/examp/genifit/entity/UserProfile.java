package com.examp.genifit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private String occupation;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private GoalType goal;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ActivityLevel activityLevel;

    @Column
    private Double targetWeightKg;

    @Column
    private Double baseTargetCalorie;

    @Column
    private Double initialWeight;

    @Column
    private LocalDate targetDate;

    @Column
    private LocalDate goalStartDate;

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
