package com.platform.controller;

import com.platform.entity.Question;
import com.platform.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/generate-question")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Question> generateQuestion(@RequestBody Map<String, String> request) {
        String topic = request.getOrDefault("topic", "Algorithms");
        String difficulty = request.getOrDefault("difficulty", "MEDIUM");
        return ResponseEntity.ok(aiService.generateQuestion(topic, difficulty));
    }

    @PostMapping("/interview/analyze")
    public ResponseEntity<Map<String, String>> analyzeInterview(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String response = request.get("response");
        String feedback = aiService.analyzeInterviewResponse(question, response);
        return ResponseEntity.ok(Map.of("feedback", feedback));
    }
}
