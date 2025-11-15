package com.example.payflow.hr.presentation.dto;

import com.example.payflow.hr.domain.Attendance;
import com.example.payflow.hr.domain.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AttendanceResponse {
    private Long id;
    private String userId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private AttendanceStatus status;
    private String notes;
    
    public static AttendanceResponse from(Attendance attendance) {
        return new AttendanceResponse(
            attendance.getId(),
            attendance.getUserId(),
            attendance.getCheckInTime(),
            attendance.getCheckOutTime(),
            attendance.getStatus(),
            attendance.getNotes()
        );
    }
}
