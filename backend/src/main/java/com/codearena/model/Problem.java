package com.codearena.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "problems")
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String difficulty; // Easy, Medium, Hard

    @Column(columnDefinition = "TEXT")
    private String testCases; // JSON storing inputs and expected outputs
}
