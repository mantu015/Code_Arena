package com.codearena.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_snippets")
@Data
public class CodeSnippet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String title;
    
    @Column(length = 10000)
    private String code;
    
    private String language;
    private String description;
    private String tags; // comma-separated
    
    @Column(name = "is_public")
    private Boolean isPublic = false;
    
    private Integer upvotes = 0;
    private Integer downvotes = 0;
    private Integer views = 0;
    
    @Column(name = "share_token", unique = true)
    private String shareToken; // unique URL token
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
