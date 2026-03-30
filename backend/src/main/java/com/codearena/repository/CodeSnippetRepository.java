package com.codearena.repository;

import com.codearena.model.CodeSnippet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface CodeSnippetRepository extends JpaRepository<CodeSnippet, Long> {
    List<CodeSnippet> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<CodeSnippet> findByIsPublicTrueOrderByUpvotesDesc();
    Optional<CodeSnippet> findByShareToken(String shareToken);
    
    @Query("SELECT s FROM CodeSnippet s WHERE s.isPublic = true AND (s.title LIKE %?1% OR s.tags LIKE %?1%) ORDER BY s.upvotes DESC")
    List<CodeSnippet> searchPublicSnippets(String query);
}
