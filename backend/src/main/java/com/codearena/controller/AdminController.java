package com.codearena.controller;

import com.codearena.model.Submission;
import com.codearena.model.User;
import com.codearena.repository.ProblemRepository;
import com.codearena.repository.SubmissionRepository;
import com.codearena.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AdminController {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody AdminLoginRequest req) {
        return userRepository.findByUsername(req.getUsername())
            .filter(u -> "ADMIN".equals(u.getRole()) && u.getPassword().equals(req.getPassword()))
            .map(u -> ResponseEntity.ok(Map.of("id", u.getId(), "username", u.getUsername(), "role", u.getRole())))
            .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader("X-Admin-Token") String token) {
        if (!isValidAdmin(token)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(
            userRepository.findAll(Sort.by(Sort.Direction.DESC, "points"))
                .stream().filter(u -> !"ADMIN".equals(u.getRole())).collect(Collectors.toList())
        );
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestHeader("X-Admin-Token") String token) {
        if (!isValidAdmin(token)) return ResponseEntity.status(403).build();
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        // Delete submissions first (FK constraint)
        submissionRepository.findByUserId(id).forEach(s -> submissionRepository.deleteById(s.getId()));
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    @GetMapping("/submissions")
    public ResponseEntity<List<Submission>> getAllSubmissions(@RequestHeader("X-Admin-Token") String token) {
        if (!isValidAdmin(token)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(
            submissionRepository.findAll(Sort.by(Sort.Direction.DESC, "submittedAt"))
        );
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPlatformStats(@RequestHeader("X-Admin-Token") String token) {
        if (!isValidAdmin(token)) return ResponseEntity.status(403).build();

        long totalUsers = userRepository.findAll().stream().filter(u -> !"ADMIN".equals(u.getRole())).count();
        long totalProblems = problemRepository.count();
        List<Submission> allSubs = submissionRepository.findAll();
        long totalSubmissions = allSubs.size();
        long accepted = allSubs.stream().filter(s -> "Accepted".equals(s.getStatus())).count();
        long wrongAnswer = allSubs.stream().filter(s -> "Wrong Answer".equals(s.getStatus())).count();
        long errors = allSubs.stream().filter(s -> s.getStatus().contains("Error")).count();

        // Top 5 most attempted problems
        Map<Long, Long> problemAttempts = allSubs.stream()
            .collect(Collectors.groupingBy(Submission::getProblemId, Collectors.counting()));
        List<Map<String, Object>> topProblems = problemAttempts.entrySet().stream()
            .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
            .limit(5)
            .map(e -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("problemId", e.getKey());
                m.put("attempts", e.getValue());
                problemRepository.findById(e.getKey()).ifPresent(p -> m.put("title", p.getTitle()));
                return m;
            }).collect(Collectors.toList());

        // Language distribution
        Map<String, Long> langDist = allSubs.stream()
            .collect(Collectors.groupingBy(Submission::getLanguage, Collectors.counting()));

        // Recent 10 submissions with username
        List<Map<String, Object>> recentSubs = allSubs.stream()
            .sorted(Comparator.comparing(s -> s.getSubmittedAt() != null ? s.getSubmittedAt() : java.time.LocalDateTime.MIN, Comparator.reverseOrder()))
            .limit(10)
            .map(s -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", s.getId());
                m.put("problemId", s.getProblemId());
                m.put("language", s.getLanguage());
                m.put("status", s.getStatus());
                m.put("submittedAt", s.getSubmittedAt());
                userRepository.findById(s.getUserId()).ifPresent(u -> m.put("username", u.getUsername()));
                problemRepository.findById(s.getProblemId()).ifPresent(p -> m.put("problemTitle", p.getTitle()));
                return m;
            }).collect(Collectors.toList());

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalProblems", totalProblems);
        stats.put("totalSubmissions", totalSubmissions);
        stats.put("accepted", accepted);
        stats.put("wrongAnswer", wrongAnswer);
        stats.put("errors", errors);
        stats.put("acceptanceRate", totalSubmissions > 0 ? Math.round((accepted * 100.0) / totalSubmissions) : 0);
        stats.put("topProblems", topProblems);
        stats.put("languageDistribution", langDist);
        stats.put("recentSubmissions", recentSubs);
        return ResponseEntity.ok(stats);
    }

    private boolean isValidAdmin(String token) {
        if (token == null || !token.startsWith("admin:")) return false;
        String username = token.substring(6);
        return userRepository.findByUsername(username)
            .map(u -> "ADMIN".equals(u.getRole()))
            .orElse(false);
    }
}

@Data
class AdminLoginRequest {
    private String username;
    private String password;
}
