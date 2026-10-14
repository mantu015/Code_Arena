package com.codearena.controller;

import com.codearena.model.ExecutionResult;
import com.codearena.model.Submission;
import com.codearena.service.CodeExecutionService;
import com.codearena.service.SubmissionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/execute")
@RequiredArgsConstructor
public class ExecutionController {

    private final CodeExecutionService executionService;
    private final SubmissionService submissionService;

    @Value("${app.execution.enabled:true}")
    private boolean executionEnabled;

    @PostMapping
    public ResponseEntity<?> execute(@RequestBody ExecuteRequest request) {
        if (!executionEnabled) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Code execution is disabled on this deployment."));
        }

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
