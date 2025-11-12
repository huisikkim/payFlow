package com.example.payflow.chatbot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversation_contexts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long conversationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationStep currentStep;

    private String selectedRegion;
    private String selectedIndustry;
    private Long minSalary;
    private Long maxSalary;

    private Long selectedJobId;
    
    @ElementCollection
    @CollectionTable(name = "user_tech_stacks_context", joinColumns = @JoinColumn(name = "context_id"))
    @Column(name = "tech_stack")
    private List<String> userTechStacks = new ArrayList<>();

    private Long currentInterviewId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public ConversationContext(Long conversationId) {
        this.conversationId = conversationId;
        this.currentStep = ConversationStep.INITIAL;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void moveToStep(ConversationStep step) {
        this.currentStep = step;
        this.updatedAt = LocalDateTime.now();
    }

    public void setSelectedRegion(String region) {
        this.selectedRegion = region;
        this.updatedAt = LocalDateTime.now();
    }

    public void setSelectedIndustry(String industry) {
        this.selectedIndustry = industry;
        this.updatedAt = LocalDateTime.now();
    }

    public void setSalaryRange(Long minSalary, Long maxSalary) {
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.updatedAt = LocalDateTime.now();
    }

    public void setSelectedJobId(Long jobId) {
        this.selectedJobId = jobId;
        this.updatedAt = LocalDateTime.now();
    }

    public void addTechStack(String techStack) {
        if (this.userTechStacks == null) {
            this.userTechStacks = new ArrayList<>();
        }
        this.userTechStacks.add(techStack);
        this.updatedAt = LocalDateTime.now();
    }

    public void setCurrentInterviewId(Long interviewId) {
        this.currentInterviewId = interviewId;
        this.updatedAt = LocalDateTime.now();
    }

    public void reset() {
        this.currentStep = ConversationStep.INITIAL;
        this.selectedRegion = null;
        this.selectedIndustry = null;
        this.minSalary = null;
        this.maxSalary = null;
        this.selectedJobId = null;
        this.userTechStacks = new ArrayList<>();
        this.currentInterviewId = null;
        this.updatedAt = LocalDateTime.now();
    }
}
