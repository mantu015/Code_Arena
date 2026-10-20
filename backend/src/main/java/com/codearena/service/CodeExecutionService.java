package com.codearena.service;

import com.codearena.model.ExecutionResult;
import com.codearena.model.ExecutionResult.TestCaseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CodeExecutionService {

    private final Judge0RunnerService runnerService;

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
            "function _ml(a){if(!a.length)return null;let h={val:a[0],next:null},c=h;for(let i=1;i<a.length;i++){c.next={val:a[i],next:null};c=c.next;}return h;}" +
            "function _ta(n){let r=[];while(n){r.push(n.val);n=n.next;}return r;}";
        String pyListHelper =
            "\nclass ListNode:\n    def __init__(self,val=0,next=None):\n        self.val=val;self.next=next\n" +
            "def _ml(a):\n    if not a:return None\n    h=ListNode(a[0]);c=h\n    for v in a[1:]:c.next=ListNode(v);c=c.next\n    return h\n" +
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

        String jsTreeHelper =
            "function _bt(a){if(!a.length||a[0]==null)return null;let r={val:a[0],left:null,right:null},q=[r],i=1;" +
            "while(q.length&&i<a.length){let n=q.shift();if(i<a.length&&a[i]!=null){n.left={val:a[i],left:null,right:null};q.push(n.left);}i++;" +
            "if(i<a.length&&a[i]!=null){n.right={val:a[i],left:null,right:null};q.push(n.right);}i++;}return r;}";
        String pyTreeHelper =
            "\nclass TreeNode:\n    def __init__(self,val=0,left=None,right=None):self.val=val;self.left=left;self.right=right\n" +
            "def _bt(a):\n    if not a or a[0] is None:return None\n" +
            "    root=TreeNode(a[0]);q=[root];i=1\n" +
            "    while q and i<len(a):\n        n=q.pop(0)\n        if i<len(a) and a[i] is not None:n.left=TreeNode(a[i]);q.append(n.left)\n        i+=1\n" +
            "        if i<len(a) and a[i] is not None:n.right=TreeNode(a[i]);q.append(n.right)\n        i+=1\n    return root\n";

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

        TESTS.put(7L, List.of(
            new TC("s='()'", "true", "String(isValid('()'))",
                pyCall("print(str(isValid('()')).lower())"),
                javaWrap(7, "System.out.println(new Solution().isValid(\"()\"));")),
            new TC("s='()[]{}'", "true", "String(isValid('()[]{}'))",
                pyCall("print(str(isValid('()[]{}')).lower())"),
                javaWrap(7, "System.out.println(new Solution().isValid(\"()[]{}\"));")),
            new TC("s='(]'", "false", "String(isValid('(]'))",
                pyCall("print(str(isValid('(]')).lower())"),
                javaWrap(7, "System.out.println(new Solution().isValid(\"(]\"));"))
        ));

        TESTS.put(8L, List.of(
            new TC("list1=[1,2,4], list2=[1,3,4]", "[1,1,2,3,4,4]",
                jsListHelper + "JSON.stringify(_ta(mergeTwoLists(_ml([1,2,4]),_ml([1,3,4]))))",
                pyListHelper + pyCall("print(_ta(mergeTwoLists(_ml([1,2,4]),_ml([1,3,4]))))"),
                javaWrap(8, "System.out.println(Arrays.toString(toArr(new Solution().mergeTwoLists(makeList(new int[]{1,2,4}),makeList(new int[]{1,3,4})))).replace(\", \",\",\"));")),
            new TC("list1=[], list2=[]", "[]",
                jsListHelper + "JSON.stringify(_ta(mergeTwoLists(_ml([]),_ml([]))))",
                pyListHelper + pyCall("print(_ta(mergeTwoLists(_ml([]),_ml([]))))"),
                javaWrap(8, "System.out.println(Arrays.toString(toArr(new Solution().mergeTwoLists(makeList(new int[]{}),makeList(new int[]{})))).replace(\", \",\",\"));"))
        ));

        TESTS.put(9L, List.of(
            new TC("nums=[-2,1,-3,4,-1,2,1,-5,4]", "6",
                "String(maxSubArray([-2,1,-3,4,-1,2,1,-5,4]))",
                pyCall("print(maxSubArray([-2,1,-3,4,-1,2,1,-5,4]))"),
                javaWrap(9, "System.out.println(new Solution().maxSubArray(new int[]{-2,1,-3,4,-1,2,1,-5,4}));")),
            new TC("nums=[1]", "1",
                "String(maxSubArray([1]))",
                pyCall("print(maxSubArray([1]))"),
                javaWrap(9, "System.out.println(new Solution().maxSubArray(new int[]{1}));")),
            new TC("nums=[5,4,-1,7,8]", "23",
                "String(maxSubArray([5,4,-1,7,8]))",
                pyCall("print(maxSubArray([5,4,-1,7,8]))"),
                javaWrap(9, "System.out.println(new Solution().maxSubArray(new int[]{5,4,-1,7,8}));"))
        ));

        TESTS.put(10L, List.of(
            new TC("n=2", "2", "String(climbStairs(2))",
                pyCall("print(climbStairs(2))"),
                javaWrap(10, "System.out.println(new Solution().climbStairs(2));")),
            new TC("n=3", "3", "String(climbStairs(3))",
                pyCall("print(climbStairs(3))"),
                javaWrap(10, "System.out.println(new Solution().climbStairs(3));")),
            new TC("n=5", "8", "String(climbStairs(5))",
                pyCall("print(climbStairs(5))"),
                javaWrap(10, "System.out.println(new Solution().climbStairs(5));"))
        ));

        TESTS.put(11L, List.of(
            new TC("root=[1,null,2,3]", "[1,3,2]",
                jsTreeHelper + "JSON.stringify(inorderTraversal(_bt([1,null,2,3])))",
                pyTreeHelper + pyCall("print(inorderTraversal(_bt([1,None,2,3])))"),
                javaWrap(11, "System.out.println(new Solution().inorderTraversal(makeTree(new Integer[]{1,null,2,3})).toString().replace(\", \",\",\"));")),
            new TC("root=[]", "[]",
                jsTreeHelper + "JSON.stringify(inorderTraversal(_bt([])))",
                pyTreeHelper + pyCall("print(inorderTraversal(_bt([])))"),
                javaWrap(11, "System.out.println(new Solution().inorderTraversal(makeTree(new Integer[]{})).toString().replace(\", \",\",\"));")),
            new TC("root=[1]", "[1]",
                jsTreeHelper + "JSON.stringify(inorderTraversal(_bt([1])))",
                pyTreeHelper + pyCall("print(inorderTraversal(_bt([1])))"),
                javaWrap(11, "System.out.println(new Solution().inorderTraversal(makeTree(new Integer[]{1})).toString().replace(\", \",\",\"));"))
        ));

        TESTS.put(12L, List.of(
            new TC("prices=[7,1,5,3,6,4]", "5",
                "String(maxProfit([7,1,5,3,6,4]))",
                pyCall("print(maxProfit([7,1,5,3,6,4]))"),
                javaWrap(12, "System.out.println(new Solution().maxProfit(new int[]{7,1,5,3,6,4}));")),
            new TC("prices=[7,6,4,3,1]", "0",
                "String(maxProfit([7,6,4,3,1]))",
                pyCall("print(maxProfit([7,6,4,3,1]))"),
                javaWrap(12, "System.out.println(new Solution().maxProfit(new int[]{7,6,4,3,1}));"))
        ));

        TESTS.put(13L, List.of(
            new TC("s='A man, a plan, a canal: Panama'", "true",
                "String(isPalindrome('A man, a plan, a canal: Panama'))",
                pyCall("print(str(isPalindrome('A man, a plan, a canal: Panama')).lower())"),
                javaWrap(13, "System.out.println(new Solution().isPalindrome(\"A man, a plan, a canal: Panama\"));")),
            new TC("s='race a car'", "false",
                "String(isPalindrome('race a car'))",
                pyCall("print(str(isPalindrome('race a car')).lower())"),
                javaWrap(13, "System.out.println(new Solution().isPalindrome(\"race a car\"));"))
        ));

        TESTS.put(14L, List.of(
            new TC("head=[1,2,3,4,5]", "[5,4,3,2,1]",
                jsListHelper + "JSON.stringify(_ta(reverseList(_ml([1,2,3,4,5]))))",
                pyListHelper + pyCall("print(_ta(reverseList(_ml([1,2,3,4,5]))))"),
                javaWrap(14, "System.out.println(Arrays.toString(toArr(new Solution().reverseList(makeList(new int[]{1,2,3,4,5})))).replace(\", \",\",\"));")),
            new TC("head=[1,2]", "[2,1]",
                jsListHelper + "JSON.stringify(_ta(reverseList(_ml([1,2]))))",
                pyListHelper + pyCall("print(_ta(reverseList(_ml([1,2]))))"),
                javaWrap(14, "System.out.println(Arrays.toString(toArr(new Solution().reverseList(makeList(new int[]{1,2})))).replace(\", \",\",\"));")),
            new TC("head=[]", "[]",
                jsListHelper + "JSON.stringify(_ta(reverseList(_ml([]))))",
                pyListHelper + pyCall("print(_ta(reverseList(_ml([]))))"),
                javaWrap(14, "System.out.println(Arrays.toString(toArr(new Solution().reverseList(makeList(new int[]{})))).replace(\", \",\",\"));"))
        ));

        TESTS.put(15L, List.of(
            new TC("nums=[1,2,3,1]", "true",
                "String(containsDuplicate([1,2,3,1]))",
                pyCall("print(str(containsDuplicate([1,2,3,1])).lower())"),
                javaWrap(15, "System.out.println(new Solution().containsDuplicate(new int[]{1,2,3,1}));")),
            new TC("nums=[1,2,3,4]", "false",
                "String(containsDuplicate([1,2,3,4]))",
                pyCall("print(str(containsDuplicate([1,2,3,4])).lower())"),
                javaWrap(15, "System.out.println(new Solution().containsDuplicate(new int[]{1,2,3,4}));")),
            new TC("nums=[1,1,1,3,3,4,3,2,4,2]", "true",
                "String(containsDuplicate([1,1,1,3,3,4,3,2,4,2]))",
                pyCall("print(str(containsDuplicate([1,1,1,3,3,4,3,2,4,2])).lower())"),
                javaWrap(15, "System.out.println(new Solution().containsDuplicate(new int[]{1,1,1,3,3,4,3,2,4,2}));"))
        ));

        TESTS.put(16L, List.of(
            new TC("nums=[1,2,3,4]", "[24,12,8,6]",
                "JSON.stringify(productExceptSelf([1,2,3,4]))",
                pyCall("print(productExceptSelf([1,2,3,4]))"),
                javaWrap(16, "System.out.println(Arrays.toString(new Solution().productExceptSelf(new int[]{1,2,3,4})).replace(\", \",\",\"));")),
            new TC("nums=[-1,1,0,-3,3]", "[0,0,9,0,0]",
                "JSON.stringify(productExceptSelf([-1,1,0,-3,3]))",
                pyCall("print(productExceptSelf([-1,1,0,-3,3]))"),
                javaWrap(16, "System.out.println(Arrays.toString(new Solution().productExceptSelf(new int[]{-1,1,0,-3,3})).replace(\", \",\",\"));"))
        ));
    }

    private static String javaWrap(int problemId, String callCode) {
        String listNodeHelper = "";
        if (problemId == 2 || problemId == 8 || problemId == 14) {
            listNodeHelper =
                "    static class ListNode { int val; ListNode next; ListNode(int v){val=v;} }\n" +
                "    static ListNode makeList(int[] a){\n" +
                "        if(a.length==0)return null;\n" +
                "        ListNode h=new ListNode(a[0]),c=h;\n" +
                "        for(int i=1;i<a.length;i++){c.next=new ListNode(a[i]);c=c.next;}\n" +
                "        return h;}\n" +
                "    static int[] toArr(ListNode n){\n" +
                "        List<Integer> r=new ArrayList<>();\n" +
                "        while(n!=null){r.add(n.val);n=n.next;}\n" +
                "        return r.stream().mapToInt(Integer::intValue).toArray();}\n";
        }
        String treeNodeHelper = "";
        if (problemId == 11) {
            treeNodeHelper =
                "    static class TreeNode { int val; TreeNode left,right; TreeNode(int v){val=v;} }\n" +
                "    static TreeNode makeTree(Integer[] a){\n" +
                "        if(a.length==0||a[0]==null)return null; TreeNode root=new TreeNode(a[0]); Queue<TreeNode> q=new LinkedList<>(); q.add(root); int i=1;\n" +
                "        while(!q.isEmpty()&&i<a.length){TreeNode n=q.remove(); if(i<a.length&&a[i]!=null){n.left=new TreeNode(a[i]);q.add(n.left);}i++; if(i<a.length&&a[i]!=null){n.right=new TreeNode(a[i]);q.add(n.right);}i++;} return root;}\n";
        }
        return "import java.util.*;\n" +
               "public class Main {\n" +
               listNodeHelper +
               treeNodeHelper +
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
        if (language == null || !Set.of("javascript", "python", "java", "cpp", "c++").contains(language.toLowerCase(Locale.ROOT))) {
            return notSupported(tests, language == null ? "unknown" : language);
        }

        try {
            List<TestCaseResult> testResults = new ArrayList<>();
            long totalRuntime = 0;
            boolean allPassed = true;
            String solutionBody = language.equalsIgnoreCase("java") ? extractSolutionBody(code) : null;

            for (int i = 0; i < tests.size(); i++) {
                TC test = tests.get(i);
                String source = switch (language.toLowerCase(Locale.ROOT)) {
                    case "javascript" -> code + "\nconsole.log(String(eval("
                            + runnerService.encodeJavaScriptString(test.jsHarness()) + ")));\n";
                    case "python" -> code + "\n" + test.pyDriver();
                    case "java" -> test.javaDriver().replace("{{USER_CODE}}", solutionBody);
                    case "cpp", "c++" -> cppSource(problemId, test.input(), code);
                    default -> throw new IllegalStateException("Unsupported language reached the execution dispatcher.");
                };
                Judge0RunnerService.RunnerResult run = runnerService.execute(language, source, "");
                totalRuntime += run.runtimeMs();
                String actual = actualOutput(run);
                boolean passed = run.statusId() == 3
                        && !actual.startsWith("Runtime Error:")
                        && normalize(actual).equals(normalize(test.expected()));
                testResults.add(new TestCaseResult(
                        i + 1, test.input(), test.expected(), actual, passed, run.runtimeMs()));
                if (!passed) {
                    allPassed = false;
                    break;
                }
            }

            ExecutionResult result = new ExecutionResult();
            result.setTestResults(testResults);
            result.setRuntimeMs(totalRuntime);
            result.setStatus(deriveStatus(allPassed, testResults));
            return result;
        } catch (IllegalArgumentException exception) {
            return executionError("Invalid execution request", exception.getMessage());
        } catch (Judge0RunnerService.CodeRunnerException exception) {
            return executionError("Runner Error", exception.getMessage());
        }
    }

    private String actualOutput(Judge0RunnerService.RunnerResult run) {
        if (run.statusId() == 6) {
            return "Compile Error: " + firstLines(run.compileOutput(), 3);
        }
        if (run.statusId() == 5) {
            return "Time Limit Exceeded";
        }
        if (run.statusId() != 3) {
            String details = firstLines(
                    run.stderr() == null || run.stderr().isBlank() ? run.stdout() : run.stderr(), 3);
            return "Runtime Error: " + (details.isBlank() ? run.status() : details);
        }
        return run.stdout() == null ? "" : run.stdout().trim();
    }

    private ExecutionResult executionError(String status, String message) {
        ExecutionResult result = new ExecutionResult();
        result.setStatus(status);
        result.setErrorMessage(message);
        result.setTestResults(List.of());
        return result;
    }

    private String cppSource(Long problemId, String input, String code) {
        String call = switch (problemId.intValue()) {
            case 1 -> "vector<int> nums=" + intArray(input, "nums") + "; int target=" + intValue(input, "target")
                    + "; auto r=Solution().twoSum(nums,target); sort(r.begin(),r.end()); printVector(r);";
            case 2 -> "auto r=Solution().addTwoNumbers(makeList(" + intArray(input, "l1")
                    + "),makeList(" + intArray(input, "l2") + ")); printVector(listToVector(r));";
            case 3 -> "cout << Solution().lengthOfLongestSubstring(" + cppString(input, "s") + ");";
            case 4 -> "vector<int> a=" + intArray(input, "nums1") + ", b=" + intArray(input, "nums2")
                    + "; cout << fixed << setprecision(1) << Solution().findMedianSortedArrays(a,b);";
            case 5 -> "vector<int> nums=" + intArray(input, "nums") + "; auto r=Solution().threeSum(nums)"
                    + "; for(auto& v:r) sort(v.begin(),v.end()); sort(r.begin(),r.end()); printNested(r);";
            case 6 -> "cout << boolalpha << Solution().isMatch(" + cppString(input, "s") + ","
                    + cppString(input, "p") + ");";
            case 7 -> "cout << boolalpha << Solution().isValid(" + cppString(input, "s") + ");";
            case 8 -> "auto r=Solution().mergeTwoLists(makeList(" + intArray(input, "list1")
                    + "),makeList(" + intArray(input, "list2") + ")); printVector(listToVector(r));";
            case 9 -> "vector<int> nums=" + intArray(input, "nums") + "; cout << Solution().maxSubArray(nums);";
            case 10 -> "cout << Solution().climbStairs(" + intValue(input, "n") + ");";
            case 11 -> "printVector(Solution().inorderTraversal(makeTree(" + treeArray(input) + ")));";
            case 12 -> "vector<int> prices=" + intArray(input, "prices") + "; cout << Solution().maxProfit(prices);";
            case 13 -> "cout << boolalpha << Solution().isPalindrome(" + cppString(input, "s") + ");";
            case 14 -> "auto r=Solution().reverseList(makeList(" + intArray(input, "head")
                    + ")); printVector(listToVector(r));";
            case 15 -> "vector<int> nums=" + intArray(input, "nums") + "; cout << boolalpha << Solution().containsDuplicate(nums);";
            case 16 -> "vector<int> nums=" + intArray(input, "nums") + "; printVector(Solution().productExceptSelf(nums));";
            default -> throw new IllegalArgumentException("No C++ test harness exists for this problem.");
        };

        return "#include <bits/stdc++.h>\nusing namespace std;\n"
                + "struct ListNode{int val;ListNode* next;ListNode(int x=0,ListNode* n=nullptr):val(x),next(n){}};\n"
                + "struct TreeNode{int val;TreeNode* left;TreeNode* right;TreeNode(int x=0,TreeNode* l=nullptr,TreeNode* r=nullptr):val(x),left(l),right(r){}};\n"
                + "ListNode* makeList(vector<int> a){if(a.empty())return nullptr;ListNode* h=new ListNode(a[0]);auto c=h;for(size_t i=1;i<a.size();i++){c->next=new ListNode(a[i]);c=c->next;}return h;}\n"
                + "vector<int> listToVector(ListNode* n){vector<int> r;while(n){r.push_back(n->val);n=n->next;}return r;}\n"
                + "TreeNode* makeTree(vector<string> a){if(a.empty()||a[0]==\"null\")return nullptr;TreeNode* r=new TreeNode(stoi(a[0]));vector<TreeNode*> q{r};size_t i=1,j=0;while(j<q.size()&&i<a.size()){auto n=q[j++];if(i<a.size()&&a[i]!=\"null\"){n->left=new TreeNode(stoi(a[i]));q.push_back(n->left);}i++;if(i<a.size()&&a[i]!=\"null\"){n->right=new TreeNode(stoi(a[i]));q.push_back(n->right);}i++;}return r;}\n"
                + "void printVector(const vector<int>& v){cout<<\"[\";for(size_t i=0;i<v.size();i++){if(i)cout<<\",\";cout<<v[i];}cout<<\"]\\n\";}\n"
                + "void printVectorInline(const vector<int>& v){cout<<\"[\";for(size_t i=0;i<v.size();i++){if(i)cout<<\",\";cout<<v[i];}cout<<\"]\";}\n"
                + "void printNested(const vector<vector<int>>& v){cout<<\"[\";for(size_t i=0;i<v.size();i++){if(i)cout<<\",\";printVectorInline(v[i]);}cout<<\"]\\n\";}\n"
                + code + "\nint main(){ " + call + " return 0; }\n";
    }

    private String intArray(String input, String key) {
        String values = capture(input, "\\b" + Pattern.quote(key) + "\\s*=\\s*\\[([^\\]]*)\\]");
        if (values.isBlank()) {
            return "vector<int>{}";
        }
        return "vector<int>{" + values.replaceAll("\\s+", "") + "}";
    }

    private String treeArray(String input) {
        String values = capture(input, "\\broot\\s*=\\s*\\[([^\\]]*)\\]");
        if (values.isBlank()) {
            return "vector<string>{}";
        }
        String[] nodes = values.split(",");
        List<String> quoted = new ArrayList<>();
        for (String node : nodes) {
            String value = node.trim();
            quoted.add("\"" + value + "\"");
        }
        return "vector<string>{" + String.join(",", quoted) + "}";
    }

    private String intValue(String input, String key) {
        return capture(input, "\\b" + Pattern.quote(key) + "\\s*=\\s*(-?\\d+)");
    }

    private String cppString(String input, String key) {
        String value = capture(input, "\\b" + Pattern.quote(key) + "\\s*=\\s*(['\"])(.*?)\\1");
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private String capture(String input, String expression) {
        Matcher matcher = Pattern.compile(expression).matcher(input);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Could not prepare the test input for the C++ runner.");
        }
        return matcher.group(matcher.groupCount());
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
