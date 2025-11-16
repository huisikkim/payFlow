package com.example.payflow.recruitment.presentation.dto;

import com.example.payflow.recruitment.domain.Skill;
import com.example.payflow.recruitment.domain.SkillCategory;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class SkillResponse {
    private final Long id;
    private final String name;
    private final SkillCategory category;
    private final String description;
    private final List<String> similarSkills;
    
    public SkillResponse(Skill skill) {
        this.id = skill.getId();
        this.name = skill.getName();
        this.category = skill.getCategory();
        this.description = skill.getDescription();
        this.similarSkills = skill.getSimilarSkills().stream()
            .map(Skill::getName)
            .collect(Collectors.toList());
    }
}
