package com.codearena;

import com.codearena.model.Problem;
import com.codearena.repository.ProblemRepository;
import com.codearena.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProblemRepository problemRepository;
    private final AchievementService achievementService;

    @Override
    public void run(String... args) {
        if (problemRepository.count() < 16) {
            seed();
        }
        achievementService.initializeAchievements();
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

        // Problem 7: Valid Parentheses
        Problem p7 = new Problem();
        p7.setTitle("Valid Parentheses");
        p7.setDifficulty("Easy");
        p7.setDescription(
            "Given a string s containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.\n\n" +
            "An input string is valid if:\n" +
            "1. Open brackets must be closed by the same type of brackets.\n" +
            "2. Open brackets must be closed in the correct order.\n" +
            "3. Every close bracket has a corresponding open bracket of the same type."
        );
        p7.setTestCases("[{\"input\":\"s = \\\"()\\\"\",\"expected\":\"true\"},{\"input\":\"s = \\\"()[]{}\\\"\",\"expected\":\"true\"},{\"input\":\"s = \\\"(]\\\"\",\"expected\":\"false\"}]");
        problemRepository.save(p7);

        // Problem 8: Merge Two Sorted Lists
        Problem p8 = new Problem();
        p8.setTitle("Merge Two Sorted Lists");
        p8.setDifficulty("Easy");
        p8.setDescription(
            "You are given the heads of two sorted linked lists list1 and list2.\n\n" +
            "Merge the two lists into one sorted list. The list should be made by splicing together the nodes of the first two lists.\n\n" +
            "Return the head of the merged linked list."
        );
        p8.setTestCases("[{\"input\":\"list1 = [1,2,4], list2 = [1,3,4]\",\"expected\":\"[1,1,2,3,4,4]\"},{\"input\":\"list1 = [], list2 = []\",\"expected\":\"[]\"}]");
        problemRepository.save(p8);

        // Problem 9: Maximum Subarray
        Problem p9 = new Problem();
        p9.setTitle("Maximum Subarray");
        p9.setDifficulty("Medium");
        p9.setDescription(
            "Given an integer array nums, find the subarray with the largest sum, and return its sum.\n\n" +
            "A subarray is a contiguous non-empty sequence of elements within an array."
        );
        p9.setTestCases("[{\"input\":\"nums = [-2,1,-3,4,-1,2,1,-5,4]\",\"expected\":\"6\"},{\"input\":\"nums = [1]\",\"expected\":\"1\"},{\"input\":\"nums = [5,4,-1,7,8]\",\"expected\":\"23\"}]");
        problemRepository.save(p9);

        // Problem 10: Climbing Stairs
        Problem p10 = new Problem();
        p10.setTitle("Climbing Stairs");
        p10.setDifficulty("Easy");
        p10.setDescription(
            "You are climbing a staircase. It takes n steps to reach the top.\n\n" +
            "Each time you can either climb 1 or 2 steps. In how many distinct ways can you climb to the top?"
        );
        p10.setTestCases("[{\"input\":\"n = 2\",\"expected\":\"2\"},{\"input\":\"n = 3\",\"expected\":\"3\"},{\"input\":\"n = 5\",\"expected\":\"8\"}]");
        problemRepository.save(p10);

        // Problem 11: Binary Tree Inorder Traversal
        Problem p11 = new Problem();
        p11.setTitle("Binary Tree Inorder Traversal");
        p11.setDifficulty("Easy");
        p11.setDescription(
            "Given the root of a binary tree, return the inorder traversal of its nodes' values.\n\n" +
            "Inorder traversal visits nodes in the order: Left -> Root -> Right."
        );
        p11.setTestCases("[{\"input\":\"root = [1,null,2,3]\",\"expected\":\"[1,3,2]\"},{\"input\":\"root = []\",\"expected\":\"[]\"},{\"input\":\"root = [1]\",\"expected\":\"[1]\"}]");
        problemRepository.save(p11);

        // Problem 12: Best Time to Buy and Sell Stock
        Problem p12 = new Problem();
        p12.setTitle("Best Time to Buy and Sell Stock");
        p12.setDifficulty("Easy");
        p12.setDescription(
            "You are given an array prices where prices[i] is the price of a given stock on the ith day.\n\n" +
            "You want to maximize your profit by choosing a single day to buy one stock and choosing a different day in the future to sell that stock.\n\n" +
            "Return the maximum profit you can achieve from this transaction. If you cannot achieve any profit, return 0."
        );
        p12.setTestCases("[{\"input\":\"prices = [7,1,5,3,6,4]\",\"expected\":\"5\"},{\"input\":\"prices = [7,6,4,3,1]\",\"expected\":\"0\"}]");
        problemRepository.save(p12);

        // Problem 13: Valid Palindrome
        Problem p13 = new Problem();
        p13.setTitle("Valid Palindrome");
        p13.setDifficulty("Easy");
        p13.setDescription(
            "A phrase is a palindrome if, after converting all uppercase letters into lowercase letters and removing all non-alphanumeric characters, " +
            "it reads the same forward and backward. Alphanumeric characters include letters and numbers.\n\n" +
            "Given a string s, return true if it is a palindrome, or false otherwise."
        );
        p13.setTestCases("[{\"input\":\"s = \\\"A man, a plan, a canal: Panama\\\"\",\"expected\":\"true\"},{\"input\":\"s = \\\"race a car\\\"\",\"expected\":\"false\"}]");
        problemRepository.save(p13);

        // Problem 14: Reverse Linked List
        Problem p14 = new Problem();
        p14.setTitle("Reverse Linked List");
        p14.setDifficulty("Easy");
        p14.setDescription(
            "Given the head of a singly linked list, reverse the list, and return the reversed list."
        );
        p14.setTestCases("[{\"input\":\"head = [1,2,3,4,5]\",\"expected\":\"[5,4,3,2,1]\"},{\"input\":\"head = [1,2]\",\"expected\":\"[2,1]\"},{\"input\":\"head = []\",\"expected\":\"[]\"}]");
        problemRepository.save(p14);

        // Problem 15: Contains Duplicate
        Problem p15 = new Problem();
        p15.setTitle("Contains Duplicate");
        p15.setDifficulty("Easy");
        p15.setDescription(
            "Given an integer array nums, return true if any value appears at least twice in the array, " +
            "and return false if every element is distinct."
        );
        p15.setTestCases("[{\"input\":\"nums = [1,2,3,1]\",\"expected\":\"true\"},{\"input\":\"nums = [1,2,3,4]\",\"expected\":\"false\"},{\"input\":\"nums = [1,1,1,3,3,4,3,2,4,2]\",\"expected\":\"true\"}]");
        problemRepository.save(p15);

        // Problem 16: Product of Array Except Self
        Problem p16 = new Problem();
        p16.setTitle("Product of Array Except Self");
        p16.setDifficulty("Medium");
        p16.setDescription(
            "Given an integer array nums, return an array answer such that answer[i] is equal to the product of all the elements of nums except nums[i].\n\n" +
            "The product of any prefix or suffix of nums is guaranteed to fit in a 32-bit integer.\n\n" +
            "You must write an algorithm that runs in O(n) time and without using the division operation."
        );
        p16.setTestCases("[{\"input\":\"nums = [1,2,3,4]\",\"expected\":\"[24,12,8,6]\"},{\"input\":\"nums = [-1,1,0,-3,3]\",\"expected\":\"[0,0,9,0,0]\"}]");
        problemRepository.save(p16);
    }
}
