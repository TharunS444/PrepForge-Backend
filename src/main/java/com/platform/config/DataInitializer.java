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
        if (questionRepository.count() == 0) {
            // Q1: Two Sum
            Question q1 = Question.builder()
                    .title("Two Sum")
                    .difficulty("EASY")
                    .description("Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice.\n\n### Example 1\n**Input:** nums = [2,7,11,15], target = 9  \n**Output:** [0,1]")
                    .sampleInput("2,7,11,15\n9")
                    .sampleOutput("[0,1]")
                    .companyTags("TCS, Infosys, Accenture")
                    .build();
            questionRepository.save(q1);

            // Q2: Reverse String
            Question q2 = Question.builder()
                    .title("Reverse a String")
                    .difficulty("EASY")
                    .description("Write a program that takes a string as input and returns the reversed string.\n\n### Example 1\n**Input:** hello  \n**Output:** olleh")
                    .sampleInput("hello")
                    .sampleOutput("olleh")
                    .companyTags("Infosys, Accenture")
                    .build();
            questionRepository.save(q2);

            // Q3: Add Two Numbers
            Question q3 = Question.builder()
                    .title("Add Two Numbers")
                    .difficulty("MEDIUM")
                    .description("You are given two non-empty linked lists representing two non-negative integers. The digits are stored in reverse order, and each of their nodes contains a single digit. Add the two numbers and return the sum as a linked list.\n\n### Example 1\n**Input:** l1 = [2,4,3], l2 = [5,6,4]  \n**Output:** [7,0,8]  \n**Explanation:** 342 + 465 = 807.")
                    .sampleInput("2,4,3\n5,6,4")
                    .sampleOutput("[7,0,8]")
                    .companyTags("Target, TCS")
                    .build();
            questionRepository.save(q3);

            // Q4: Find Prime Numbers
            Question q4 = Question.builder()
                    .title("Prime Numbers Range")
                    .difficulty("MEDIUM")
                    .description("Write a function to count all prime numbers strictly less than a non-negative number, `n`.\n\n### Example 1\n**Input:** 10  \n**Output:** 4 (Primes: 2, 3, 5, 7)")
                    .sampleInput("10")
                    .sampleOutput("4")
                    .companyTags("Target, Infosys")
                    .build();
            questionRepository.save(q4);

            // Q5: Median of Two Sorted Arrays
            Question q5 = Question.builder()
                    .title("Median of Two Sorted Arrays")
                    .difficulty("HARD")
                    .description("Given two sorted arrays `nums1` and `nums2` of size `m` and `n` respectively, return the median of the two sorted arrays.\n\n### Example 1\n**Input:** nums1 = [1,3], nums2 = [2]  \n**Output:** 2.0")
                    .sampleInput("1,3\n2")
                    .sampleOutput("2.0")
                    .companyTags("Target")
                    .build();
            questionRepository.save(q5);
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
