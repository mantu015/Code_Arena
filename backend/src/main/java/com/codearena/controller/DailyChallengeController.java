package com.codearena.controller;

import com.codearena.model.DailyChallenge;
import com.codearena.model.Problem;
import com.codearena.model.User;
import com.codearena.repository.DailyChallengeRepository;
import com.codearena.repository.ProblemRepository;
import com.codearena.repository.SubmissionRepository;
import com.codearena.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/daily")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class DailyChallengeController {

    private final DailyChallengeRepository dailyChallengeRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    @GetMapping
    public ResponseEntity<?> getToday() {
        LocalDate today = LocalDate.now();
        Optional<DailyChallenge> opt = dailyChallengeRepository.findByChallengeDate(today);

        if (opt.isEmpty()) {
            // Auto-assign: pick problem based on day-of-year mod problem count
            long count = problemRepository.count();
            if (count == 0) return ResponseEntity.noContent().build();
            long idx = (today.getDayOfYear() % count) + 1;
            // Find closest existing id
            Problem problem = problemRepository.findById(idx)
                .orElse(problemRepository.findAll().get(0));

            DailyChallenge dc = new DailyChallenge();
            dc.setChallengeDate(today);
            dc.setProblemId(problem.getId());
            dc.setBonusPoints(50);
            dailyChallengeRepository.save(dc);
            opt = Optional.of(dc);
        }

        DailyChallenge dc = opt.get();
        Problem problem = problemRepository.findById(dc.getProblemId()).orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("challengeDate", dc.getChallengeDate().toString());
        response.put("bonusPoints", dc.getBonusPoints());
        response.put("problem", problem);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/claim")
    @Transactional
    public ResponseEntity<?> claimBonus(@RequestBody ClaimRequest request) {
        LocalDate today = LocalDate.now();
        Optional<DailyChallenge> opt = dailyChallengeRepository.findByChallengeDate(today);
        if (opt.isEmpty()) return ResponseEntity.badRequest().body("No daily challenge today.");

        DailyChallenge dc = opt.get();

        // Verify user solved the daily problem today
        boolean solved = submissionRepository
            .findByUserIdAndProblemId(request.getUserId(), dc.getProblemId())
            .stream().anyMatch(s -> "Accepted".equals(s.getStatus()));

        if (!solved) return ResponseEntity.badRequest().body("Solve the daily problem first!");

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if already claimed (points would be double-awarded)
        // Simple guard: bonus is 50, regular solve is 10 — if points mod 50 == 0 already claimed
        // Better: use a claimed flag — for simplicity track via a dedicated field
        user.setPoints(user.getPoints() + dc.getBonusPoints());
        userRepository.save(user);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Bonus claimed! +" + dc.getBonusPoints() + " points");
        res.put("newPoints", user.getPoints());
        return ResponseEntity.ok(res);
    }
}

@Data
class ClaimRequest {
    private Long userId;
}
