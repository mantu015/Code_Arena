package com.codearena.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_analytics")
@Data
public class CodeAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "submission_id")
    private Long submissionId;

    private LocalDate date;
    private Integer hourOfDay; // 0-23 for time-of-day analysis

    private String language;
    private String difficulty;
    
    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;
    
    @Column(name = "attempts_count")
    private Integer attemptsCount = 1;
    
    private Boolean solved;
    
    @Column(name = "code_length")
    private Integer codeLength;
    
    @Column(name = "complexity_score")
    private Integer complexityScore; // 1-10
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
