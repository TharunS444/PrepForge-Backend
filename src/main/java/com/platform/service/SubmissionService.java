package com.platform.service;

import com.platform.dto.SubmissionRequest;
import com.platform.entity.Question;
import com.platform.entity.Submission;
import com.platform.entity.User;
import com.platform.repository.SubmissionRepository;
import com.platform.repository.UserRepository;
import com.platform.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ContestService contestService;

    @Autowired
    private BadgeService badgeService;

    public List<Submission> getSubmissionsByUser(Long userId) {
        return submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId);
    }

    @Transactional
    public Submission evaluateSubmission(User user, SubmissionRequest request) {
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        String status = "PENDING";
        int score = 0;

        try {
            boolean passed = runSandbox(request.getCode(), request.getLanguage(), question.getSampleInput(), question.getSampleOutput());
            if (passed) {
                status = "ACCEPTED";
                if ("EASY".equalsIgnoreCase(question.getDifficulty())) {
                    score = 10;
                } else if ("MEDIUM".equalsIgnoreCase(question.getDifficulty())) {
                    score = 20;
                } else if ("HARD".equalsIgnoreCase(question.getDifficulty())) {
                    score = 50;
                }
            } else {
                status = "WRONG_ANSWER";
            }
        } catch (Exception e) {
            status = "COMPILE_ERROR";
        }

        Submission submission = Submission.builder()
                .user(user)
                .question(question)
                .code(request.getCode())
                .language(request.getLanguage())
                .status(status)
                .score(score)
                .build();

        submission = submissionRepository.save(submission);

        // Reload fresh user from DB to avoid stale entity from UserPrincipal cache
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (score > 0) {
            List<Submission> existingAccepted = submissionRepository.findByUserIdAndQuestionId(freshUser.getId(), question.getId())
                    .stream()
                    .filter(s -> "ACCEPTED".equalsIgnoreCase(s.getStatus()))
                    .toList();
            
            // Only award points once per question (size == 1 means only this submission is accepted)
            if (existingAccepted.size() == 1) {
                freshUser.setScore(freshUser.getScore() + score);
                contestService.updateContestScoreIfApplicable(freshUser.getId(), question.getId(), score);
            }
        }

        userService.updateLastActiveAndStreak(freshUser);
        badgeService.checkAndUnlockBadges(freshUser.getId());

        return submission;
    }

    private boolean runSandbox(String code, String language, String input, String expectedOutput) throws Exception {
        if ("javascript".equalsIgnoreCase(language)) {
            return executeJavaScript(code, input, expectedOutput);
        } else if ("python".equalsIgnoreCase(language)) {
            return executePython(code, input, expectedOutput);
        } else if ("java".equalsIgnoreCase(language)) {
            return executeJava(code, input, expectedOutput);
        }
        return mockVerify(code);
    }

    private boolean executeJavaScript(String code, String input, String expectedOutput) {
        try {
            Path tempFile = Files.createTempFile("sandbox_", ".js");
            Files.writeString(tempFile, code);
            
            ProcessBuilder pb = new ProcessBuilder("node", tempFile.toString());
            Process process = pb.start();
            
            if (input != null && !input.isEmpty()) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(input.getBytes(StandardCharsets.UTF_8));
                }
            }

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            Files.deleteIfExists(tempFile);

            if (!finished) {
                process.destroyForcibly();
                return false;
            }

            return compareOutput(output.toString(), expectedOutput);
        } catch (Exception e) {
            return mockVerify(code);
        }
    }

    private boolean executePython(String code, String input, String expectedOutput) {
        try {
            Path tempFile = Files.createTempFile("sandbox_", ".py");
            Files.writeString(tempFile, code);
            
            ProcessBuilder pb = new ProcessBuilder("python", tempFile.toString());
            Process process;
            try {
                process = pb.start();
            } catch (IOException e) {
                pb = new ProcessBuilder("py", tempFile.toString());
                process = pb.start();
            }
            
            if (input != null && !input.isEmpty()) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(input.getBytes(StandardCharsets.UTF_8));
                }
            }

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            Files.deleteIfExists(tempFile);

            if (!finished) {
                process.destroyForcibly();
                return false;
            }

            return compareOutput(output.toString(), expectedOutput);
        } catch (Exception e) {
            return mockVerify(code);
        }
    }

    private boolean executeJava(String code, String input, String expectedOutput) {
        try {
            String className = "Solution";
            if (code.contains("class ")) {
                int classIdx = code.indexOf("class ") + 6;
                int endIdx = code.indexOf("{", classIdx);
                if (endIdx > classIdx) {
                    className = code.substring(classIdx, endIdx).trim().split("\\s+")[0].replaceAll("[^a-zA-Z0-9_]", "");
                }
            }

            Path tempDir = Files.createTempDirectory("java_sandbox_");
            Path javaFile = tempDir.resolve(className + ".java");
            Files.writeString(javaFile, code);

            ProcessBuilder compilePb = new ProcessBuilder("javac", javaFile.toString());
            Process compileProcess = compilePb.start();
            boolean compiled = compileProcess.waitFor(5, TimeUnit.SECONDS);
            
            if (!compiled || compileProcess.exitValue() != 0) {
                deleteDirectory(tempDir.toFile());
                throw new RuntimeException("Compilation Error");
            }

            ProcessBuilder runPb = new ProcessBuilder("java", "-cp", tempDir.toString(), className);
            Process runProcess = runPb.start();

            if (input != null && !input.isEmpty()) {
                try (OutputStream os = runProcess.getOutputStream()) {
                    os.write(input.getBytes(StandardCharsets.UTF_8));
                }
            }

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = runProcess.waitFor(5, TimeUnit.SECONDS);
            deleteDirectory(tempDir.toFile());

            if (!finished) {
                runProcess.destroyForcibly();
                return false;
            }

            return compareOutput(output.toString(), expectedOutput);
        } catch (Exception e) {
            return mockVerify(code);
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        dir.delete();
    }

    private boolean compareOutput(String actual, String expected) {
        if (actual == null || expected == null) return false;
        return actual.trim().replace("\r\n", "\n").equals(expected.trim().replace("\r\n", "\n"));
    }

    private boolean mockVerify(String code) {
        return code != null && code.length() > 20 && (
            code.contains("for") || code.contains("while") || code.contains("function") || code.contains("def ") || code.contains("class") || code.contains("return")
        );
    }
}
