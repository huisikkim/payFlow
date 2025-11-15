package com.example.payflow.hr.application;

import com.example.payflow.hr.domain.*;
import com.example.payflow.hr.presentation.dto.AttendanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {
    
    private final AttendanceRepository attendanceRepository;
    
    @Transactional
    public AttendanceResponse checkIn(String userId) {
        // 이미 출근한 상태인지 확인
        attendanceRepository.findActiveCheckIn(userId)
            .ifPresent(a -> {
                throw new IllegalStateException("이미 출근 처리되었습니다.");
            });
        
        Attendance attendance = Attendance.checkIn(userId);
        Attendance saved = attendanceRepository.save(attendance);
        
        return AttendanceResponse.from(saved);
    }
    
    @Transactional
    public AttendanceResponse checkOut(String userId) {
        Attendance attendance = attendanceRepository.findActiveCheckIn(userId)
            .orElseThrow(() -> new IllegalStateException("출근 기록이 없습니다."));
        
        attendance.checkOut();
        
        return AttendanceResponse.from(attendance);
    }
    
    public List<AttendanceResponse> getMyAttendances(String userId) {
        return attendanceRepository.findByUserIdOrderByCheckInTimeDesc(userId)
            .stream()
            .map(AttendanceResponse::from)
            .collect(Collectors.toList());
    }
    
    public List<AttendanceResponse> getMonthlyAttendances(String userId, int year, int month) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);
        
        return attendanceRepository.findByUserIdAndDateRange(userId, startDate, endDate)
            .stream()
            .map(AttendanceResponse::from)
            .collect(Collectors.toList());
    }
    
    public AttendanceResponse getTodayAttendance(String userId) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        return attendanceRepository.findByUserIdAndDateRange(userId, startOfDay, endOfDay)
            .stream()
            .findFirst()
            .map(AttendanceResponse::from)
            .orElse(null);
    }
}
