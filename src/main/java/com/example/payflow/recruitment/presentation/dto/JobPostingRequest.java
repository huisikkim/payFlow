package com.example.payflow.recruitment.presentation.dto;

import com.example.payflow.hr.domain.Position;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class JobPostingRequest {
    private String title;
    private String description;
    private Long departmentId;
    private Position position;
    private Integer headcount;
    private LocalDate startDate;
    private LocalDate endDate;
}
