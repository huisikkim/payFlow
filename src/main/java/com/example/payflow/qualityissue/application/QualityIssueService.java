package com.example.payflow.qualityissue.application;

import com.example.payflow.qualityissue.domain.*;
import com.example.payflow.qualityissue.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QualityIssueService {
    
    private final QualityIssueRepository qualityIssueRepository;
    
    @Transactional
    public QualityIssueResponse submitIssue(SubmitIssueRequest request) {
        log.info("품질 이슈 신고: orderId={}, itemId={}, issueType={}", 
                request.getOrderId(), request.getItemId(), request.getIssueType());
        
        QualityIssue issue = new QualityIssue(
                request.getOrderId(),
                request.getItemId(),
                request.getItemName(),
                request.getStoreId(),
                request.getStoreName(),
                request.getDistributorId(),
                request.getIssueType(),
                request.getPhotoUrls(),
                request.getDescription(),
                request.getRequestAction()
        );
        
        QualityIssue saved = qualityIssueRepository.save(issue);
        
        log.info("품질 이슈 신고 완료: issueId={}", saved.getId());
        
        return QualityIssueResponse.from(saved);
    }
    
    public QualityIssueResponse getIssue(Long issueId) {
        QualityIssue issue = qualityIssueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("품질 이슈를 찾을 수 없습니다: " + issueId));
        return QualityIssueResponse.from(issue);
    }
    
    public List<QualityIssueResponse> getStoreIssues(String storeId) {
        return qualityIssueRepository.findByStoreIdOrderBySubmittedAtDesc(storeId)
                .stream()
                .map(QualityIssueResponse::from)
                .collect(Collectors.toList());
    }
    
    public List<QualityIssueResponse> getDistributorIssues(String distributorId) {
        return qualityIssueRepository.findByDistributorIdOrderBySubmittedAtDesc(distributorId)
                .stream()
                .map(QualityIssueResponse::from)
                .collect(Collectors.toList());
    }
    
    public List<QualityIssueResponse> getPendingIssues(String distributorId) {
        return qualityIssueRepository.findByDistributorIdAndStatus(distributorId, IssueStatus.SUBMITTED)
                .stream()
                .map(QualityIssueResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public QualityIssueResponse startReview(Long issueId) {
        QualityIssue issue = qualityIssueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("품질 이슈를 찾을 수 없습니다: " + issueId));
        
        issue.startReview();
        log.info("품질 이슈 검토 시작: issueId={}", issueId);
        
        return QualityIssueResponse.from(issue);
    }
    
    @Transactional
    public QualityIssueResponse approveIssue(Long issueId, ReviewIssueRequest request) {
        QualityIssue issue = qualityIssueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("품질 이슈를 찾을 수 없습니다: " + issueId));
        
        issue.approve(request.getComment());
        log.info("품질 이슈 승인: issueId={}, comment={}", issueId, request.getComment());
        
        return QualityIssueResponse.from(issue);
    }
    
    @Transactional
    public QualityIssueResponse rejectIssue(Long issueId, ReviewIssueRequest request) {
        QualityIssue issue = qualityIssueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("품질 이슈를 찾을 수 없습니다: " + issueId));
        
        issue.reject(request.getComment());
        log.info("품질 이슈 거절: issueId={}, comment={}", issueId, request.getComment());
        
        return QualityIssueResponse.from(issue);
    }
    
    @Transactional
    public QualityIssueResponse schedulePickup(Long issueId, SchedulePickupRequest request) {
        QualityIssue issue = qualityIssueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("품질 이슈를 찾을 수 없습니다: " + issueId));
        
        issue.schedulePickup(request.getPickupTime());
        log.info("수거 예약: issueId={}, pickupTime={}", issueId, request.getPickupTime());
        
        return QualityIssueResponse.from(issue);
    }
    
    @Transactional
    public QualityIssueResponse completePickup(Long issueId) {
        QualityIssue issue = qualityIssueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("품질 이슈를 찾을 수 없습니다: " + issueId));
        
        issue.completePickup();
        log.info("수거 완료: issueId={}", issueId);
        
        return QualityIssueResponse.from(issue);
    }
    
    @Transactional
    public QualityIssueResponse completeResolution(Long issueId, CompleteResolutionRequest request) {
        QualityIssue issue = qualityIssueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("품질 이슈를 찾을 수 없습니다: " + issueId));
        
        if (issue.getRequestAction() == RequestAction.REFUND) {
            issue.completeRefund(request.getNote());
            log.info("환불 완료: issueId={}", issueId);
        } else {
            issue.completeExchange(request.getNote());
            log.info("교환 완료: issueId={}", issueId);
        }
        
        return QualityIssueResponse.from(issue);
    }
}
