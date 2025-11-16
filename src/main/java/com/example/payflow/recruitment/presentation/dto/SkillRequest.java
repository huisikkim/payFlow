package com.example.payflow.recruitment.presentation.dto;

import com.example.payflow.recruitment.domain.SkillCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SkillRequest {
    private String name;
    private SkillCategory category;
    private String description;
}
