package com.codearena.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "submissions")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long problemId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String status; // Pending, Accepted, Wrong Answer, Error

    @Column
    private java.time.LocalDateTime submittedAt = java.time.LocalDateTime.now();
}
