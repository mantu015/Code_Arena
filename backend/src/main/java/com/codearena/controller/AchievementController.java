package com.codearena.controller;

import com.codearena.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5090", "http://localhost:5173", "http://localhost:5174"})
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public ResponseEntity<?> getAllAchievements() {
        return ResponseEntity.ok(achievementService.getAllAchievements());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserAchievements(@PathVariable Long userId) {
        return ResponseEntity.ok(achievementService.getUserAchievements(userId));
    }

    @PostMapping("/check/{userId}")
    public ResponseEntity<?> checkAchievements(@PathVariable Long userId) {
        achievementService.checkAndUnlockAchievements(userId);
        return ResponseEntity.ok(achievementService.getUserAchievements(userId));
    }
}
