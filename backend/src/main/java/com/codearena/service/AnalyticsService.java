package com.codearena.service;

import com.codearena.model.CodeAnalytics;
import com.codearena.repository.CodeAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    
    private final CodeAnalyticsRepository analyticsRepo;

    public void trackSubmission(Long userId, Long submissionId, String language, String difficulty, 
                                int timeTaken, boolean solved, int codeLength) {
        CodeAnalytics analytics = new CodeAnalytics();
        analytics.setUserId(userId);
        analytics.setSubmissionId(submissionId);
        analytics.setDate(LocalDate.now());
        analytics.setHourOfDay(java.time.LocalTime.now().getHour());
        analytics.setLanguage(language);
        analytics.setDifficulty(difficulty);
        analytics.setTimeTakenSeconds(timeTaken);
        analytics.setSolved(solved);
        analytics.setCodeLength(codeLength);
        analytics.setComplexityScore(calculateComplexity(codeLength, timeTaken));
        analyticsRepo.save(analytics);
    }

    public Map<String, Object> getUserAnalytics(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // Get heatmap data (last 365 days)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(365);
        List<CodeAnalytics> yearData = analyticsRepo.findByUserIdAndDateRange(userId, startDate, endDate);
        
        Map<String, Integer> heatmap = yearData.stream()
            .collect(Collectors.groupingBy(
                a -> a.getDate().toString(),
                Collectors.summingInt(a -> 1)
            ));
        result.put("heatmap", heatmap);
        
        // Language proficiency
        List<Object[]> langStats = analyticsRepo.getLanguageStats(userId);
        Map<String, Long> languageData = new HashMap<>();
        for (Object[] stat : langStats) {
            languageData.put((String) stat[0], (Long) stat[1]);
        }
        result.put("languageProficiency", languageData);
        
        // Hourly performance
        List<Object[]> hourlyStats = analyticsRepo.getHourlyPerformance(userId);
        Map<Integer, Double> hourlyPerformance = new HashMap<>();
        for (Object[] stat : hourlyStats) {
            hourlyPerformance.put((Integer) stat[0], (Double) stat[1]);
        }
        result.put("hourlyPerformance", hourlyPerformance);
        
        // Speed trends (last 30 days)
        LocalDate last30Days = endDate.minusDays(30);
        List<CodeAnalytics> recentData = analyticsRepo.findByUserIdAndDateRange(userId, last30Days, endDate);
        
        double avgTime = recentData.stream()
            .mapToInt(CodeAnalytics::getTimeTakenSeconds)
            .average()
            .orElse(0);
        result.put("averageTimeSeconds", avgTime);
        
        // Difficulty breakdown
        Map<String, Long> difficultyBreakdown = recentData.stream()
            .filter(a -> a.getSolved())
            .collect(Collectors.groupingBy(
                CodeAnalytics::getDifficulty,
                Collectors.counting()
            ));
        result.put("difficultyBreakdown", difficultyBreakdown);
        
        return result;
    }

    private int calculateComplexity(int codeLength, int timeTaken) {
        // Simple complexity score: 1-10 based on code length and time
        int lengthScore = Math.min(codeLength / 50, 5);
        int timeScore = Math.min(timeTaken / 60, 5);
        return Math.max(1, Math.min(10, lengthScore + timeScore));
    }
}
