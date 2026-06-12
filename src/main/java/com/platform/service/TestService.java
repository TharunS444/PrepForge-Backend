package com.platform.service;

import com.platform.dto.TestResultRequest;
import com.platform.entity.QuestionMcq;
import com.platform.entity.Result;
import com.platform.entity.Test;
import com.platform.entity.User;
import com.platform.exception.ResourceNotFoundException;
import com.platform.repository.QuestionMcqRepository;
import com.platform.repository.ResultRepository;
import com.platform.repository.TestRepository;
import com.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TestService {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionMcqRepository questionMcqRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    public List<Test> getAllTests() {
        return testRepository.findAll();
    }

    public Test getTestById(Long id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + id));
    }

    public List<QuestionMcq> getQuestionsByTestId(Long testId) {
        // Ensure test exists
        getTestById(testId);
        return questionMcqRepository.findByTestId(testId);
    }

    @Transactional
    public Test createTest(Test test) {
        return testRepository.save(test);
    }

    @Transactional
    public QuestionMcq addQuestionToTest(Long testId, QuestionMcq question) {
        Test test = getTestById(testId);
        question.setTest(test);
        return questionMcqRepository.save(question);
    }

    @Transactional
    public Result submitTestResult(User user, Long testId, TestResultRequest request) {
        Test test = getTestById(testId);
        List<QuestionMcq> questions = questionMcqRepository.findByTestId(testId);
        
        Map<Long, QuestionMcq> questionMap = questions.stream()
                .collect(Collectors.toMap(QuestionMcq::getId, q -> q));

        int score = 0;
        int totalMarks = 0;

        for (QuestionMcq q : questions) {
            totalMarks += q.getMarks();
        }

        if (request.getAnswers() != null) {
            for (TestResultRequest.AnswerSelection answer : request.getAnswers()) {
                QuestionMcq question = questionMap.get(answer.getQuestionId());
                if (question != null) {
                    if (question.getCorrectOption().equalsIgnoreCase(answer.getSelectedOption())) {
                        score += question.getMarks();
                    }
                }
            }
        }

        Result result = Result.builder()
                .user(user)
                .test(test)
                .score(score)
                .totalMarks(totalMarks)
                .completedAt(LocalDateTime.now())
                .build();

        result = resultRepository.save(result);

        // Update user score
        // Check if user already took this test to prevent double scoring
        List<Result> existingResults = resultRepository.findByUserIdAndTestId(user.getId(), test.getId());
        if (existingResults.size() <= 1) { // includes the current one just saved
            user.setScore(user.getScore() + score);
        }

        // Update activity streak
        userService.updateLastActiveAndStreak(user);
        userRepository.save(user);

        return result;
    }

    public List<Result> getResultsByUser(Long userId) {
        return resultRepository.findByUserIdOrderByCompletedAtDesc(userId);
    }
}
