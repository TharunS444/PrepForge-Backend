package com.platform.config;

import com.platform.entity.Question;
import com.platform.entity.QuestionMcq;
import com.platform.entity.Test;
import com.platform.entity.User;
import com.platform.entity.Contest;
import com.platform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionMcqRepository questionMcqRepository;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedQuestions();
        seedTests();
        seedContests();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            // Seed Admin
            User admin = User.builder()
                    .name("Admin System")
                    .email("admin@platform.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ROLE_ADMIN")
                    .score(0)
                    .streak(0)
                    .build();
            userRepository.save(admin);

            // Seed Student 1
            User student1 = User.builder()
                    .name("Rahul Sharma")
                    .email("rahul@student.com")
                    .password(passwordEncoder.encode("student123"))
                    .role("ROLE_STUDENT")
                    .score(120)
                    .streak(3)
                    .build();
            userRepository.save(student1);

            // Seed Student 2
            User student2 = User.builder()
                    .name("Priya Patel")
                    .email("priya@student.com")
                    .password(passwordEncoder.encode("student123"))
                    .role("ROLE_STUDENT")
                    .score(80)
                    .streak(5)
                    .build();
            userRepository.save(student2);

            // Seed Student 3
            User student3 = User.builder()
                    .name("Kiran Kumar")
                    .email("kiran@student.com")
                    .password(passwordEncoder.encode("student123"))
                    .role("ROLE_STUDENT")
                    .score(50)
                    .streak(1)
                    .build();
            userRepository.save(student3);
        }
    }

    private void seedQuestions() {
        // Seed existing questions if they don't exist
        saveQuestionIfNotExist("Two Sum", "EASY", 
            "Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice.\n\n### Example 1\n**Input:** nums = [2,7,11,15], target = 9  \n**Output:** [0,1]", 
            "2,7,11,15\n9", "[0,1]", "TCS, Infosys, Accenture");
            
        saveQuestionIfNotExist("Reverse a String", "EASY", 
            "Write a program that takes a string as input and returns the reversed string.\n\n### Example 1\n**Input:** hello  \n**Output:** olleh", 
            "hello", "olleh", "Infosys, Accenture");
            
        saveQuestionIfNotExist("Add Two Numbers", "MEDIUM", 
            "You are given two non-empty linked lists representing two non-negative integers. The digits are stored in reverse order, and each of their nodes contains a single digit. Add the two numbers and return the sum as a linked list.\n\n### Example 1\n**Input:** l1 = [2,4,3], l2 = [5,6,4]  \n**Output:** [7,0,8]  \n**Explanation:** 342 + 465 = 807.", 
            "2,4,3\n5,6,4", "[7,0,8]", "Target, TCS");
            
        saveQuestionIfNotExist("Prime Numbers Range", "MEDIUM", 
            "Write a function to count all prime numbers strictly less than a non-negative number, `n`.\n\n### Example 1\n**Input:** 10  \n**Output:** 4 (Primes: 2, 3, 5, 7)", 
            "10", "4", "Target, Infosys");
            
        saveQuestionIfNotExist("Median of Two Sorted Arrays", "HARD", 
            "Given two sorted arrays `nums1` and `nums2` of size `m` and `n` respectively, return the median of the two sorted arrays.\n\n### Example 1\n**Input:** nums1 = [1,3], nums2 = [2]  \n**Output:** 2.0", 
            "1,3\n2", "2.0", "Target");

        // Topic 1: Arrays & Hashing
        saveQuestionIfNotExist("Contains Duplicate", "EASY", 
            "Given an integer array `nums`, return `true` if any value appears at least twice in the array, and return `false` if every element is distinct.\n\n### Example 1\n**Input:** nums = [1,2,3,1]  \n**Output:** true", 
            "1,2,3,1", "true", "TCS, Infosys");
            
        saveQuestionIfNotExist("Product of Array Except Self", "MEDIUM", 
            "Given an integer array `nums`, return an array `answer` such that `answer[i]` is equal to the product of all the elements of `nums` except `nums[i]`.\n\n### Example 1\n**Input:** nums = [1,2,3,4]  \n**Output:** [24,12,8,6]", 
            "1,2,3,4", "[24,12,8,6]", "Target");

        // Topic 2: Strings
        saveQuestionIfNotExist("Valid Anagram", "EASY", 
            "Given two strings `s` and `t`, return `true` if `t` is an anagram of `s`, and `false` otherwise.\n\n### Example 1\n**Input:** s = \"anagram\", t = \"nagaram\"  \n**Output:** true", 
            "anagram\nnagaram", "true", "Accenture, TCS");
            
        saveQuestionIfNotExist("Longest Substring Without Repeating Characters", "MEDIUM", 
            "Given a string `s`, find the length of the longest substring without repeating characters.\n\n### Example 1\n**Input:** s = \"abcabcbb\"  \n**Output:** 3", 
            "abcabcbb", "3", "Target, Infosys");

        // Topic 3: Linked Lists
        saveQuestionIfNotExist("Reverse Linked List", "EASY", 
            "Given the head of a singly linked list, reverse the list, and return the reversed list.\n\n### Example 1\n**Input:** head = [1,2,3,4,5]  \n**Output:** [5,4,3,2,1]", 
            "1,2,3,4,5", "[5,4,3,2,1]", "Infosys, Accenture");
            
        saveQuestionIfNotExist("Merge Two Sorted Lists", "EASY", 
            "You are given the heads of two sorted linked lists `list1` and `list2`. Merge the two lists into one sorted list and return it.\n\n### Example 1\n**Input:** list1 = [1,2,4], list2 = [1,3,4]  \n**Output:** [1,1,2,3,4,4]", 
            "1,2,4\n1,3,4", "[1,1,2,3,4,4]", "TCS, Target");

        // Topic 4: Math / Dynamic Programming
        saveQuestionIfNotExist("Fibonacci Number", "EASY", 
            "The Fibonacci numbers, commonly denoted `F(n)` form a sequence, called the Fibonacci sequence, such that each number is the sum of the two preceding ones, starting from 0 and 1. Given `n`, calculate `F(n)`.\n\n### Example 1\n**Input:** n = 4  \n**Output:** 3", 
            "4", "3", "Accenture, Infosys");
            
        saveQuestionIfNotExist("Climbing Stairs", "EASY", 
            "You are climbing a staircase. It takes `n` steps to reach the top. Each time you can either climb 1 or 2 steps. In how many distinct ways can you climb to the top?\n\n### Example 1\n**Input:** n = 3  \n**Output:** 3", 
            "3", "3", "TCS, Accenture");

        // Topic 5: Binary Search & Sorting
        saveQuestionIfNotExist("Binary Search", "EASY", 
            "Given an array of integers `nums` which is sorted in ascending order, and an integer `target`, write a function to search `target` in `nums`. If `target` exists, then return its index. Otherwise, return `-1`.\n\n### Example 1\n**Input:** nums = [-1,0,3,5,9,12], target = 9  \n**Output:** 4", 
            "-1,0,3,5,9,12\n9", "4", "Infosys, TCS");
            
        saveQuestionIfNotExist("Search in Rotated Sorted Array", "MEDIUM", 
            "Given the array `nums` after the possible rotation and an integer `target`, return the index of `target` if it is in `nums`, or `-1` if it is not in `nums`.\n\n### Example 1\n**Input:** nums = [4,5,6,7,0,1,2], target = 0  \n**Output:** 4", 
            "4,5,6,7,0,1,2\n0", "4", "Target, Infosys");
    }

    private void saveQuestionIfNotExist(String title, String difficulty, String description, String sampleInput, String sampleOutput, String companyTags) {
        if (!questionRepository.existsByTitle(title)) {
            Question q = Question.builder()
                    .title(title)
                    .difficulty(difficulty)
                    .description(description)
                    .sampleInput(sampleInput)
                    .sampleOutput(sampleOutput)
                    .companyTags(companyTags)
                    .build();
            questionRepository.save(q);
        }
    }

    private void seedTests() {
        if (testRepository.count() == 0) {
            // Test 1: Java Basics MCQ
            Test t1 = Test.builder()
                    .title("Java Core Assessment")
                    .durationMinutes(15)
                    .category("Technical")
                    .build();
            testRepository.save(t1);

            QuestionMcq q1_1 = QuestionMcq.builder()
                    .test(t1)
                    .questionText("Which of the following is NOT a concept of Object Oriented Programming?")
                    .optionA("Encapsulation")
                    .optionB("Inheritance")
                    .optionC("Compilation")
                    .optionD("Polymorphism")
                    .correctOption("C")
                    .marks(1)
                    .build();
            questionMcqRepository.save(q1_1);

            QuestionMcq q1_2 = QuestionMcq.builder()
                    .test(t1)
                    .questionText("What is the memory size of a double primitive type in Java?")
                    .optionA("4 bytes")
                    .optionB("8 bytes")
                    .optionC("16 bytes")
                    .optionD("Depends on OS")
                    .correctOption("B")
                    .marks(1)
                    .build();
            questionMcqRepository.save(q1_2);

            QuestionMcq q1_3 = QuestionMcq.builder()
                    .test(t1)
                    .questionText("Which class in Java is the root of the class hierarchy?")
                    .optionA("String")
                    .optionB("System")
                    .optionC("Object")
                    .optionD("Class")
                    .correctOption("C")
                    .marks(1)
                    .build();
            questionMcqRepository.save(q1_3);

            // Test 2: Aptitude Test
            Test t2 = Test.builder()
                    .title("Quantitative Aptitude Basics")
                    .durationMinutes(10)
                    .category("Aptitude")
                    .build();
            testRepository.save(t2);

            QuestionMcq q2_1 = QuestionMcq.builder()
                    .test(t2)
                    .questionText("A train running at the speed of 60 km/hr crosses a pole in 9 seconds. What is the length of the train?")
                    .optionA("120 meters")
                    .optionB("150 meters")
                    .optionC("180 meters")
                    .optionD("324 meters")
                    .correctOption("B")
                    .marks(1)
                    .build();
            questionMcqRepository.save(q2_1);

            QuestionMcq q2_2 = QuestionMcq.builder()
                    .test(t2)
                    .questionText("What is the average of first five prime numbers?")
                    .optionA("5.0")
                    .optionB("5.6")
                    .optionC("6.2")
                    .optionD("6.8")
                    .correctOption("B")
                    .marks(1)
                    .build();
            questionMcqRepository.save(q2_2);
        }
    }

    private void seedContests() {
        if (contestRepository.count() == 0) {
            List<Question> questions = questionRepository.findAll();
            if (questions.size() >= 2) {
                Contest c1 = new Contest();
                c1.setTitle("Weekly Coding Challenge #1");
                c1.setStartTime(LocalDateTime.now().minusDays(1)); // Started yesterday
                c1.setEndTime(LocalDateTime.now().plusDays(2)); // Ends in 2 days
                c1.setDurationMinutes(120);
                c1.setQuestions(questions.subList(0, 2)); // Use first two questions
                contestRepository.save(c1);
            }
        }
    }
}
