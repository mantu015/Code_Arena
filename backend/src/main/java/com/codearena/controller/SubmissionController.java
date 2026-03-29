package com.codearena.controller;

import com.codearena.model.Submission;
import com.codearena.service.SubmissionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<Submission> submitCode(@RequestBody SubmitRequest request) {
        Submission submission = submissionService.submitCode(
                request.getUserId(),
                request.getProblemId(),
                request.getCode(),
                request.getLanguage()
        );
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/user/{userId}")
    public List<Submission> getUserSubmissions(@PathVariable Long userId) {
        return submissionService.getUserSubmissions(userId);
    }

    @GetMapping("/user/{userId}/solved")
    public ResponseEntity<Long> getSolvedCount(@PathVariable Long userId) {
        return ResponseEntity.ok(submissionService.getSolvedCount(userId));
    }

    @GetMapping("/user/{userId}/problem/{problemId}")
    public List<Submission> getSubmissionsForProblem(@PathVariable Long userId, @PathVariable Long problemId) {
        return submissionService.getUserSubmissionsForProblem(userId, problemId);
    }
}

@Data
class SubmitRequest {
    private Long userId;
    private Long problemId;
    private String code;
    private String language;
}
