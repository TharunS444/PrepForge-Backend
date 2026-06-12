package com.platform.dto;

import lombok.Data;

@Data
public class SubmissionRequest {
    private Long questionId;
    private String code;
    private String language; // "java", "javascript", "python"
}
