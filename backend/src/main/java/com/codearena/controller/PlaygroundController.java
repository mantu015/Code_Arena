package com.codearena.controller;

import com.codearena.service.PlaygroundExecutionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/playground")
@RequiredArgsConstructor
public class PlaygroundController {

    private final PlaygroundExecutionService playgroundService;

    @Value("${app.execution.enabled:true}")
    private boolean executionEnabled;

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runCode(@Valid @RequestBody PlaygroundRequest request) {
        if (!executionEnabled) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Code execution is disabled on this deployment."));
        }

        Map<String, Object> result = playgroundService.runCode(
            request.getCode(),
            request.getLanguage(),
            request.getInput()
        );
        return ResponseEntity.ok(result);
    }
}

@Data
class PlaygroundRequest {
    @NotBlank
    @Size(max = 30_000)
    private String code;

    @NotBlank
    @Pattern(regexp = "(?i)javascript|python|java|c|cpp|c\\+\\+")
    private String language;

    @Size(max = 10_000)
    private String input;
}
