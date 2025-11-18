package com.example.payflow.ainjob.application.dto;

import com.example.payflow.ainjob.domain.ApplicationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApplicationStatusChangeRequest {
    
    private ApplicationStatus fromStatus;
    private ApplicationStatus toStatus;
    private String reason;
}
