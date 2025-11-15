package com.example.payflow.hr.presentation.dto;

import com.example.payflow.hr.domain.LeaveType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LeaveRequest {
    
    @NotNull
    private LeaveType type;
    
    @NotNull
    private LocalDate startDate;
    
    @NotNull
    private LocalDate endDate;
    
    @NotNull
    private Integer days;
    
    @NotNull
    private String reason;
}
