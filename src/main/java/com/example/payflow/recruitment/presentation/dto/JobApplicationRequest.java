package com.example.payflow.recruitment.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JobApplicationRequest {
    private Long candidateId;
    private Long jobPostingId;
    private String coverLetter;
}
