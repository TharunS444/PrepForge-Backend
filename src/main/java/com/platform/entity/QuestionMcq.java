package com.platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions_mcq")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionMcq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false)
    private String optionA;

    @Column(nullable = false)
    private String optionB;

    @Column(nullable = false)
    private String optionC;

    @Column(nullable = false)
    private String optionD;

    @Column(nullable = false)
    private String correctOption; // "A", "B", "C", "D"

    @Column(nullable = false)
    private Integer marks = 1;
}
