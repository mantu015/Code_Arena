package com.codearena.service;

import com.codearena.model.Submission;
import com.codearena.model.User;
import com.codearena.repository.SubmissionRepository;
import com.codearena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Submission submitCode(Long userId, Long problemId, String code, String language, String status) {
        // Guard: skip if user doesn't exist (stale session)
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User " + userId + " not found — stale session");
        }

        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setProblemId(problemId);
        submission.setCode(code);
        submission.setLanguage(language);
        submission.setStatus(status);

        if ("Accepted".equals(status)) {
            // Only award points if this problem hasn't been accepted before
            boolean alreadySolved = submissionRepository.findByUserId(userId).stream()
                .anyMatch(s -> s.getProblemId().equals(problemId) && "Accepted".equals(s.getStatus()));
            if (!alreadySolved) {
                User user = userRepository.findById(userId).orElseThrow();
                user.setPoints(user.getPoints() + 10);
                userRepository.save(user);
            }
        }

        return submissionRepository.save(submission);
    }

    // Legacy overload kept for backward compat
    @Transactional
    public Submission submitCode(Long userId, Long problemId, String code, String language) {
        return submitCode(userId, problemId, code, language, "Accepted");
    }

    public List<Submission> getUserSubmissions(Long userId) {
        return submissionRepository.findByUserId(userId);
    }

    public List<Submission> getUserSubmissionsForProblem(Long userId, Long problemId) {
        return submissionRepository.findByUserIdAndProblemId(userId, problemId);
    }

    public long getSolvedCount(Long userId) {
        return submissionRepository.findByUserId(userId).stream()
                .filter(s -> "Accepted".equals(s.getStatus()))
                .map(Submission::getProblemId)
                .distinct()
                .count();
    }
}
