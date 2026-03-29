package com.codearena;

import com.codearena.model.Problem;
import com.codearena.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProblemRepository problemRepository;

    @Override
    public void run(String... args) {
        if (problemRepository.count() == 0) {
            seed();
        }
    }

    private void seed() {
        Problem p1 = new Problem();
        p1.setTitle("Two Sum");
        p1.setDifficulty("Easy");
        p1.setDescription(
            "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.\n\n" +
            "You may assume that each input would have exactly one solution, and you may not use the same element twice.\n\n" +
            "You can return the answer in any order."
        );
        p1.setTestCases("[{\"input\":\"[2,7,11,15], 9\",\"expected\":\"[0,1]\"},{\"input\":\"[3,2,4], 6\",\"expected\":\"[1,2]\"}]");
        problemRepository.save(p1);

        Problem p2 = new Problem();
        p2.setTitle("Add Two Numbers");
        p2.setDifficulty("Medium");
        p2.setDescription(
            "You are given two non-empty linked lists representing two non-negative integers. " +
            "The digits are stored in reverse order, and each of their nodes contains a single digit. " +
            "Add the two numbers and return the sum as a linked list.\n\n" +
            "You may assume the two numbers do not contain any leading zero, except the number 0 itself."
        );
        p2.setTestCases("[{\"input\":\"l1 = [2,4,3], l2 = [5,6,4]\",\"expected\":\"[7,0,8]\"}]");
        problemRepository.save(p2);

        Problem p3 = new Problem();
        p3.setTitle("Longest Substring Without Repeating Characters");
        p3.setDifficulty("Medium");
        p3.setDescription(
            "Given a string s, find the length of the longest substring without repeating characters.\n\n" +
            "A substring is a contiguous non-empty sequence of characters within a string."
        );
        p3.setTestCases("[{\"input\":\"s = \\\"abcabcbb\\\"\",\"expected\":\"3\"},{\"input\":\"s = \\\"pwwkew\\\"\",\"expected\":\"3\"}]");
        problemRepository.save(p3);

        Problem p4 = new Problem();
        p4.setTitle("Median of Two Sorted Arrays");
        p4.setDifficulty("Hard");
        p4.setDescription(
            "Given two sorted arrays nums1 and nums2 of size m and n respectively, " +
            "return the median of the two sorted arrays.\n\n" +
            "The overall run time complexity should be O(log (m+n))."
        );
        p4.setTestCases("[{\"input\":\"nums1 = [1,3], nums2 = [2]\",\"expected\":\"2.00000\"}]");
        problemRepository.save(p4);

        Problem p5 = new Problem();
        p5.setTitle("3Sum");
        p5.setDifficulty("Medium");
        p5.setDescription(
            "Given an integer array nums, return all the triplets [nums[i], nums[j], nums[k]] such that " +
            "i != j, i != k, and j != k, and nums[i] + nums[j] + nums[k] == 0.\n\n" +
            "Notice that the solution set must not contain duplicate triplets."
        );
        p5.setTestCases("[{\"input\":\"nums = [-1,0,1,2,-1,-4]\",\"expected\":\"[[-1,-1,2],[-1,0,1]]\"}]");
        problemRepository.save(p5);

        Problem p6 = new Problem();
        p6.setTitle("Regular Expression Matching");
        p6.setDifficulty("Hard");
        p6.setDescription(
            "Given an input string s and a pattern p, implement regular expression matching with support for '.' and '*' where:\n" +
            "- '.' Matches any single character.\n" +
            "- '*' Matches zero or more of the preceding element.\n\n" +
            "The matching should cover the entire input string (not partial)."
        );
        p6.setTestCases("[{\"input\":\"s = \\\"aa\\\", p = \\\"a\\\"\",\"expected\":\"false\"},{\"input\":\"s = \\\"aa\\\", p = \\\"a*\\\"\",\"expected\":\"true\"}]");
        problemRepository.save(p6);
    }
}
