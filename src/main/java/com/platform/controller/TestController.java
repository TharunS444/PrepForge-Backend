package com.platform.controller;

import com.platform.dto.TestResultRequest;
import com.platform.entity.QuestionMcq;
import com.platform.entity.Result;
import com.platform.entity.Test;
import com.platform.entity.User;
import com.platform.security.UserPrincipal;
import com.platform.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    @Autowired
    private TestService testService;

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUser();
        }
        throw new RuntimeException("User not authenticated");
    }

    private boolean isAdmin() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            return "ROLE_ADMIN".equalsIgnoreCase(((UserPrincipal) principal).getUser().getRole());
        }
        return false;
    }

    @GetMapping
    public ResponseEntity<List<Test>> getAllTests() {
        return ResponseEntity.ok(testService.getAllTests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Test> getTestById(@PathVariable Long id) {
        return ResponseEntity.ok(testService.getTestById(id));
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionMcq>> getTestQuestions(@PathVariable Long id) {
        return ResponseEntity.ok(testService.getQuestionsByTestId(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitTest(@PathVariable Long id, @RequestBody TestResultRequest request) {
        try {
            User user = getAuthenticatedUser();
            Result result = testService.submitTestResult(user, id, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/results")
    public ResponseEntity<List<Result>> getUserResults() {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(testService.getResultsByUser(user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> createTest(@RequestBody Test test) {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("Access denied. Admins only.");
        }
        return ResponseEntity.ok(testService.createTest(test));
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<?> addQuestionToTest(@PathVariable Long id, @RequestBody QuestionMcq question) {
        if (!isAdmin()) {
            return ResponseEntity.status(403).body("Access denied. Admins only.");
        }
        return ResponseEntity.ok(testService.addQuestionToTest(id, question));
    }
}
