package com.platform.service;

import com.platform.dto.SubmissionRequest;
import com.platform.entity.Question;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AIService {

    public Question generateQuestion(String topic, String difficulty) {
        // In a real app, this would call Gemini/OpenAI API.
        // For MVP, we mock realistic responses.
        
        Question q = new Question();
        q.setTitle("AI Generated: " + topic + " Challenge");
        q.setDifficulty(difficulty.toUpperCase());
        q.setCompanyTags("AI Generated, " + topic);
        q.setDescription("This is an AI generated question about " + topic + ".\n\n" +
                "Write a function to solve the core problem described in standard " + topic + " implementations.\n\n" +
                "### Example 1\n**Input:** data = [1,2,3]\n**Output:** [3,2,1]");
        q.setSampleInput("1,2,3");
        q.setSampleOutput("3,2,1");
        
        return q;
    }

    public String analyzeInterviewResponse(String question, String userResponse) {
        // Real implementation calls LLM.
        if (userResponse == null || userResponse.trim().isEmpty()) {
            return "You didn't provide an answer. Please elaborate on your thought process.";
        }
        if (userResponse.length() < 20) {
            return "Your answer is quite brief. In a real interview, you should explain the 'why' and discuss trade-offs.";
        }
        return "Good explanation. You touched upon the core concepts. To improve, try mentioning edge cases (like null inputs or empty collections) and time/space complexity O(N).";
    }
}
