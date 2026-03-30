package com.codearena.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(exclude = {"claimedUserIds"})
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

    @Column(columnDefinition = "INT DEFAULT 50")
    private int bonusPoints = 50;

    @Column(columnDefinition = "VARCHAR(2000) DEFAULT ''")
    private String claimedUserIds = "";

    public boolean hasUserClaimed(Long userId) {
        if (claimedUserIds == null || claimedUserIds.isBlank()) return false;
        for (String id : claimedUserIds.split(",")) {
            if (id.trim().equals(userId.toString())) return true;
        }
        return false;
    }

    public void addClaimedUser(Long userId) {
        claimedUserIds = (claimedUserIds == null || claimedUserIds.isBlank())
            ? userId.toString()
            : claimedUserIds + "," + userId;
    }
}
