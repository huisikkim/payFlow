package com.example.payflow.hr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaves")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Leave {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Column(nullable = false)
    private Integer days;
    
    @Column(nullable = false, length = 500)
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;
    
    private String approverId;
    
    private String rejectionReason;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public static Leave create(String userId, LeaveType type, LocalDate startDate, 
                               LocalDate endDate, Integer days, String reason) {
        Leave leave = new Leave();
        leave.userId = userId;
        leave.type = type;
        leave.startDate = startDate;
        leave.endDate = endDate;
        leave.days = days;
        leave.reason = reason;
        leave.status = LeaveStatus.PENDING;
        leave.createdAt = LocalDateTime.now();
        return leave;
    }
    
    public void approve(String approverId) {
        if (this.status != LeaveStatus.PENDING) {
            throw new IllegalStateException("대기 중인 휴가 신청만 승인할 수 있습니다.");
        }
        this.status = LeaveStatus.APPROVED;
        this.approverId = approverId;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void reject(String approverId, String reason) {
        if (this.status != LeaveStatus.PENDING) {
            throw new IllegalStateException("대기 중인 휴가 신청만 반려할 수 있습니다.");
        }
        this.status = LeaveStatus.REJECTED;
        this.approverId = approverId;
        this.rejectionReason = reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        if (this.status == LeaveStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 휴가입니다.");
        }
        this.status = LeaveStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
}
