package com.example.payflow.chatbot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long conversationId;

    @Column(nullable = false)
    private Long jobId;

    @Column(nullable = false)
    private String userId;

    @ElementCollection
    @CollectionTable(name = "user_tech_stacks", joinColumns = @JoinColumn(name = "interview_id"))
    @Column(name = "tech_stack")
    private List<String> userTechStacks = new ArrayList<>();

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewQuestion> questions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status;

    private Integer totalScore;
    private Double passRate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Interview(Long conversationId, Long jobId, String userId, List<String> techStacks) {
        this.conversationId = conversationId;
        this.jobId = jobId;
        this.userId = userId;
        this.userTechStacks = new ArrayList<>(techStacks);
        this.status = InterviewStatus.IN_PROGRESS;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addQuestion(InterviewQuestion question) {
        questions.add(question);
        question.setInterview(this);
        this.updatedAt = LocalDateTime.now();
    }

    public void complete(Integer totalScore, Double passRate) {
        this.status = InterviewStatus.COMPLETED;
        this.totalScore = totalScore;
        this.passRate = passRate;
        this.updatedAt = LocalDateTime.now();
    }

    public int getAnsweredQuestionsCount() {
        return (int) questions.stream()
            .filter(q -> q.getUserAnswer() != null && !q.getUserAnswer().isEmpty())
            .count();
    }

    public boolean isCompleted() {
        return status == InterviewStatus.COMPLETED;
    }
}
