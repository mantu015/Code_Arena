package com.codearena.service;

import com.codearena.model.User;
import com.codearena.repository.SubmissionRepository;
import com.codearena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    public User registerUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setPoints(0);
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid credentials");
        }
        return user;
    }

    public List<User> getTopUsers(int limit) {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "points"))
                .stream()
                .filter(u -> !"ADMIN".equals(u.getRole()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Map<String, Object> getUserStats(Long userId) {
        var submissions = submissionRepository.findByUserId(userId);
        long totalSubmissions = submissions.size();
        long accepted = submissions.stream().filter(s -> "Accepted".equals(s.getStatus())).count();
        long wrongAnswer = submissions.stream().filter(s -> "Wrong Answer".equals(s.getStatus())).count();
        long runtimeError = submissions.stream().filter(s -> s.getStatus().contains("Error")).count();

        // Solved problems by unique problemId with Accepted status
        Set<Long> solvedIds = submissions.stream()
            .filter(s -> "Accepted".equals(s.getStatus()))
            .map(s -> s.getProblemId())
            .collect(Collectors.toSet());

        // Recent 10 submissions
        var recent = submissions.stream()
            .sorted(Comparator.comparing(s -> s.getSubmittedAt() != null ? s.getSubmittedAt() : java.time.LocalDateTime.MIN, Comparator.reverseOrder()))
            .limit(10)
            .collect(Collectors.toList());

        // Rank among all users
        List<User> ranked = userRepository.findAll(Sort.by(Sort.Direction.DESC, "points"))
                .stream().filter(u -> !"ADMIN".equals(u.getRole())).collect(Collectors.toList());
        int rank = 1;
        User target = userRepository.findById(userId).orElse(null);
        if (target != null) {
            for (User u : ranked) {
                if (u.getId().equals(userId)) break;
                rank++;
            }
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSubmissions", totalSubmissions);
        stats.put("accepted", accepted);
        stats.put("wrongAnswer", wrongAnswer);
        stats.put("runtimeError", runtimeError);
        stats.put("solvedCount", solvedIds.size());
        stats.put("rank", rank);
        stats.put("recentSubmissions", recent);
        return stats;
    }
}
