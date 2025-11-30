package com.example.payflow.qualityissue.presentation.dto;

import com.example.payflow.qualityissue.domain.IssueType;
import com.example.payflow.qualityissue.domain.RequestAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitIssueRequest {
    private Long orderId;
    private Long itemId;
    private String itemName;
    private String storeId;
    private String storeName;
    private String distributorId;
    private IssueType issueType;
    private List<String> photoUrls;
    private String description;
    private RequestAction requestAction;
}
