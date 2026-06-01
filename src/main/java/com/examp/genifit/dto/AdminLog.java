package com.examp.genifit.dto;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class AdminLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer adminLogId;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;



    @Column(length = 255)
    private String action;

    @Column(length = 100)
    private String targetTable;

    private Integer targetId;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
