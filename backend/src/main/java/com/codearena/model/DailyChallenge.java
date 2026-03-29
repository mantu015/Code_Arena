package com.codearena.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "daily_challenges")
public class DailyChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate challengeDate;

    @Column(nullable = false)
    private Long problemId;

    @Column(nullable = false)
    private int bonusPoints = 50;
}
