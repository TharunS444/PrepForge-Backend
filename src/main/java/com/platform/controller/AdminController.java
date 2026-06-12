package com.platform.controller;

import com.platform.entity.User;
import com.platform.repository.ContestRepository;
import com.platform.repository.QuestionRepository;
import com.platform.repository.SubmissionRepository;
import com.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Enforce admin role
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ContestRepository contestRepository;

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        Map<String, Object> data = new HashMap<>();

        // Aggregate Stats
        data.put("totalUsers", userRepository.count());
        data.put("totalProblems", questionRepository.count());
        data.put("totalSubmissions", submissionRepository.count());
        
        // Active Contests - guard against null endTime
        LocalDateTime now = LocalDateTime.now();
        long activeContests = contestRepository.findAll().stream()
                .filter(c -> c.getEndTime() != null && c.getEndTime().isAfter(now))
                .count();
        data.put("activeContests", activeContests);

        // Top Solvers (Top 5 by score)
        List<User> topUsers = userRepository.findAllByOrderByScoreDesc().stream().limit(5).toList();
        List<Map<String, Object>> topSolversData = new ArrayList<>();
        for (User u : topUsers) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", u.getName());
            map.put("score", u.getScore());
            topSolversData.add(map);
        }
        data.put("topSolvers", topSolversData);

        // Real data aggregation from DB
        LocalDateTime oneWeekAgo = now.minusDays(7);
        
        List<com.platform.entity.Submission> recentSubs = submissionRepository.findAll().stream()
                .filter(s -> s.getSubmittedAt() != null && s.getSubmittedAt().isAfter(oneWeekAgo))
                .toList();
                
        List<User> recentUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(oneWeekAgo))
                .toList();

        List<Map<String, Object>> submissionsPerDay = new ArrayList<>();
        List<Map<String, Object>> userRegistrations = new ArrayList<>();
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEE");
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            String dayName = dayStart.format(formatter);
            
            long subsCount = recentSubs.stream()
                    .filter(s -> !s.getSubmittedAt().isBefore(dayStart) && s.getSubmittedAt().isBefore(dayEnd))
                    .count();
                    
            long usersCount = recentUsers.stream()
                    .filter(u -> !u.getCreatedAt().isBefore(dayStart) && u.getCreatedAt().isBefore(dayEnd))
                    .count();
                    
            Map<String, Object> subMap = new HashMap<>();
            subMap.put("day", dayName);
            subMap.put("count", subsCount);
            submissionsPerDay.add(subMap);
            
            Map<String, Object> regMap = new HashMap<>();
            regMap.put("day", dayName);
            regMap.put("count", usersCount);
            userRegistrations.add(regMap);
        }
        
        data.put("submissionsPerDay", submissionsPerDay);
        data.put("userRegistrations", userRegistrations);

        return ResponseEntity.ok(data);
    }
}
