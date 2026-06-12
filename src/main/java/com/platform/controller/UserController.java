package com.platform.controller;

import com.platform.entity.User;
import com.platform.security.UserPrincipal;
import com.platform.service.UserService;
import com.platform.service.BadgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUser();
        }
        throw new RuntimeException("User not authenticated");
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile() {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(userService.findById(user.getId()).orElse(user));
    }

    @Autowired
    private BadgeService badgeService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMyStats() {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(userService.getUserStats(user.getId()));
    }

    @GetMapping("/me/badges")
    public ResponseEntity<?> getMyBadges() {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(badgeService.getUserBadges(user.getId()));
    }

    @Autowired
    private com.platform.service.PdfService pdfService;

    @Autowired
    private com.platform.service.SubmissionService submissionService;

    @GetMapping(value = "/me/report", produces = org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getProgressReport() {
        User user = getAuthenticatedUser();
        java.util.List<com.platform.entity.Submission> subs = submissionService.getSubmissionsByUser(user.getId());
        java.util.List<com.platform.entity.UserBadge> badges = badgeService.getUserBadges(user.getId());
        byte[] pdfBytes = pdfService.generateProgressReport(user, subs, badges);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentDispositionFormData("attachment", "ProgressReport_" + user.getName() + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
}
