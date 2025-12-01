package com.example.payflow.qualityissue.presentation;

import com.example.payflow.qualityissue.application.QualityIssueService;
import com.example.payflow.qualityissue.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quality-issues")
@RequiredArgsConstructor
public class QualityIssueController {
    
    private final QualityIssueService qualityIssueService;
    
    /**
     * 품질 이슈 신고 (가게사장님)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<QualityIssueResponse> submitIssue(@RequestBody SubmitIssueRequest request) {
        QualityIssueResponse response = qualityIssueService.submitIssue(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 품질 이슈 상세 조회
     */
    @GetMapping("/{issueId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<QualityIssueResponse> getIssue(@PathVariable Long issueId) {
        QualityIssueResponse response = qualityIssueService.getIssue(issueId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 가게별 품질 이슈 목록 조회 (가게사장님)
     */
    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<List<QualityIssueResponse>> getStoreIssues(@PathVariable String storeId) {
        List<QualityIssueResponse> responses = qualityIssueService.getStoreIssues(storeId);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 유통사별 품질 이슈 목록 조회 (유통업자)
     */
    @GetMapping("/distributor/{distributorId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<List<QualityIssueResponse>> getDistributorIssues(@PathVariable String distributorId) {
        List<QualityIssueResponse> responses = qualityIssueService.getDistributorIssues(distributorId);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 대기 중인 품질 이슈 목록 조회 (유통업자)
     */
    @GetMapping("/distributor/{distributorId}/pending")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'STORE_OWNER', 'DISTRIBUTOR')")
    public ResponseEntity<List<QualityIssueResponse>> getPendingIssues(@PathVariable String distributorId) {
        List<QualityIssueResponse> responses = qualityIssueService.getPendingIssues(distributorId);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 품질 이슈 검토 시작 (유통업자)
     */
    @PostMapping("/{issueId}/review")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DISTRIBUTOR')")
    public ResponseEntity<QualityIssueResponse> startReview(@PathVariable Long issueId) {
        QualityIssueResponse response = qualityIssueService.startReview(issueId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 품질 이슈 승인 (유통업자)
     */
    @PostMapping("/{issueId}/approve")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DISTRIBUTOR')")
    public ResponseEntity<QualityIssueResponse> approveIssue(
            @PathVariable Long issueId,
            @RequestBody ReviewIssueRequest request) {
        QualityIssueResponse response = qualityIssueService.approveIssue(issueId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 품질 이슈 거절 (유통업자)
     */
    @PostMapping("/{issueId}/reject")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DISTRIBUTOR')")
    public ResponseEntity<QualityIssueResponse> rejectIssue(
            @PathVariable Long issueId,
            @RequestBody ReviewIssueRequest request) {
        QualityIssueResponse response = qualityIssueService.rejectIssue(issueId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 수거 예약 (유통업자)
     */
    @PostMapping("/{issueId}/schedule-pickup")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DISTRIBUTOR')")
    public ResponseEntity<QualityIssueResponse> schedulePickup(
            @PathVariable Long issueId,
            @RequestBody SchedulePickupRequest request) {
        QualityIssueResponse response = qualityIssueService.schedulePickup(issueId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 수거 완료 (유통업자)
     */
    @PostMapping("/{issueId}/complete-pickup")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DISTRIBUTOR')")
    public ResponseEntity<QualityIssueResponse> completePickup(@PathVariable Long issueId) {
        QualityIssueResponse response = qualityIssueService.completePickup(issueId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 환불/교환 완료 (유통업자)
     */
    @PostMapping("/{issueId}/complete-resolution")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DISTRIBUTOR')")
    public ResponseEntity<QualityIssueResponse> completeResolution(
            @PathVariable Long issueId,
            @RequestBody CompleteResolutionRequest request) {
        QualityIssueResponse response = qualityIssueService.completeResolution(issueId, request);
        return ResponseEntity.ok(response);
    }
}
