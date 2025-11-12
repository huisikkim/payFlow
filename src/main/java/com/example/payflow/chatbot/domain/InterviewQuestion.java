package com.example.payflow.chatbot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @Column(nullable = false)
    private Integer questionNumber;

    @Column(nullable = false, length = 1000)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionCategory category;

    @Column(length = 2000)
    private String userAnswer;

    private Integer score; // 0-100

    @Column(length = 1000)
    private String feedback;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime answeredAt;

    public InterviewQuestion(Integer questionNumber, String question, QuestionCategory category) {
        this.questionNumber = questionNumber;
        this.question = question;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    public void setInterview(Interview interview) {
        this.interview = interview;
    }

    public void answerQuestion(String answer, Integer score, String feedback) {
        this.userAnswer = answer;
        this.score = score;
        this.feedback = feedback;
        this.answeredAt = LocalDateTime.now();
    }

    public boolean isAnswered() {
        return userAnswer != null && !userAnswer.isEmpty();
    }
}
