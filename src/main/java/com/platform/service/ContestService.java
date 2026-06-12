package com.platform.service;

import com.platform.dto.ContestRequest;
import com.platform.entity.Contest;
import com.platform.entity.ContestParticipant;
import com.platform.entity.Question;
import com.platform.entity.User;
import com.platform.repository.ContestParticipantRepository;
import com.platform.repository.ContestRepository;
import com.platform.repository.QuestionRepository;
import com.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContestService {

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private ContestParticipantRepository participantRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    public Contest createContest(ContestRequest req) {
        Contest contest = new Contest();
        contest.setTitle(req.getTitle());
        contest.setStartTime(req.getStartTime());
        contest.setEndTime(req.getEndTime());
        contest.setDurationMinutes(req.getDurationMinutes());

        if (req.getQuestionIds() != null && !req.getQuestionIds().isEmpty()) {
            List<Question> questions = questionRepository.findAllById(req.getQuestionIds());
            contest.setQuestions(questions);
        }

        return contestRepository.save(contest);
    }

    public List<Contest> getAllContests() {
        return contestRepository.findAll();
    }

    public Contest getContestById(Long id) {
        return contestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contest not found"));
    }

    @Autowired
    private BadgeService badgeService;

    public ContestParticipant joinContest(Long contestId, Long userId) {
        Contest contest = getContestById(contestId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (participantRepository.findByContestIdAndUserId(contestId, userId).isPresent()) {
            throw new RuntimeException("User already joined this contest");
        }

        ContestParticipant participant = new ContestParticipant();
        participant.setContest(contest);
        participant.setUser(user);
        participant.setScore(0);
        participant = participantRepository.save(participant);
        
        badgeService.checkAndUnlockBadges(userId);
        
        return participant;
    }

    public List<ContestParticipant> getContestLeaderboard(Long contestId) {
        return participantRepository.findByContestIdOrderByScoreDesc(contestId);
    }
    
    // Call this from SubmissionService when a user solves a question during a contest.
    public void updateContestScoreIfApplicable(Long userId, Long questionId, int points) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        // Find active contests containing this question (within start/end time)
        List<Contest> activeContests = contestRepository.findAll().stream()
            .filter(c -> c.getQuestions().stream().anyMatch(q -> q.getId().equals(questionId)))
            .filter(c -> c.getStartTime() != null && c.getEndTime() != null
                      && !now.isBefore(c.getStartTime()) && now.isBefore(c.getEndTime()))
            .collect(Collectors.toList());
            
        for (Contest c : activeContests) {
            participantRepository.findByContestIdAndUserId(c.getId(), userId).ifPresent(p -> {
                p.setScore(p.getScore() + points);
                p.setQuestionsSolved(p.getQuestionsSolved() + 1);
                participantRepository.save(p);
            });
        }
    }
}
