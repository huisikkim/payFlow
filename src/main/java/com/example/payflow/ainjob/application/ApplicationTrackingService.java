package com.example.payflow.ainjob.application;

import com.example.payflow.ainjob.application.dto.ApplicationCreateRequest;
import com.example.payflow.ainjob.application.dto.ApplicationResponse;
import com.example.payflow.ainjob.application.dto.ApplicationStatusChangeRequest;
import com.example.payflow.ainjob.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationTrackingService {
    
    private final ApplicationTrackingRepository applicationTrackingRepository;
    private final ApplicantRepository applicantRepository;
    private final AinjobJobPostingRepository jobPostingRepository;
    
    @Transactional
    public ApplicationResponse createApplication(ApplicationCreateRequest request) {
        // 지원자 존재 확인
        applicantRepository.findById(request.getApplicantId())
            .orElseThrow(() -> new IllegalArgumentException("지원자를 찾을 수 없습니다: " + request.getApplicantId()));
        
        // 공고 존재 확인
        AinjobJobPosting jobPosting = jobPostingRepository.findById(request.getJobPostingId())
            .orElseThrow(() -> new IllegalArgumentException("채용 공고를 찾을 수 없습니다: " + request.getJobPostingId()));
        
        // 지원 가능 여부 확인
        if (!jobPosting.isApplicable()) {
            throw new IllegalStateException("지원 가능한 공고가 아닙니다.");
        }
        
        // 중복 지원 확인
        applicationTrackingRepository.findByApplicantIdAndJobPostingId(
            request.getApplicantId(), request.getJobPostingId()
        ).ifPresent(a -> {
            throw new IllegalArgumentException("이미 지원한 공고입니다.");
        });
        
        // Application 생성
        ApplicationTracking tracking = ApplicationTracking.create(
            request.getApplicantId(),
            request.getJobPostingId(),
            request.getResumeId()
        );
        
        ApplicationTracking saved = applicationTrackingRepository.save(tracking);
        return new ApplicationResponse(saved);
    }
    
    public ApplicationResponse getApplication(Long id) {
        ApplicationTracking tracking = applicationTrackingRepository.findByIdWithHistories(id)
            .orElseThrow(() -> new IllegalArgumentException("지원 내역을 찾을 수 없습니다: " + id));
        return new ApplicationResponse(tracking);
    }
    
    public List<ApplicationResponse> getApplicationsByApplicant(Long applicantId) {
        return applicationTrackingRepository.findByApplicantId(applicantId).stream()
            .map(ApplicationResponse::new)
            .collect(Collectors.toList());
    }
    
    public Page<ApplicationResponse> getApplicationsByJobPosting(Long jobPostingId, Pageable pageable) {
        return applicationTrackingRepository.findByJobPostingId(jobPostingId, pageable)
            .map(ApplicationResponse::new);
    }
    
    public Page<ApplicationResponse> getApplicationsByJobPostingAndStatus(
            Long jobPostingId, ApplicationStatus status, Pageable pageable) {
        return applicationTrackingRepository.findByJobPostingIdAndStatus(jobPostingId, status, pageable)
            .map(ApplicationResponse::new);
    }
    
    @Transactional
    public ApplicationResponse changeApplicationStatus(Long id, ApplicationStatusChangeRequest request) {
        ApplicationTracking tracking = applicationTrackingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("지원 내역을 찾을 수 없습니다: " + id));
        
        // 현재 상태 확인
        if (tracking.getStatus() != request.getFromStatus()) {
            throw new IllegalStateException(
                "현재 상태가 일치하지 않습니다. 현재: " + tracking.getStatus() + ", 요청: " + request.getFromStatus()
            );
        }
        
        tracking.changeStatus(request.getToStatus(), request.getReason(), "HR-USER");
        
        return new ApplicationResponse(tracking);
    }
}
