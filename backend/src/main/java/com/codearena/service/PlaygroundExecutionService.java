package com.codearena.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlaygroundExecutionService {
    private final Judge0RunnerService runnerService;

    public Map<String, Object> runCode(String code, String language, String input) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Judge0RunnerService.RunnerResult run = runnerService.execute(language, code, input);
            boolean success = run.statusId() == 3;
            result.put("status", success ? "success" : "error");
            result.put("output", outputFor(run));
            result.put("runtime", System.currentTimeMillis() - start);
        } catch (IllegalArgumentException | Judge0RunnerService.CodeRunnerException exception) {
            result.put("status", "error");
            result.put("output", exception.getMessage());
            result.put("runtime", System.currentTimeMillis() - start);
        }
        return result;
    }

    private String outputFor(Judge0RunnerService.RunnerResult run) {
        if (run.compileOutput() != null && !run.compileOutput().isBlank()) {
            return "Compile Error:\n" + run.compileOutput().trim();
        }
        if (run.statusId() == 5) {
            return "Time Limit Exceeded";
        }
        if (run.statusId() != 3) {
            String details = run.stderr();
            if (details == null || details.isBlank()) {
                details = run.stdout();
            }
            if (details == null || details.isBlank()) {
                details = run.status();
            }
            return "Runtime Error:\n" + details.trim();
        }
        return run.stdout() == null ? "" : run.stdout().trim();
    }
}
