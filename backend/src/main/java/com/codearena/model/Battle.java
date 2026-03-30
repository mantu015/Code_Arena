package com.codearena.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "battles")
@Data
public class Battle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id")
    private Long problemId;

    @Column(name = "player1_id")
    private Long player1Id;

    @Column(name = "player2_id")
    private Long player2Id;

    @Column(name = "winner_id")
    private Long winnerId;

    private String status; // WAITING, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(name = "player1_progress")
    private Integer player1Progress = 0; // test cases passed

    @Column(name = "player2_progress")
    private Integer player2Progress = 0;

    @Column(name = "player1_time")
    private Long player1Time; // completion time in ms

    @Column(name = "player2_time")
    private Long player2Time;

    @Column(name = "battle_code", unique = true)
    private String battleCode; // unique code to join battle

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
