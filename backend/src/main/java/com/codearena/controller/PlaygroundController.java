package com.codearena.controller;

import com.codearena.service.PlaygroundExecutionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/playground")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5090", "http://localhost:5173", "http://localhost:5174"})
public class PlaygroundController {

    private final PlaygroundExecutionService playgroundService;

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runCode(@RequestBody PlaygroundRequest request) {
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
    private String code;
    private String language;
    private String input;
}
