package com.example.payflow.ainjob.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApplicationCreateRequest {
    
    private Long applicantId;
    private Long jobPostingId;
    private Long resumeId;
}
