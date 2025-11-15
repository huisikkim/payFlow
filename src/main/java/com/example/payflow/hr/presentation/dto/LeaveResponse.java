package com.example.payflow.hr.presentation.dto;

import com.example.payflow.hr.domain.Leave;
import com.example.payflow.hr.domain.LeaveStatus;
import com.example.payflow.hr.domain.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LeaveResponse {
    private Long id;
    private String userId;
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer days;
    private String reason;
    private LeaveStatus status;
    private String approverId;
    private String rejectionReason;
    private LocalDateTime createdAt;
    
    public static LeaveResponse from(Leave leave) {
        return new LeaveResponse(
            leave.getId(),
            leave.getUserId(),
            leave.getType(),
            leave.getStartDate(),
            leave.getEndDate(),
            leave.getDays(),
            leave.getReason(),
            leave.getStatus(),
            leave.getApproverId(),
            leave.getRejectionReason(),
            leave.getCreatedAt()
        );
    }
}
