package com.codearena.service;

import com.codearena.model.Achievement;
import com.codearena.model.UserAchievement;
import com.codearena.repository.AchievementRepository;
import com.codearena.repository.UserAchievementRepository;
import com.codearena.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {
    
    private final AchievementRepository achievementRepo;
    private final UserAchievementRepository userAchievementRepo;
    private final SubmissionRepository submissionRepo;

    public void checkAndUnlockAchievements(Long userId) {
        long solvedCount = submissionRepo.countByUserIdAndStatus(userId, "Accepted");
        
        // Check milestone achievements
        checkMilestone(userId, "FIRST_BLOOD", 1, solvedCount);
        checkMilestone(userId, "PROBLEM_SOLVER", 10, solvedCount);
        checkMilestone(userId, "CODE_WARRIOR", 25, solvedCount);
        checkMilestone(userId, "CODE_MASTER", 50, solvedCount);
        checkMilestone(userId, "LEGEND", 100, solvedCount);
    }

    private void checkMilestone(Long userId, String code, long target, long current) {
        if (current >= target) {
            Optional<Achievement> achievement = achievementRepo.findByCode(code);
            if (achievement.isPresent()) {
                Optional<UserAchievement> existing = userAchievementRepo
                    .findByUserIdAndAchievementId(userId, achievement.get().getId());
                
                if (existing.isEmpty()) {
                    UserAchievement ua = new UserAchievement();
                    ua.setUserId(userId);
                    ua.setAchievementId(achievement.get().getId());
                    ua.setProgress((int) current);
                    ua.setTarget((int) target);
                    userAchievementRepo.save(ua);
                }
            }
        }
    }

    public List<Map<String, Object>> getUserAchievements(Long userId) {
        List<UserAchievement> userAchievements = userAchievementRepo.findByUserId(userId);
        return userAchievements.stream().map(ua -> {
            Optional<Achievement> achievement = achievementRepo.findById(ua.getAchievementId());
            Map<String, Object> result = new HashMap<>();
            if (achievement.isPresent()) {
                Achievement a = achievement.get();
                result.put("id", a.getId());
                result.put("code", a.getCode());
                result.put("name", a.getName());
                result.put("description", a.getDescription());
                result.put("icon", a.getIcon());
                result.put("category", a.getCategory());
                result.put("points", a.getPoints());
                result.put("rarity", a.getRarity());
                result.put("unlockedAt", ua.getUnlockedAt());
            }
            return result;
        }).collect(Collectors.toList());
    }

    public List<Achievement> getAllAchievements() {
        return achievementRepo.findAll();
    }

    public void initializeAchievements() {
        if (achievementRepo.count() > 0) return;

        List<Achievement> achievements = new ArrayList<>();
        
        // Milestone achievements
        achievements.add(createAchievement("FIRST_BLOOD", "First Blood", "Solve your first problem", "🎯", "MILESTONE", 10, "COMMON"));
        achievements.add(createAchievement("PROBLEM_SOLVER", "Problem Solver", "Solve 10 problems", "💪", "MILESTONE", 50, "COMMON"));
        achievements.add(createAchievement("CODE_WARRIOR", "Code Warrior", "Solve 25 problems", "⚔️", "MILESTONE", 100, "RARE"));
        achievements.add(createAchievement("CODE_MASTER", "Code Master", "Solve 50 problems", "👑", "MILESTONE", 250, "EPIC"));
        achievements.add(createAchievement("LEGEND", "Legend", "Solve 100 problems", "🏆", "MILESTONE", 500, "LEGENDARY"));
        
        // Speed achievements
        achievements.add(createAchievement("SPEED_DEMON", "Speed Demon", "Solve a problem in under 5 minutes", "⚡", "SPEED", 30, "RARE"));
        achievements.add(createAchievement("LIGHTNING_FAST", "Lightning Fast", "Solve 3 problems in under 10 minutes each", "🌩️", "SPEED", 75, "EPIC"));
        
        // Language achievements
        achievements.add(createAchievement("POLYGLOT", "Polyglot", "Solve problems in 3 different languages", "🌍", "LANGUAGE", 50, "RARE"));
        achievements.add(createAchievement("MASTER_OF_ALL", "Master of All", "Solve problems in all 5 languages", "🎓", "LANGUAGE", 150, "EPIC"));
        
        // Streak achievements
        achievements.add(createAchievement("CONSISTENT", "Consistent", "Solve problems for 7 days in a row", "📅", "STREAK", 100, "RARE"));
        achievements.add(createAchievement("UNSTOPPABLE", "Unstoppable", "Solve problems for 30 days in a row", "🔥", "STREAK", 300, "LEGENDARY"));
        
        achievementRepo.saveAll(achievements);
    }

    private Achievement createAchievement(String code, String name, String desc, String icon, String category, int points, String rarity) {
        Achievement a = new Achievement();
        a.setCode(code);
        a.setName(name);
        a.setDescription(desc);
        a.setIcon(icon);
        a.setCategory(category);
        a.setPoints(points);
        a.setRarity(rarity);
        return a;
    }
}
