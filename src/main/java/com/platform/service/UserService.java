package com.platform.service;

import com.platform.dto.RegisterRequest;
import com.platform.entity.User;
import com.platform.entity.Submission;
import com.platform.entity.Result;
import com.platform.repository.UserRepository;
import com.platform.repository.SubmissionRepository;
import com.platform.repository.ResultRepository;
import com.platform.repository.ContestParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.platform.repository.QuestionRepository questionRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private ContestParticipantRepository contestParticipantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_STUDENT") // Force all new registrations to be students for security
                .score(0)
                .streak(0)
                .build();

        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void updateLastActiveAndStreak(User user) {
        LocalDateTime now = LocalDateTime.now();
        if (user.getLastActive() != null) {
            long daysBetween = ChronoUnit.DAYS.between(user.getLastActive().toLocalDate(), now.toLocalDate());
            if (daysBetween == 1) {
                user.setStreak(user.getStreak() + 1);
            } else if (daysBetween > 1) {
                user.setStreak(1); // Reset streak
            }
        } else {
            user.setStreak(1); // First activity
        }
        user.setLastActive(now);
        userRepository.save(user);
    }

    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        List<Submission> submissions = submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId);
        List<Result> results = resultRepository.findByUserIdOrderByCompletedAtDesc(userId);

        Set<Long> solvedEasy = new HashSet<>();
        Set<Long> solvedMedium = new HashSet<>();
        Set<Long> solvedHard = new HashSet<>();

        for (Submission s : submissions) {
            if ("ACCEPTED".equalsIgnoreCase(s.getStatus())) {
                String diff = s.getQuestion().getDifficulty();
                Long qId = s.getQuestion().getId();
                if ("EASY".equalsIgnoreCase(diff)) {
                    solvedEasy.add(qId);
                } else if ("MEDIUM".equalsIgnoreCase(diff)) {
                    solvedMedium.add(qId);
                } else if ("HARD".equalsIgnoreCase(diff)) {
                    solvedHard.add(qId);
                }
            }
        }

        stats.put("easySolved", solvedEasy.size());
        stats.put("mediumSolved", solvedMedium.size());
        stats.put("hardSolved", solvedHard.size());
        stats.put("totalSolved", solvedEasy.size() + solvedMedium.size() + solvedHard.size());
        stats.put("totalSubmissions", submissions.size());

        int mcqTestsTaken = results.size();
        double avgMcqPercentage = 0;
        if (mcqTestsTaken > 0) {
            double totalScore = 0;
            double totalPossible = 0;
            for (Result r : results) {
                totalScore += r.getScore();
                totalPossible += r.getTotalMarks();
            }
            avgMcqPercentage = totalPossible > 0 ? (totalScore / totalPossible) * 100.0 : 0;
        }

        stats.put("mcqTestsTaken", mcqTestsTaken);
        stats.put("mcqAveragePercentage", Math.round(avgMcqPercentage * 100.0) / 100.0);

        stats.put("recentSubmissions", submissions.stream().limit(5).toList());
        stats.put("recentResults", results.stream().limit(5).toList());

        // Get User Rank
        List<User> rankedUsers = userRepository.findAllByOrderByScoreDesc();
        int rank = 0;
        for (int i = 0; i < rankedUsers.size(); i++) {
            if (rankedUsers.get(i).getId().equals(userId)) {
                rank = i + 1;
                break;
            }
        }
        stats.put("rank", rank);

        // Get Contest History
        stats.put("contestHistory", contestParticipantRepository.findByUserIdOrderByJoinedAtDesc(userId));

        // Send user score and streak back for badges
        User user = userRepository.findById(userId).orElseThrow();
        stats.put("score", user.getScore());
        stats.put("streak", user.getStreak());

        // Company Tracks Calculation
        java.util.List<com.platform.entity.Question> allQuestions = questionRepository.findAll();
        Map<String, Integer> companyTotals = new HashMap<>();
        Map<String, Integer> companySolved = new HashMap<>();
        String[] trackedCompanies = {"TCS", "Infosys", "Target", "Accenture"};
        
        for (String company : trackedCompanies) {
            companyTotals.put(company, 0);
            companySolved.put(company, 0);
        }

        for (com.platform.entity.Question q : allQuestions) {
            if (q.getCompanyTags() != null) {
                for (String company : trackedCompanies) {
                    if (q.getCompanyTags().toLowerCase().contains(company.toLowerCase())) {
                        companyTotals.put(company, companyTotals.get(company) + 1);
                        if (solvedEasy.contains(q.getId()) || solvedMedium.contains(q.getId()) || solvedHard.contains(q.getId())) {
                            companySolved.put(company, companySolved.get(company) + 1);
                        }
                    }
                }
            }
        }

        Map<String, Integer> companyProgress = new HashMap<>();
        for (String company : trackedCompanies) {
            int total = companyTotals.get(company);
            int solved = companySolved.get(company);
            companyProgress.put(company, total > 0 ? (int) Math.round((solved * 100.0) / total) : 0);
        }
        stats.put("companyProgress", companyProgress);

        return stats;
    }
}
