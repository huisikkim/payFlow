package com.example.payflow.qualityissue.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quality_issues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QualityIssue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long orderId;
    
    @Column(nullable = false)
    private Long itemId;
    
    @Column(nullable = false)
    private String itemName;
    
    @Column(nullable = false)
    private String storeId;
    
    @Column(nullable = false)
    private String storeName;
    
    @Column(nullable = false)
    private String distributorId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueType issueType;
    
    @ElementCollection
    @CollectionTable(name = "quality_issue_photos", joinColumns = @JoinColumn(name = "issue_id"))
    @Column(name = "photo_url", length = 2000)
    private List<String> photoUrls = new ArrayList<>();
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestAction requestAction;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;
    
    private LocalDateTime submittedAt;
    
    private LocalDateTime reviewedAt;
    
    private String reviewerComment;
    
    private LocalDateTime pickupScheduledAt;
    
    private LocalDateTime resolvedAt;
    
    private String resolutionNote;
    
    public QualityIssue(Long orderId, Long itemId, String itemName, String storeId, String storeName,
                        String distributorId, IssueType issueType, List<String> photoUrls,
                        String description, RequestAction requestAction) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.storeId = storeId;
        this.storeName = storeName;
        this.distributorId = distributorId;
        this.issueType = issueType;
        this.photoUrls = photoUrls != null ? new ArrayList<>(photoUrls) : new ArrayList<>();
        this.description = description;
        this.requestAction = requestAction;
        this.status = IssueStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }
    
    public void startReview() {
        this.status = IssueStatus.REVIEWING;
        this.reviewedAt = LocalDateTime.now();
    }
    
    public void approve(String comment) {
        this.status = IssueStatus.APPROVED;
        this.reviewerComment = comment;
        this.reviewedAt = LocalDateTime.now();
    }
    
    public void reject(String comment) {
        this.status = IssueStatus.REJECTED;
        this.reviewerComment = comment;
        this.reviewedAt = LocalDateTime.now();
        this.resolvedAt = LocalDateTime.now();
    }
    
    public void schedulePickup(LocalDateTime pickupTime) {
        if (this.status != IssueStatus.APPROVED) {
            throw new IllegalStateException("승인된 이슈만 수거 예약이 가능합니다.");
        }
        this.status = IssueStatus.PICKUP_SCHEDULED;
        this.pickupScheduledAt = pickupTime;
    }
    
    public void completePickup() {
        if (this.status != IssueStatus.PICKUP_SCHEDULED) {
            throw new IllegalStateException("수거 예정 상태에서만 수거 완료 처리가 가능합니다.");
        }
        this.status = IssueStatus.PICKED_UP;
    }
    
    public void completeRefund(String note) {
        if (this.requestAction != RequestAction.REFUND) {
            throw new IllegalStateException("환불 요청이 아닙니다.");
        }
        this.status = IssueStatus.REFUNDED;
        this.resolutionNote = note;
        this.resolvedAt = LocalDateTime.now();
    }
    
    public void completeExchange(String note) {
        if (this.requestAction != RequestAction.EXCHANGE) {
            throw new IllegalStateException("교환 요청이 아닙니다.");
        }
        this.status = IssueStatus.EXCHANGED;
        this.resolutionNote = note;
        this.resolvedAt = LocalDateTime.now();
    }
}
