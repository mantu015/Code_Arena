package com.codearena.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.*;

@Service
public class AICodeReviewService {

    public Map<String, Object> analyzeCode(String code, String language, String testResults) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Code quality metrics
        analysis.put("codeLength", code.length());
        analysis.put("lineCount", code.split("\n").length);
        analysis.put("qualityScore", calculateQualityScore(code, language));
        
        // Complexity analysis
        analysis.put("complexity", analyzeComplexity(code, language));
        
        // Suggestions
        analysis.put("suggestions", generateSuggestions(code, language, testResults));
        
        // Hints based on test failures
        analysis.put("hints", generateHints(testResults));
        
        // Best practices check
        analysis.put("bestPractices", checkBestPractices(code, language));
        
        return analysis;
    }

    private int calculateQualityScore(String code, String language) {
        int score = 100;
        
        // Penalize very long lines
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.length() > 120) score -= 2;
        }
        
        // Reward comments
        long commentCount = Arrays.stream(lines)
            .filter(line -> line.trim().startsWith("//") || line.trim().startsWith("#"))
            .count();
        score += Math.min(commentCount * 2, 10);
        
        // Check for proper naming
        if (language.equals("java") || language.equals("javascript")) {
            if (!code.contains("camelCase") && code.matches(".*[a-z][A-Z].*")) {
                score += 5;
            }
        }
        
        return Math.max(0, Math.min(100, score));
    }

    private String analyzeComplexity(String code, String language) {
        int nestedLoops = countNestedLoops(code);
        int conditionals = countConditionals(code);
        
        if (nestedLoops >= 3 || conditionals >= 5) {
            return "HIGH - Consider refactoring to reduce complexity";
        } else if (nestedLoops >= 2 || conditionals >= 3) {
            return "MEDIUM - Code complexity is acceptable";
        } else {
            return "LOW - Clean and simple code";
        }
    }

    private int countNestedLoops(String code) {
        int maxNesting = 0;
        int currentNesting = 0;
        
        Pattern loopPattern = Pattern.compile("\\b(for|while)\\b");
        Matcher matcher = loopPattern.matcher(code);
        
        while (matcher.find()) {
            currentNesting++;
            maxNesting = Math.max(maxNesting, currentNesting);
        }
        
        return maxNesting;
    }

    private int countConditionals(String code) {
        Pattern ifPattern = Pattern.compile("\\b(if|else if|switch)\\b");
        Matcher matcher = ifPattern.matcher(code);
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }

    private List<String> generateSuggestions(String code, String language, String testResults) {
        List<String> suggestions = new ArrayList<>();
        
        // Check for common inefficiencies
        if (code.contains("for") && code.contains("for")) {
            suggestions.add("💡 Consider using built-in methods instead of nested loops for better performance");
        }
        
        if (language.equals("python") && !code.contains("def ")) {
            suggestions.add("💡 Consider breaking code into functions for better readability");
        }
        
        if (code.length() > 500 && !code.contains("//") && !code.contains("#")) {
            suggestions.add("💡 Add comments to explain complex logic");
        }
        
        if (testResults != null && testResults.contains("Time Limit Exceeded")) {
            suggestions.add("⚡ Optimize your algorithm - current solution is too slow");
            suggestions.add("💡 Try using a HashMap/Dictionary for O(1) lookups");
        }
        
        return suggestions;
    }

    private List<String> generateHints(String testResults) {
        List<String> hints = new ArrayList<>();
        
        if (testResults == null) return hints;
        
        if (testResults.contains("Wrong Answer")) {
            hints.add("🔍 Check edge cases: empty input, single element, duplicates");
            hints.add("🔍 Verify your logic with the sample test cases");
        }
        
        if (testResults.contains("Runtime Error")) {
            hints.add("⚠️ Check for: null pointers, array bounds, division by zero");
            hints.add("⚠️ Add input validation");
        }
        
        if (testResults.contains("Compile Error")) {
            hints.add("🔧 Check syntax: missing semicolons, brackets, or parentheses");
            hints.add("🔧 Verify variable declarations and types");
        }
        
        return hints;
    }

    private List<String> checkBestPractices(String code, String language) {
        List<String> practices = new ArrayList<>();
        
        switch (language.toLowerCase()) {
            case "java":
                if (!code.contains("private") && !code.contains("public")) {
                    practices.add("❌ Use access modifiers (private/public)");
                } else {
                    practices.add("✅ Proper use of access modifiers");
                }
                break;
            case "python":
                if (code.matches(".*\\bdef [a-z_]+\\(.*")) {
                    practices.add("✅ Following Python naming conventions");
                }
                break;
            case "javascript":
                if (code.contains("const") || code.contains("let")) {
                    practices.add("✅ Using modern ES6+ syntax");
                }
                if (code.contains("var ")) {
                    practices.add("❌ Avoid 'var', use 'const' or 'let' instead");
                }
                break;
        }
        
        return practices;
    }
}
