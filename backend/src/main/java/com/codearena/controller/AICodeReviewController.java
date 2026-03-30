package com.codearena.controller;

import com.codearena.service.AICodeReviewService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-review")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5090", "http://localhost:5173", "http://localhost:5174"})
public class AICodeReviewController {

    private final AICodeReviewService aiReviewService;

    @PostMapping
    public ResponseEntity<?> reviewCode(@RequestBody CodeReviewRequest request) {
        return ResponseEntity.ok(aiReviewService.analyzeCode(
            request.getCode(),
            request.getLanguage(),
            request.getTestResults()
        ));
    }
}

@Data
class CodeReviewRequest {
    private String code;
    private String language;
    private String testResults;
}
