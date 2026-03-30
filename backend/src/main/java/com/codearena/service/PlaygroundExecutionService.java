package com.codearena.service;

import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class PlaygroundExecutionService {

    public Map<String, Object> runCode(String code, String language, String input) {
        long start = System.currentTimeMillis();
        Map<String, Object> result;
        
        try {
            result = switch (language.toLowerCase()) {
                case "javascript" -> executeJS(code, input);
                case "python" -> executePython(code, input);
                case "java" -> executeJava(code, input);
                case "c" -> executeC(code, input);
                case "cpp", "c++" -> executeCpp(code, input);
                default -> Map.of("status", "error", "output", "Unsupported language: " + language);
            };
        } catch (Exception e) {
            result = new HashMap<>();
            result.put("status", "error");
            result.put("output", "Execution failed: " + e.getMessage());
        }
        
        result.put("runtime", System.currentTimeMillis() - start);
        return result;
    }

    private Map<String, Object> executeJS(String code, String input) {
        Map<String, Object> result = new HashMap<>();
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        
        if (engine == null) {
            result.put("status", "error");
            result.put("output", "JavaScript engine not available");
            return result;
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream old = System.out;
            System.setOut(ps);
            
            engine.put("input", input);
            engine.eval("var console = { log: function(x) { print(x); } };");
            engine.eval(code);
            
            System.out.flush();
            System.setOut(old);
            
            result.put("status", "success");
            result.put("output", baos.toString().trim());
        } catch (Exception e) {
            result.put("status", "error");
            result.put("output", "Runtime Error: " + e.getMessage());
        }
        
        return result;
    }

    private Map<String, Object> executePython(String code, String input) {
        Map<String, Object> result = new HashMap<>();
        Path tempDir = null;
        
        try {
            tempDir = Files.createTempDirectory("playground_py_");
            Path scriptFile = tempDir.resolve("script.py");
            Files.writeString(scriptFile, code);
            
            ProcessResult pr = runProcess(tempDir, List.of(findPython(), scriptFile.toString()), 10, input);
            
            if (pr.exitCode != 0 && !pr.stderr.isBlank()) {
                result.put("status", "error");
                result.put("output", pr.stderr.trim());
            } else {
                result.put("status", "success");
                result.put("output", pr.stdout.trim());
            }
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("output", "Execution error: " + e.getMessage());
        } finally {
            if (tempDir != null) deleteTempDir(tempDir);
        }
        
        return result;
    }

    private Map<String, Object> executeJava(String code, String input) {
        Map<String, Object> result = new HashMap<>();
        Path tempDir = null;
        
        try {
            tempDir = Files.createTempDirectory("playground_java_");
            
            String className = extractClassName(code);
            if (className == null) className = "Main";
            
            Path javaFile = tempDir.resolve(className + ".java");
            Files.writeString(javaFile, code);
            
            ProcessResult compile = runProcess(tempDir, List.of("javac", javaFile.toString()), 15, null);
            if (compile.exitCode != 0) {
                result.put("status", "error");
                result.put("output", "Compile Error:\n" + compile.stderr);
                return result;
            }
            
            ProcessResult run = runProcess(tempDir, List.of("java", "-cp", tempDir.toString(), className), 10, input);
            
            if (run.exitCode != 0) {
                result.put("status", "error");
                result.put("output", "Runtime Error:\n" + (run.stderr.isBlank() ? run.stdout : run.stderr));
            } else {
                result.put("status", "success");
                result.put("output", run.stdout.trim());
            }
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("output", "Execution error: " + e.getMessage());
        } finally {
            if (tempDir != null) deleteTempDir(tempDir);
        }
        
        return result;
    }

    private Map<String, Object> executeC(String code, String input) {
        return executeCFamily(code, input, "gcc", "c");
    }

    private Map<String, Object> executeCpp(String code, String input) {
        return executeCFamily(code, input, "g++", "cpp");
    }

    private Map<String, Object> executeCFamily(String code, String input, String compiler, String ext) {
        Map<String, Object> result = new HashMap<>();
        Path tempDir = null;
        
        try {
            tempDir = Files.createTempDirectory("playground_" + ext + "_");
            Path sourceFile = tempDir.resolve("program." + ext);
            Files.writeString(sourceFile, code);
            
            boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
            String exeName = isWindows ? "a.exe" : "a.out";
            Path exePath = tempDir.resolve(exeName);
            
            ProcessResult compile = runProcess(tempDir, List.of(compiler, sourceFile.toString(), "-o", exePath.toString()), 15, null);
            if (compile.exitCode != 0) {
                result.put("status", "error");
                result.put("output", "Compile Error:\n" + compile.stderr);
                return result;
            }
            
            ProcessResult run = runProcess(tempDir, List.of(exePath.toString()), 10, input);
            
            if (run.exitCode != 0 && !run.stderr.isBlank()) {
                result.put("status", "error");
                result.put("output", "Runtime Error:\n" + run.stderr);
            } else {
                result.put("status", "success");
                result.put("output", run.stdout.trim());
            }
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("output", "Execution error: " + e.getMessage());
        } finally {
            if (tempDir != null) deleteTempDir(tempDir);
        }
        
        return result;
    }

    private String extractClassName(String code) {
        int idx = code.indexOf("class ");
        if (idx == -1) return null;
        int start = idx + 6;
        int end = start;
        while (end < code.length() && (Character.isLetterOrDigit(code.charAt(end)) || code.charAt(end) == '_')) {
            end++;
        }
        return end > start ? code.substring(start, end) : null;
    }

    private ProcessResult runProcess(Path workDir, List<String> cmd, int timeoutSec, String input) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(workDir.toFile());
            Process proc = pb.start();

            if (input != null && !input.isBlank()) {
                try (OutputStream os = proc.getOutputStream()) {
                    os.write(input.getBytes());
                    os.flush();
                }
            }

            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();
            Thread outT = new Thread(() -> readStream(proc.getInputStream(), stdout));
            Thread errT = new Thread(() -> readStream(proc.getErrorStream(), stderr));
            outT.start();
            errT.start();

            boolean finished = proc.waitFor(timeoutSec, TimeUnit.SECONDS);
            if (!finished) {
                proc.destroyForcibly();
                return new ProcessResult(-1, "", "Time Limit Exceeded (" + timeoutSec + "s)");
            }
            
            outT.join(1000);
            errT.join(1000);
            return new ProcessResult(proc.exitValue(), stdout.toString(), stderr.toString());
        } catch (Exception e) {
            return new ProcessResult(-1, "", "Process error: " + e.getMessage());
        }
    }

    private void readStream(InputStream is, StringBuilder sb) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append("\n");
        } catch (IOException ignored) {}
    }

    private String findPython() {
        return System.getProperty("os.name", "").toLowerCase().contains("win") ? "python" : "python3";
    }

    private void deleteTempDir(Path dir) {
        try {
            Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException ignored) {}
    }

    record ProcessResult(int exitCode, String stdout, String stderr) {}
}
