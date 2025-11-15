package com.example.payflow.hr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private LocalDateTime checkInTime;
    
    private LocalDateTime checkOutTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;
    
    private String notes;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public static Attendance checkIn(String userId) {
        Attendance attendance = new Attendance();
        attendance.userId = userId;
        attendance.checkInTime = LocalDateTime.now();
        attendance.status = AttendanceStatus.CHECKED_IN;
        attendance.createdAt = LocalDateTime.now();
        return attendance;
    }
    
    public void checkOut() {
        if (this.status != AttendanceStatus.CHECKED_IN) {
            throw new IllegalStateException("출근 상태가 아닙니다.");
        }
        this.checkOutTime = LocalDateTime.now();
        this.status = AttendanceStatus.CHECKED_OUT;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addNotes(String notes) {
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }
}
