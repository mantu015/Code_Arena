package com.codearena.service;

import com.codearena.model.ExecutionResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CodeExecutionServiceTest {
    private final Judge0RunnerService runnerService = mock(Judge0RunnerService.class);
    private final CodeExecutionService service = new CodeExecutionService(runnerService);

    @Test
    void runsJavascriptTestHarnessesInSandbox() {
        when(runnerService.encodeJavaScriptString(anyString())).thenAnswer(invocation ->
                "\"" + invocation.<String>getArgument(0).replace("\"", "\\\"") + "\"");
        when(runnerService.execute(eq("javascript"), anyString(), eq("")))
                .thenReturn(accepted("[0,1]"), accepted("[1,2]"), accepted("[0,1]"));

        ExecutionResult result = service.execute(1L,
                "function twoSum(nums, target) { return nums[0] === 2 ? [0, 1] : [1, 2]; }",
                "javascript");

        assertEquals("Accepted", result.getStatus());
        assertEquals(3, result.getTestResults().size());
        verify(runnerService, times(3)).execute(eq("javascript"), contains("eval("), eq(""));
    }

    @Test
    void turnsRunnerFailuresIntoVisibleExecutionErrors() {
        when(runnerService.encodeJavaScriptString(anyString())).thenReturn("\"test\"");
        when(runnerService.execute(eq("javascript"), anyString(), eq("")))
                .thenThrow(new Judge0RunnerService.CodeRunnerException("Runner unavailable"));

        ExecutionResult result = service.execute(1L, "function twoSum() {}", "javascript");

        assertEquals("Runner Error", result.getStatus());
        assertEquals("Runner unavailable", result.getErrorMessage());
    }

    @Test
    void preparesCppHarnessesForProblemInputs() {
        when(runnerService.execute(eq("cpp"), anyString(), eq("")))
                .thenReturn(accepted("2"), accepted("3"), accepted("8"));

        ExecutionResult result = service.execute(10L,
                "class Solution { public: int climbStairs(int n) { return n; } };",
                "cpp");

        assertEquals("Accepted", result.getStatus());
        ArgumentCaptor<String> sources = ArgumentCaptor.forClass(String.class);
        verify(runnerService, times(3)).execute(eq("cpp"), sources.capture(), eq(""));
        assertTrue(sources.getAllValues().get(0).contains("climbStairs(2)"));
        assertTrue(sources.getAllValues().get(1).contains("climbStairs(3)"));
        assertTrue(sources.getAllValues().get(2).contains("climbStairs(5)"));
    }

    private Judge0RunnerService.RunnerResult accepted(String stdout) {
        return new Judge0RunnerService.RunnerResult(3, "Accepted", stdout, null, null, 1);
    }
}
