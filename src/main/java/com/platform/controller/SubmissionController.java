package com.platform.controller;

import com.platform.dto.SubmissionRequest;
import com.platform.entity.Submission;
import com.platform.entity.User;
import com.platform.security.UserPrincipal;
import com.platform.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submission")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUser();
        }
        throw new RuntimeException("User not authenticated");
    }

    @PostMapping
    public ResponseEntity<?> submitCode(@RequestBody SubmissionRequest submissionRequest) {
        try {
            User user = getAuthenticatedUser();
            Submission submission = submissionService.evaluateSubmission(user, submissionRequest);
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<Submission>> getUserSubmissions(@PathVariable Long id) {
        // Can optionally enforce that users only view their own submissions (unless admin)
        User currentUser = getAuthenticatedUser();
        if (!currentUser.getId().equals(id) && !"ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(submissionService.getSubmissionsByUser(id));
    }
}
