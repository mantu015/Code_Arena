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
        DailyChallenge dc = dailyChallengeRepository.findByChallengeDate(today)
            .orElseGet(() -> {
                long count = problemRepository.count();
                if (count == 0) return null;
                long idx = (today.getDayOfYear() % count) + 1;
                Problem p = problemRepository.findById(idx)
                    .orElse(problemRepository.findAll().get(0));
                DailyChallenge newDc = new DailyChallenge();
                newDc.setChallengeDate(today);
                newDc.setProblemId(p.getId());
                newDc.setBonusPoints(50);
                return dailyChallengeRepository.save(newDc);
            });

        if (dc == null) return ResponseEntity.noContent().build();
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

        if (dc.hasUserClaimed(request.getUserId()))
            return ResponseEntity.badRequest().body("Bonus already claimed today!");

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPoints(user.getPoints() + dc.getBonusPoints());
        userRepository.save(user);
        dc.addClaimedUser(request.getUserId());
        dailyChallengeRepository.save(dc);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Bonus claimed! +" + dc.getBonusPoints() + " points");
        res.put("newPoints", user.getPoints());
        return ResponseEntity.ok(res);
    }

    @Data
    static class ClaimRequest {
        private Long userId;
    }
}
