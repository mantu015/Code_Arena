package com.codearena.controller;

import com.codearena.model.ExecutionResult;
import com.codearena.model.Submission;
import com.codearena.service.CodeExecutionService;
import com.codearena.service.SubmissionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/execute")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5090", "http://localhost:5173", "http://localhost:5174"})
public class ExecutionController {

    private final CodeExecutionService executionService;
    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<ExecutionResult> execute(@RequestBody ExecuteRequest request) {
        ExecutionResult result = executionService.execute(
            request.getProblemId(),
            request.getCode(),
            request.getLanguage()
        );

        // Persist submission only if user actually exists in DB
        if (request.getUserId() != null && request.getUserId() > 0) {
            try {
                submissionService.submitCode(
                    request.getUserId(),
                    request.getProblemId(),
                    request.getCode(),
                    request.getLanguage(),
                    result.getStatus()
                );
            } catch (Exception ignored) {
                // User ID from stale localStorage session — execution result still returned
            }
        }

        return ResponseEntity.ok(result);
    }
}

@Data
class ExecuteRequest {
    private Long userId;
    private Long problemId;
    private String code;
    private String language;
}
