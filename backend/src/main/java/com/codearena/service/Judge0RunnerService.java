package com.codearena.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

@Service
public class Judge0RunnerService {
    private static final int MAX_SOURCE_LENGTH = 30_000;
    private static final int MAX_STDIN_LENGTH = 10_000;
    private static final Map<String, Integer> LANGUAGE_IDS = Map.of(
            "javascript", 97,
            "python", 92,
            "java", 62,
            "c", 103,
            "cpp", 105,
            "c++", 105
    );

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public Judge0RunnerService(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.code-runner.url:https://ce.judge0.com}") String runnerUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(40));
        this.restClient = restClientBuilder
                .baseUrl(runnerUrl)
                .requestFactory(requestFactory)
                .build();
        this.objectMapper = objectMapper;
    }

    public RunnerResult execute(String language, String source, String stdin) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Write some code before running it.");
        }
        if (source.length() > MAX_SOURCE_LENGTH) {
            throw new IllegalArgumentException("Code is too large to run (maximum 30,000 characters).");
        }
        if (stdin != null && stdin.length() > MAX_STDIN_LENGTH) {
            throw new IllegalArgumentException("Input is too large to run (maximum 10,000 characters).");
        }

        Integer languageId = LANGUAGE_IDS.get(language == null ? "" : language.trim().toLowerCase());
        if (languageId == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        Map<String, Object> request = Map.of(
                "source_code", source,
                "language_id", languageId,
                "stdin", stdin == null ? "" : stdin,
                "cpu_time_limit", 3.0,
                "cpu_extra_time", 1.0,
                "wall_time_limit", 8.0,
                "memory_limit", 128_000,
                "max_processes_and_or_threads", 20,
                "max_file_size", 1_024,
                "enable_network", false
        );

        try {
            SubmissionResponse response = restClient.post()
                    .uri("/submissions?base64_encoded=false&wait=true")
                    .body(request)
                    .retrieve()
                    .body(SubmissionResponse.class);
            if (response == null || response.status() == null) {
                throw new CodeRunnerException("The code runner returned an empty response.");
            }
            return new RunnerResult(
                    response.status().id(),
                    response.status().description(),
                    response.stdout(),
                    response.stderr(),
                    response.compileOutput(),
                    response.time() == null ? 0 : Math.round(response.time() * 1_000)
            );
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 429) {
                throw new CodeRunnerException("The code runner is busy. Wait a moment, then try again.", exception);
            }
            throw new CodeRunnerException(
                    "The code runner could not process this request (HTTP "
                            + exception.getStatusCode().value() + "). Try again shortly.",
                    exception);
        } catch (RestClientException exception) {
            throw new CodeRunnerException("Could not reach the secure code runner. Try again shortly.", exception);
        }
    }

    public String encodeJavaScriptString(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not prepare the JavaScript test harness.", exception);
        }
    }

    public record RunnerResult(
            int statusId,
            String status,
            String stdout,
            String stderr,
            String compileOutput,
            long runtimeMs) {
    }

    private record SubmissionResponse(
            String stdout,
            String stderr,
            @JsonProperty("compile_output") String compileOutput,
            Double time,
            RunnerStatus status) {
    }

    private record RunnerStatus(int id, String description) {
    }

    public static class CodeRunnerException extends RuntimeException {
        public CodeRunnerException(String message) {
            super(message);
        }

        public CodeRunnerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
