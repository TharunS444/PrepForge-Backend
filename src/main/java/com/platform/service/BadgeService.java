package com.platform.service;

import com.platform.entity.User;
import com.platform.entity.UserBadge;
import com.platform.repository.ContestParticipantRepository;
import com.platform.repository.SubmissionRepository;
import com.platform.repository.UserBadgeRepository;
import com.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BadgeService {

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ContestParticipantRepository contestParticipantRepository;

    @Autowired
    private UserRepository userRepository;

    public void checkAndUnlockBadges(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || "ROLE_ADMIN".equals(user.getRole())) return;

        long solvedCount = submissionRepository.countByUserIdAndStatus(userId, "ACCEPTED");

        // First Solve
        if (solvedCount >= 1) {
            awardBadge(userId, "FIRST_SOLVE", "First Solve", "Solved your first problem!", "🥉");
        }
        
        // 10 Solves
        if (solvedCount >= 10) {
            awardBadge(userId, "TEN_SOLVES", "10 Solves", "Solved 10 programming problems", "🥈");
        }

        // 50 Solves
        if (solvedCount >= 50) {
            awardBadge(userId, "FIFTY_SOLVES", "50 Solves", "Solved 50 programming problems", "🥇");
        }

        // Streak Badges
        if (user.getStreak() >= 7) {
            awardBadge(userId, "SEVEN_DAY_STREAK", "7 Day Streak", "Maintained a 7 day active streak", "🔥");
        }

        // Top 10 Rank
        List<User> rankedUsers = userRepository.findAllByOrderByScoreDesc();
        int rank = -1;
        for (int i = 0; i < rankedUsers.size(); i++) {
            if (rankedUsers.get(i).getId().equals(userId)) {
                rank = i + 1;
                break;
            }
        }
        if (rank > 0 && rank <= 10) {
            awardBadge(userId, "TOP_10_RANK", "Top 10 Rank", "Reached the top 10 on the leaderboard", "🏆");
        }

        // First Contest
        long contestCount = contestParticipantRepository.findByUserIdOrderByJoinedAtDesc(userId).size();
        if (contestCount >= 1) {
            awardBadge(userId, "FIRST_CONTEST", "First Contest", "Participated in your first contest", "⚡");
        }
    }

    private void awardBadge(Long userId, String badgeType, String title, String description, String icon) {
        if (!userBadgeRepository.existsByUserIdAndBadgeType(userId, badgeType)) {
            UserBadge badge = UserBadge.builder()
                    .userId(userId)
                    .badgeType(badgeType)
                    .title(title)
                    .description(description)
                    .icon(icon)
                    .build();
            userBadgeRepository.save(badge);
        }
    }

    public List<UserBadge> getUserBadges(Long userId) {
        return userBadgeRepository.findByUserIdOrderByUnlockedAtDesc(userId);
    }
}
