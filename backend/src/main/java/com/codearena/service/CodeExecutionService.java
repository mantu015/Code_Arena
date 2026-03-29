package com.codearena.service;

import com.codearena.model.ExecutionResult;
import com.codearena.model.ExecutionResult.TestCaseResult;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

@Service
public class CodeExecutionService {

    private static final Map<Long, List<TC>> TESTS = new LinkedHashMap<>();

    record TC(String input, String expected,
              String jsHarness,
              String pyDriver,
              String javaDriver) {}

    // Wraps a Python driver call in try/except so runtime errors print to stdout
    // Supports both function-style (twoSum(...)) and class-style (Solution().twoSum(...))
    private static String pyCall(String call) {
        return "\nfrom typing import List, Optional\n" +
               "\ntry:\n" +
               "    _sol = Solution()\n" +
               "    # bind top-level names to instance methods for driver compatibility\n" +
               "    import sys as _sys\n" +
               "    _mod = _sys.modules[__name__]\n" +
               "    for _name in [m for m in dir(_sol) if not m.startswith('_')]:\n" +
               "        setattr(_mod, _name, getattr(_sol, _name))\n" +
               "except Exception:\n" +
               "    pass  # user wrote plain functions, not a class\n" +
               "\ntry:\n    " + call + "\nexcept Exception as _e:\n    print('Runtime Error: ' + str(_e))\n";
    }

    static {
        // ── Problem 1: Two Sum ───────────────────────────────────────────────
        TESTS.put(1L, List.of(
            new TC("nums=[2,7,11,15], target=9", "[0,1]",
                "JSON.stringify(twoSum([2,7,11,15],9).slice().sort((a,b)=>a-b))",
                pyCall("r=twoSum([2,7,11,15],9);r.sort();print(r)"),
                javaWrap(1, "int[] r=new Solution().twoSum(new int[]{2,7,11,15},9);Arrays.sort(r);System.out.println(Arrays.toString(r).replace(\", \",\",\"));")),
            new TC("nums=[3,2,4], target=6", "[1,2]",
                "JSON.stringify(twoSum([3,2,4],6).slice().sort((a,b)=>a-b))",
                pyCall("r=twoSum([3,2,4],6);r.sort();print(r)"),
                javaWrap(1, "int[] r=new Solution().twoSum(new int[]{3,2,4},6);Arrays.sort(r);System.out.println(Arrays.toString(r).replace(\", \",\",\"));")),
            new TC("nums=[3,3], target=6", "[0,1]",
                "JSON.stringify(twoSum([3,3],6).slice().sort((a,b)=>a-b))",
                pyCall("r=twoSum([3,3],6);r.sort();print(r)"),
                javaWrap(1, "int[] r=new Solution().twoSum(new int[]{3,3},6);Arrays.sort(r);System.out.println(Arrays.toString(r).replace(\", \",\",\"));"))
        ));

        // ── Problem 2: Add Two Numbers ───────────────────────────────────────
        String jsListHelper =
            "function _ml(a){let h={val:a[0],next:null},c=h;for(let i=1;i<a.length;i++){c.next={val:a[i],next:null};c=c.next;}return h;}" +
            "function _ta(n){let r=[];while(n){r.push(n.val);n=n.next;}return r;}";
        String pyListHelper =
            "\nclass ListNode:\n    def __init__(self,val=0,next=None):\n        self.val=val;self.next=next\n" +
            "def _ml(a):\n    h=ListNode(a[0]);c=h\n    for v in a[1:]:c.next=ListNode(v);c=c.next\n    return h\n" +
            "def _ta(n):\n    r=[]\n    while n:r.append(n.val);n=n.next\n    return r\n";
        TESTS.put(2L, List.of(
            new TC("l1=[2,4,3], l2=[5,6,4]", "[7,0,8]",
                jsListHelper + "JSON.stringify(_ta(addTwoNumbers(_ml([2,4,3]),_ml([5,6,4]))))",
                pyListHelper + pyCall("print(_ta(addTwoNumbers(_ml([2,4,3]),_ml([5,6,4]))))"),
                javaWrap(2, "System.out.println(Arrays.toString(toArr(new Solution().addTwoNumbers(makeList(new int[]{2,4,3}),makeList(new int[]{5,6,4})))).replace(\", \",\",\"));")),
            new TC("l1=[0], l2=[0]", "[0]",
                jsListHelper + "JSON.stringify(_ta(addTwoNumbers(_ml([0]),_ml([0]))))",
                pyListHelper + pyCall("print(_ta(addTwoNumbers(_ml([0]),_ml([0]))))"),
                javaWrap(2, "System.out.println(Arrays.toString(toArr(new Solution().addTwoNumbers(makeList(new int[]{0}),makeList(new int[]{0})))).replace(\", \",\",\"));"))
        ));

        // ── Problem 3: Longest Substring ─────────────────────────────────────
        TESTS.put(3L, List.of(
            new TC("s=\"abcabcbb\"", "3",
                "String(lengthOfLongestSubstring('abcabcbb'))",
                pyCall("print(lengthOfLongestSubstring('abcabcbb'))"),
                javaWrap(3, "System.out.println(new Solution().lengthOfLongestSubstring(\"abcabcbb\"));")),
            new TC("s=\"bbbbb\"", "1",
                "String(lengthOfLongestSubstring('bbbbb'))",
                pyCall("print(lengthOfLongestSubstring('bbbbb'))"),
                javaWrap(3, "System.out.println(new Solution().lengthOfLongestSubstring(\"bbbbb\"));")),
            new TC("s=\"pwwkew\"", "3",
                "String(lengthOfLongestSubstring('pwwkew'))",
                pyCall("print(lengthOfLongestSubstring('pwwkew'))"),
                javaWrap(3, "System.out.println(new Solution().lengthOfLongestSubstring(\"pwwkew\"));"))
        ));

        // ── Problem 4: Median of Two Sorted Arrays ───────────────────────────
        TESTS.put(4L, List.of(
            new TC("nums1=[1,3], nums2=[2]", "2.0",
                "String(findMedianSortedArrays([1,3],[2]))",
                pyCall("print(float(findMedianSortedArrays([1,3],[2])))"),
                javaWrap(4, "System.out.println(new Solution().findMedianSortedArrays(new int[]{1,3},new int[]{2}));")),
            new TC("nums1=[1,2], nums2=[3,4]", "2.5",
                "String(findMedianSortedArrays([1,2],[3,4]))",
                pyCall("print(float(findMedianSortedArrays([1,2],[3,4])))"),
                javaWrap(4, "System.out.println(new Solution().findMedianSortedArrays(new int[]{1,2},new int[]{3,4}));"))
        ));

        // ── Problem 5: 3Sum ──────────────────────────────────────────────────
        TESTS.put(5L, List.of(
            new TC("nums=[-1,0,1,2,-1,-4]", "[[-1,-1,2],[-1,0,1]]",
                "var r=threeSum([-1,0,1,2,-1,-4]);r.forEach(t=>t.sort((a,b)=>a-b));r.sort((a,b)=>a[0]-b[0]||a[1]-b[1]);JSON.stringify(r)",
                pyCall("r=threeSum([-1,0,1,2,-1,-4]);r=[sorted(t) for t in r];r.sort();print(r)"),
                javaWrap(5, "List<List<Integer>> r=new Solution().threeSum(new int[]{-1,0,1,2,-1,-4});r.forEach(Collections::sort);r.sort(Comparator.comparingInt(a->a.get(0)));System.out.println(r.toString().replace(\", \",\",\"));")),
            new TC("nums=[0,0,0]", "[[0,0,0]]",
                "JSON.stringify(threeSum([0,0,0]))",
                pyCall("print(threeSum([0,0,0]))"),
                javaWrap(5, "System.out.println(new Solution().threeSum(new int[]{0,0,0}).toString().replace(\", \",\",\"));"))
        ));

        // ── Problem 6: Regular Expression Matching ───────────────────────────
        TESTS.put(6L, List.of(
            new TC("s=\"aa\", p=\"a\"",  "false",
                "String(isMatch('aa','a'))",
                pyCall("print(str(isMatch('aa','a')).lower())"),
                javaWrap(6, "System.out.println(new Solution().isMatch(\"aa\",\"a\"));")),
            new TC("s=\"aa\", p=\"a*\"", "true",
                "String(isMatch('aa','a*'))",
                pyCall("print(str(isMatch('aa','a*')).lower())"),
                javaWrap(6, "System.out.println(new Solution().isMatch(\"aa\",\"a*\"));")),
            new TC("s=\"ab\", p=\".*\"", "true",
                "String(isMatch('ab','.*'))",
                pyCall("print(str(isMatch('ab','.*')).lower())"),
                javaWrap(6, "System.out.println(new Solution().isMatch(\"ab\",\".*\"));"))
        ));
    }

    private static String javaWrap(int problemId, String callCode) {
        String listNodeHelper = "";
        if (problemId == 2) {
            listNodeHelper =
                "    static class ListNode { int val; ListNode next; ListNode(int v){val=v;} }\n" +
                "    static ListNode makeList(int[] a){\n" +
                "        ListNode h=new ListNode(a[0]),c=h;\n" +
                "        for(int i=1;i<a.length;i++){c.next=new ListNode(a[i]);c=c.next;}\n" +
                "        return h;}\n" +
                "    static int[] toArr(ListNode n){\n" +
                "        List<Integer> r=new ArrayList<>();\n" +
                "        while(n!=null){r.add(n.val);n=n.next;}\n" +
                "        return r.stream().mapToInt(Integer::intValue).toArray();}\n";
        }
        return "import java.util.*;\n" +
               "public class Main {\n" +
               listNodeHelper +
               "    {{USER_CODE}}\n" +
               "    public static void main(String[] args) throws Exception {\n" +
               "        " + callCode + "\n" +
               "    }\n" +
               "}\n";
    }

    // ── Public entry point ───────────────────────────────────────────────────
    public ExecutionResult execute(Long problemId, String code, String language) {
        List<TC> tests = TESTS.getOrDefault(problemId, List.of());
        if (tests.isEmpty()) {
            ExecutionResult r = new ExecutionResult();
            r.setStatus("No Test Cases");
            r.setTestResults(List.of());
            return r;
        }
        return switch (language.toLowerCase()) {
            case "javascript" -> executeJS(tests, code);
            case "python"     -> executePython(tests, code);
            case "java"       -> executeJava(tests, code);
            case "cpp"        -> notSupported(tests, "C++");
            default           -> notSupported(tests, language);
        };
    }

    // ── JavaScript via Nashorn ───────────────────────────────────────────────
    private ExecutionResult executeJS(List<TC> tests, String code) {
        ExecutionResult result = new ExecutionResult();
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        if (engine == null) return notSupported(tests, "JavaScript (Nashorn unavailable)");

        List<TestCaseResult> tcResults = new ArrayList<>();
        long totalRuntime = 0;
        boolean allPassed = true;

        for (int i = 0; i < tests.size(); i++) {
            TC tc = tests.get(i);
            long start = System.currentTimeMillis();
            try {
                ScriptEngine fresh = manager.getEngineByName("JavaScript");
                fresh.eval(code);
                Object raw = fresh.eval(tc.jsHarness());
                long elapsed = System.currentTimeMillis() - start;
                totalRuntime += elapsed;
                String actual = raw == null ? "null" : raw.toString();
                boolean passed = normalize(actual).equals(normalize(tc.expected()));
                tcResults.add(new TestCaseResult(i + 1, tc.input(), tc.expected(), actual, passed, elapsed));
                if (!passed) allPassed = false;
            } catch (ScriptException e) {
                long elapsed = System.currentTimeMillis() - start;
                String msg = e.getMessage() != null ? e.getMessage().split("\n")[0] : "Script error";
                tcResults.add(new TestCaseResult(i + 1, tc.input(), tc.expected(), "Runtime Error: " + msg, false, elapsed));
                allPassed = false;
                break;
            }
        }

        result.setTestResults(tcResults);
        result.setRuntimeMs(totalRuntime);
        result.setStatus(deriveStatus(allPassed, tcResults));
        return result;
    }

    // ── Python via ProcessBuilder ────────────────────────────────────────────
    // Each test case runs in its own process. Errors are caught inside the Python
    // script via try/except (pyCall wrapper) and printed to stdout — so we only
    // need to check stdout for "Runtime Error:" prefix, never stderr.
    private ExecutionResult executePython(List<TC> tests, String userCode) {
        ExecutionResult result = new ExecutionResult();
        List<TestCaseResult> tcResults = new ArrayList<>();
        long totalRuntime = 0;
        boolean allPassed = true;

        Path tempDir;
        try { tempDir = Files.createTempDirectory("codearena_py_"); }
        catch (IOException e) { return ioError(e); }

        try {
            String python = findPython();
            for (int i = 0; i < tests.size(); i++) {
                TC tc = tests.get(i);
                // Write user code + driver into a single .py file
                Path file = tempDir.resolve("sol_" + i + ".py");
                try { Files.writeString(file, userCode + "\n" + tc.pyDriver()); }
                catch (IOException e) { return ioError(e); }

                long start = System.currentTimeMillis();
                ProcessResult pr = runProcess(tempDir, List.of(python, file.toString()), 10);
                long elapsed = System.currentTimeMillis() - start;
                totalRuntime += elapsed;

                // Python syntax errors go to stderr with exit code 1
                if (pr.exitCode != 0 && !pr.stderr.isBlank()) {
                    String err = firstLines(pr.stderr, 3);
                    tcResults.add(new TestCaseResult(i + 1, tc.input(), tc.expected(),
                        "Syntax Error: " + err, false, elapsed));
                    allPassed = false;
                    break;
                }

                String actual = pr.stdout.trim();
                // pyCall wrapper prints "Runtime Error: ..." to stdout on exception
                if (actual.startsWith("Runtime Error:")) {
                    tcResults.add(new TestCaseResult(i + 1, tc.input(), tc.expected(),
                        actual, false, elapsed));
                    allPassed = false;
                    break;
                }

                actual = actual.replaceAll(",\\s+", ",");
                boolean passed = normalize(actual).equals(normalize(tc.expected()));
                tcResults.add(new TestCaseResult(i + 1, tc.input(), tc.expected(), actual, passed, elapsed));
                if (!passed) allPassed = false;
            }
        } finally {
            deleteTempDir(tempDir);
        }

        result.setTestResults(tcResults);
        result.setRuntimeMs(totalRuntime);
        result.setStatus(deriveStatus(allPassed, tcResults));
        return result;
    }

    // ── Java via ProcessBuilder ──────────────────────────────────────────────
    // Compile once per submission (all TCs share the same class), run per TC.
    private ExecutionResult executeJava(List<TC> tests, String userCode) {
        ExecutionResult result = new ExecutionResult();
        List<TestCaseResult> tcResults = new ArrayList<>();
        long totalRuntime = 0;
        boolean allPassed = true;

        Path tempDir;
        try { tempDir = Files.createTempDirectory("codearena_java_"); }
        catch (IOException e) { return ioError(e); }

        try {
            for (int i = 0; i < tests.size(); i++) {
                TC tc = tests.get(i);
                // Build full source for this test case
                String solutionBody = extractSolutionBody(userCode);
                String fullSource = tc.javaDriver().replace("{{USER_CODE}}", solutionBody);
                Path srcFile = tempDir.resolve("Main.java");
                try { Files.writeString(srcFile, fullSource); }
                catch (IOException e) { return ioError(e); }

                // Compile
                long start = System.currentTimeMillis();
                ProcessResult compile = runProcess(tempDir, List.of("javac", srcFile.toString()), 15);
                if (compile.exitCode != 0) {
                    long elapsed = System.currentTimeMillis() - start;
                    totalRuntime += elapsed;
                    String err = firstLines(compile.stderr, 3);
                    tcResults.add(new TestCaseResult(i + 1, tc.input(), tc.expected(),
                        "Compile Error: " + err, false, elapsed));
                    allPassed = false;
                    break;
                }

                // Run
                ProcessResult run = runProcess(tempDir, List.of("java", "-cp", tempDir.toString(), "Main"), 10);
                long elapsed = System.currentTimeMillis() - start;
                totalRuntime += elapsed;

                if (run.exitCode != 0) {
                    String err = firstLines(run.stderr.isBlank() ? run.stdout : run.stderr, 3);
                    tcResults.add(new TestCaseResult(i + 1, tc.input(), tc.expected(),
                        "Runtime Error: " + err, false, elapsed));
                    allPassed = false;
                    break;
                }

                String actual = run.stdout.trim().replaceAll(",\\s+", ",");
                boolean passed = normalize(actual).equals(normalize(tc.expected()));
                tcResults.add(new TestCaseResult(i + 1, tc.input(), tc.expected(), actual, passed, elapsed));
                if (!passed) allPassed = false;
            }
        } finally {
            deleteTempDir(tempDir);
        }

        result.setTestResults(tcResults);
        result.setRuntimeMs(totalRuntime);
        result.setStatus(deriveStatus(allPassed, tcResults));
        return result;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String deriveStatus(boolean allPassed, List<TestCaseResult> results) {
        if (allPassed) return "Accepted";
        for (TestCaseResult t : results) {
            if (t.getActual().startsWith("Compile Error")) return "Compile Error";
            if (t.getActual().startsWith("Runtime Error") || t.getActual().startsWith("Syntax Error")) return "Runtime Error";
        }
        return "Wrong Answer";
    }

    private String firstLines(String text, int n) {
        if (text == null) return "";
        return Arrays.stream(text.split("\n"))
            .map(String::trim)
            .filter(l -> !l.isBlank())
            .limit(n)
            .reduce("", (a, b) -> a.isEmpty() ? b : a + " | " + b);
    }

    private String extractSolutionBody(String code) {
        int classIdx = code.indexOf("class Solution");
        if (classIdx == -1) return "static class Solution { " + code + " }";
        int braceStart = code.indexOf('{', classIdx);
        if (braceStart == -1) return "static class Solution { " + code + " }";
        int depth = 0, end = -1;
        for (int i = braceStart; i < code.length(); i++) {
            if (code.charAt(i) == '{') depth++;
            else if (code.charAt(i) == '}') { depth--; if (depth == 0) { end = i; break; } }
        }
        if (end == -1) return "static class Solution { " + code + " }";
        return "static class Solution {\n" + code.substring(braceStart + 1, end) + "\n}";
    }

    private ProcessResult runProcess(Path workDir, List<String> cmd, int timeoutSec) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(workDir.toFile());
            pb.redirectErrorStream(false);
            Process proc = pb.start();

            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();
            Thread outT = new Thread(() -> {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                    String line; while ((line = r.readLine()) != null) stdout.append(line).append("\n");
                } catch (IOException ignored) {}
            });
            Thread errT = new Thread(() -> {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
                    String line; while ((line = r.readLine()) != null) stderr.append(line).append("\n");
                } catch (IOException ignored) {}
            });
            outT.start(); errT.start();

            boolean finished = proc.waitFor(timeoutSec, TimeUnit.SECONDS);
            if (!finished) { proc.destroyForcibly(); return new ProcessResult(-1, "", "Time Limit Exceeded (" + timeoutSec + "s)"); }
            outT.join(1000); errT.join(1000);
            return new ProcessResult(proc.exitValue(), stdout.toString(), stderr.toString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ProcessResult(-1, "", "Process error: " + e.getMessage());
        }
    }

    record ProcessResult(int exitCode, String stdout, String stderr) {}

    private String findPython() {
        return System.getProperty("os.name", "").toLowerCase().contains("win") ? "python" : "python3";
    }

    private String normalize(String s) {
        if (s == null) return "null";
        return s.trim().replaceAll(",\\s+", ",").replaceAll("\\s+", "").toLowerCase();
    }

    private void deleteTempDir(Path dir) {
        if (dir == null) return;
        try { Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete); }
        catch (IOException ignored) {}
    }

    private ExecutionResult ioError(IOException e) {
        ExecutionResult r = new ExecutionResult();
        r.setStatus("Internal Error");
        r.setErrorMessage("IO Error: " + e.getMessage());
        r.setTestResults(List.of());
        return r;
    }

    private ExecutionResult notSupported(List<TC> tests, String lang) {
        ExecutionResult result = new ExecutionResult();
        List<TestCaseResult> tcResults = new ArrayList<>();
        for (int i = 0; i < tests.size(); i++)
            tcResults.add(new TestCaseResult(i + 1, tests.get(i).input(), tests.get(i).expected(), "Not supported", false, 0));
        result.setStatus("Not Supported");
        result.setErrorMessage("Live execution for " + lang + " is not available in this environment.");
        result.setTestResults(tcResults);
        return result;
    }
}
