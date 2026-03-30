package com.codearena.repository;

import com.codearena.model.CodeAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface CodeAnalyticsRepository extends JpaRepository<CodeAnalytics, Long> {
    List<CodeAnalytics> findByUserIdOrderByDateDesc(Long userId);
    
    @Query("SELECT a FROM CodeAnalytics a WHERE a.userId = ?1 AND a.date BETWEEN ?2 AND ?3")
    List<CodeAnalytics> findByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end);
    
    @Query("SELECT a.language, COUNT(a) FROM CodeAnalytics a WHERE a.userId = ?1 GROUP BY a.language")
    List<Object[]> getLanguageStats(Long userId);
    
    @Query("SELECT a.hour, AVG(a.complexityScore) FROM CodeAnalytics a WHERE a.userId = ?1 GROUP BY a.hour ORDER BY a.hour")
    List<Object[]> getHourlyPerformance(Long userId);
}
