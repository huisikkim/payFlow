package com.example.payflow.hr.presentation;

import com.example.payflow.hr.application.AttendanceService;
import com.example.payflow.hr.presentation.dto.AttendanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    
    private final AttendanceService attendanceService;
    
    @PostMapping("/check-in")
    public ResponseEntity<AttendanceResponse> checkIn(Authentication auth) {
        String userId = auth.getName();
        AttendanceResponse response = attendanceService.checkIn(userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/check-out")
    public ResponseEntity<AttendanceResponse> checkOut(Authentication auth) {
        String userId = auth.getName();
        AttendanceResponse response = attendanceService.checkOut(userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<AttendanceResponse>> getMyAttendances(Authentication auth) {
        String userId = auth.getName();
        List<AttendanceResponse> attendances = attendanceService.getMyAttendances(userId);
        return ResponseEntity.ok(attendances);
    }
    
    @GetMapping("/today")
    public ResponseEntity<AttendanceResponse> getTodayAttendance(Authentication auth) {
        String userId = auth.getName();
        AttendanceResponse attendance = attendanceService.getTodayAttendance(userId);
        return ResponseEntity.ok(attendance);
    }
    
    @GetMapping("/monthly")
    public ResponseEntity<List<AttendanceResponse>> getMonthlyAttendances(
            Authentication auth,
            @RequestParam int year,
            @RequestParam int month) {
        String userId = auth.getName();
        List<AttendanceResponse> attendances = attendanceService.getMonthlyAttendances(userId, year, month);
        return ResponseEntity.ok(attendances);
    }
}
