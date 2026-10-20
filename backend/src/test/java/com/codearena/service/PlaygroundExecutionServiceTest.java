package com.codearena.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PlaygroundExecutionServiceTest {
    @Test
    void runsPlaygroundCodeThroughTheSandboxAndReturnsOutput() {
        Judge0RunnerService runnerService = mock(Judge0RunnerService.class);
        when(runnerService.execute("cpp", "int main() {}", "input"))
                .thenReturn(new Judge0RunnerService.RunnerResult(3, "Accepted", "done\n", null, null, 2));
        PlaygroundExecutionService service = new PlaygroundExecutionService(runnerService);

        var result = service.runCode("int main() {}", "cpp", "input");

        assertEquals("success", result.get("status"));
        assertEquals("done", result.get("output"));
        verify(runnerService).execute("cpp", "int main() {}", "input");
    }

    @Test
    void reportsRunnerErrorsToThePlayground() {
        Judge0RunnerService runnerService = mock(Judge0RunnerService.class);
        when(runnerService.execute("python", "print(1)", ""))
                .thenThrow(new Judge0RunnerService.CodeRunnerException("Runner unavailable"));
        PlaygroundExecutionService service = new PlaygroundExecutionService(runnerService);

        var result = service.runCode("print(1)", "python", "");

        assertEquals("error", result.get("status"));
        assertEquals("Runner unavailable", result.get("output"));
    }
}
