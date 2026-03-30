package com.codearena.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Data
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code; // FIRST_BLOOD, SPEED_DEMON, etc.
    private String name;
    private String description;
    private String icon; // emoji or icon name
    private String category; // MILESTONE, SPEED, LANGUAGE, STREAK
    private Integer points;
    private String rarity; // COMMON, RARE, EPIC, LEGENDARY

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
