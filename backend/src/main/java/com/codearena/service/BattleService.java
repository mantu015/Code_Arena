package com.codearena.service;

import com.codearena.model.Battle;
import com.codearena.repository.BattleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BattleService {
    
    private final BattleRepository battleRepo;

    public Battle createBattle(Long userId, Long problemId) {
        Battle battle = new Battle();
        battle.setPlayer1Id(userId);
        battle.setProblemId(problemId);
        battle.setStatus("WAITING");
        battle.setBattleCode(generateBattleCode());
        return battleRepo.save(battle);
    }

    public Battle joinBattle(String battleCode, Long userId) {
        Optional<Battle> battle = battleRepo.findByBattleCode(battleCode);
        if (battle.isPresent() && battle.get().getStatus().equals("WAITING")) {
            Battle b = battle.get();
            b.setPlayer2Id(userId);
            b.setStatus("IN_PROGRESS");
            b.setStartedAt(LocalDateTime.now());
            return battleRepo.save(b);
        }
        return null;
    }

    public Battle updateProgress(Long battleId, Long userId, int progress, Long time) {
        Optional<Battle> battle = battleRepo.findById(battleId);
        if (battle.isPresent()) {
            Battle b = battle.get();
            if (b.getPlayer1Id().equals(userId)) {
                b.setPlayer1Progress(progress);
                b.setPlayer1Time(time);
            } else if (b.getPlayer2Id().equals(userId)) {
                b.setPlayer2Progress(progress);
                b.setPlayer2Time(time);
            }
            
            // Check if battle is complete
            if (progress == 100) {
                b.setWinnerId(userId);
                b.setStatus("COMPLETED");
                b.setCompletedAt(LocalDateTime.now());
            }
            
            return battleRepo.save(b);
        }
        return null;
    }

    public List<Battle> getAvailableBattles() {
        return battleRepo.findAvailableBattles();
    }

    public List<Battle> getUserBattles(Long userId) {
        return battleRepo.findUserBattles(userId);
    }

    public Optional<Battle> getBattleByCode(String code) {
        return battleRepo.findByBattleCode(code);
    }

    private String generateBattleCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
