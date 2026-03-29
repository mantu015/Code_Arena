package com.codearena.model;

import java.util.List;

public class ExecutionResult {
    private String status;        // Accepted, Wrong Answer, Runtime Error, Compile Error
    private long runtimeMs;
    private List<TestCaseResult> testResults;
    private String errorMessage;

    public ExecutionResult() {}

    public static class TestCaseResult {
        private int index;
        private String input;
        private String expected;
        private String actual;
        private boolean passed;
        private long runtimeMs;

        public TestCaseResult(int index, String input, String expected, String actual, boolean passed, long runtimeMs) {
            this.index = index;
            this.input = input;
            this.expected = expected;
            this.actual = actual;
            this.passed = passed;
            this.runtimeMs = runtimeMs;
        }

        public int getIndex() { return index; }
        public String getInput() { return input; }
        public String getExpected() { return expected; }
        public String getActual() { return actual; }
        public boolean isPassed() { return passed; }
        public long getRuntimeMs() { return runtimeMs; }
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getRuntimeMs() { return runtimeMs; }
    public void setRuntimeMs(long runtimeMs) { this.runtimeMs = runtimeMs; }
    public List<TestCaseResult> getTestResults() { return testResults; }
    public void setTestResults(List<TestCaseResult> testResults) { this.testResults = testResults; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
