package com.platform.service;

import com.platform.dto.LeaderboardEntryDTO;
import com.platform.entity.User;
import com.platform.repository.SubmissionRepository;
import com.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    public List<LeaderboardEntryDTO> getLeaderboard() {
        List<User> students = userRepository.findByRoleOrderByScoreDesc("ROLE_STUDENT");
        return students.stream().map(user -> {
            long totalSolved = submissionRepository.countByUserIdAndStatus(user.getId(), "ACCEPTED");
            return new LeaderboardEntryDTO(
                user.getId(),
                user.getName(),
                user.getScore(),
                user.getStreak(),
                totalSolved
            );
        }).collect(Collectors.toList());
    }
}
