package com.example.payflow.recruitment.presentation.dto;

import com.example.payflow.recruitment.domain.EducationLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CandidateRequest {
    private String name;
    private String email;
    private String phone;
    private EducationLevel education;
    private String university;
    private String major;
}
