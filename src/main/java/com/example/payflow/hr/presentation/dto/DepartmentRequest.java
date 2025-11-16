package com.example.payflow.hr.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentRequest {
    
    @NotNull
    private String code;
    
    @NotNull
    private String name;
    
    private String description;
    
    private Long parentId;
}
