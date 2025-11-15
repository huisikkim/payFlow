package com.example.payflow.hr.presentation;

import com.example.payflow.hr.application.LeaveService;
import com.example.payflow.hr.presentation.dto.LeaveRequest;
import com.example.payflow.hr.presentation.dto.LeaveResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hr/leaves")
@RequiredArgsConstructor
public class LeaveController {
    
    private final LeaveService leaveService;
    
    @PostMapping
    public ResponseEntity<LeaveResponse> applyLeave(
            Authentication auth,
            @Valid @RequestBody LeaveRequest request) {
        String userId = auth.getName();
        LeaveResponse response = leaveService.applyLeave(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{leaveId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveResponse> approveLeave(
            @PathVariable Long leaveId,
            Authentication auth) {
        String approverId = auth.getName();
        LeaveResponse response = leaveService.approveLeave(leaveId, approverId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{leaveId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveResponse> rejectLeave(
            @PathVariable Long leaveId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String approverId = auth.getName();
        String reason = body.get("reason");
        LeaveResponse response = leaveService.rejectLeave(leaveId, approverId, reason);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{leaveId}")
    public ResponseEntity<Void> cancelLeave(
            @PathVariable Long leaveId,
            Authentication auth) {
        String userId = auth.getName();
        leaveService.cancelLeave(leaveId, userId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<LeaveResponse>> getMyLeaves(Authentication auth) {
        String userId = auth.getName();
        List<LeaveResponse> leaves = leaveService.getMyLeaves(userId);
        return ResponseEntity.ok(leaves);
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveResponse>> getPendingLeaves() {
        List<LeaveResponse> leaves = leaveService.getPendingLeaves();
        return ResponseEntity.ok(leaves);
    }
    
    @GetMapping("/remaining-days")
    public ResponseEntity<Map<String, Integer>> getRemainingLeaveDays(Authentication auth) {
        String userId = auth.getName();
        Integer remainingDays = leaveService.getRemainingLeaveDays(userId);
        return ResponseEntity.ok(Map.of("remainingDays", remainingDays));
    }
}
