package com.codearena.controller;

import com.codearena.model.Battle;
import com.codearena.service.BattleService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/battles")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5090", "http://localhost:5173", "http://localhost:5174"})
public class BattleController {

    private final BattleService battleService;

    @PostMapping("/create")
    public ResponseEntity<?> createBattle(@RequestBody BattleRequest request) {
        Battle battle = battleService.createBattle(request.getUserId(), request.getProblemId());
        return ResponseEntity.ok(battle);
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinBattle(@RequestBody JoinBattleRequest request) {
        Battle battle = battleService.joinBattle(request.getBattleCode(), request.getUserId());
        if (battle != null) {
            return ResponseEntity.ok(battle);
        }
        return ResponseEntity.badRequest().body("Battle not found or already started");
    }

    @PostMapping("/{id}/progress")
    public ResponseEntity<?> updateProgress(@PathVariable Long id, @RequestBody ProgressRequest request) {
        Battle battle = battleService.updateProgress(id, request.getUserId(), request.getProgress(), request.getTime());
        return ResponseEntity.ok(battle);
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableBattles() {
        return ResponseEntity.ok(battleService.getAvailableBattles());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBattles(@PathVariable Long userId) {
        return ResponseEntity.ok(battleService.getUserBattles(userId));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<?> getBattleByCode(@PathVariable String code) {
        return battleService.getBattleByCode(code)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}

@Data
class BattleRequest {
    private Long userId;
    private Long problemId;
}

@Data
class JoinBattleRequest {
    private String battleCode;
    private Long userId;
}

@Data
class ProgressRequest {
    private Long userId;
    private Integer progress;
    private Long time;
}
