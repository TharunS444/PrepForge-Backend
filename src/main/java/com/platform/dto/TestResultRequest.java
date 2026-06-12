package com.platform.dto;

import lombok.Data;
import java.util.List;

@Data
public class TestResultRequest {
    private List<AnswerSelection> answers;

    @Data
    public static class AnswerSelection {
        private Long questionId;
        private String selectedOption; // "A", "B", "C", "D"
    }
}
