package com.example.payflow.qualityissue.presentation.dto;

import com.example.payflow.qualityissue.domain.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class QualityIssueResponse {
    private Long id;
    private Long orderId;
    private Long itemId;
    private String itemName;
    private String storeId;
    private String storeName;
    private String distributorId;
    private IssueType issueType;
    private String issueTypeDescription;
    private List<String> photoUrls;
    private String description;
    private RequestAction requestAction;
    private String requestActionDescription;
    private IssueStatus status;
    private String statusDescription;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewerComment;
    private LocalDateTime pickupScheduledAt;
    private LocalDateTime resolvedAt;
    private String resolutionNote;
    
    public static QualityIssueResponse from(QualityIssue issue) {
        return new QualityIssueResponse(
                issue.getId(),
                issue.getOrderId(),
                issue.getItemId(),
                issue.getItemName(),
                issue.getStoreId(),
                issue.getStoreName(),
                issue.getDistributorId(),
                issue.getIssueType(),
                issue.getIssueType().getDescription(),
                issue.getPhotoUrls(),
                issue.getDescription(),
                issue.getRequestAction(),
                issue.getRequestAction().getDescription(),
                issue.getStatus(),
                issue.getStatus().getDescription(),
                issue.getSubmittedAt(),
                issue.getReviewedAt(),
                issue.getReviewerComment(),
                issue.getPickupScheduledAt(),
                issue.getResolvedAt(),
                issue.getResolutionNote()
        );
    }
}
