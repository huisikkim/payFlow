package com.example.payflow.hr.application;

import com.example.payflow.hr.domain.*;
import com.example.payflow.hr.presentation.dto.LeaveRequest;
import com.example.payflow.hr.presentation.dto.LeaveResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveService {
    
    private final LeaveRepository leaveRepository;
    
    @Transactional
    public LeaveResponse applyLeave(String userId, LeaveRequest request) {
        Leave leave = Leave.create(
            userId,
            request.getType(),
            request.getStartDate(),
            request.getEndDate(),
            request.getDays(),
            request.getReason()
        );
        
        Leave saved = leaveRepository.save(leave);
        return LeaveResponse.from(saved);
    }
    
    @Transactional
    public LeaveResponse approveLeave(Long leaveId, String approverId) {
        Leave leave = leaveRepository.findById(leaveId)
            .orElseThrow(() -> new IllegalArgumentException("휴가 신청을 찾을 수 없습니다."));
        
        leave.approve(approverId);
        
        return LeaveResponse.from(leave);
    }
    
    @Transactional
    public LeaveResponse rejectLeave(Long leaveId, String approverId, String reason) {
        Leave leave = leaveRepository.findById(leaveId)
            .orElseThrow(() -> new IllegalArgumentException("휴가 신청을 찾을 수 없습니다."));
        
        leave.reject(approverId, reason);
        
        return LeaveResponse.from(leave);
    }
    
    @Transactional
    public void cancelLeave(Long leaveId, String userId) {
        Leave leave = leaveRepository.findById(leaveId)
            .orElseThrow(() -> new IllegalArgumentException("휴가 신청을 찾을 수 없습니다."));
        
        if (!leave.getUserId().equals(userId)) {
            throw new IllegalStateException("본인의 휴가만 취소할 수 있습니다.");
        }
        
        leave.cancel();
    }
    
    public List<LeaveResponse> getMyLeaves(String userId) {
        return leaveRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(LeaveResponse::from)
            .collect(Collectors.toList());
    }
    
    public List<LeaveResponse> getPendingLeaves() {
        return leaveRepository.findByStatusOrderByCreatedAtDesc(LeaveStatus.PENDING)
            .stream()
            .map(LeaveResponse::from)
            .collect(Collectors.toList());
    }
    
    public Integer getRemainingLeaveDays(String userId) {
        int currentYear = LocalDate.now().getYear();
        int usedDays = leaveRepository.countUsedLeaveDays(userId, currentYear);
        int totalDays = 15; // 기본 연차 15일
        return totalDays - usedDays;
    }
}
