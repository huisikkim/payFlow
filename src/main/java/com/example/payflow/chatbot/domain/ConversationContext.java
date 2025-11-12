package com.example.payflow.chatbot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    public void reset() {
        this.currentStep = ConversationStep.INITIAL;
        this.selectedRegion = null;
        this.selectedIndustry = null;
        this.minSalary = null;
        this.maxSalary = null;
        this.updatedAt = LocalDateTime.now();
    }
}
