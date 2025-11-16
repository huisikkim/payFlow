package com.example.payflow.hr.presentation.dto;

import com.example.payflow.hr.domain.Department;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
            department.getId(),
            department.getCode(),
            department.getName(),
            department.getDescription(),
            department.getParent() != null ? department.getParent().getId() : null,
            department.getParent() != null ? department.getParent().getName() : null
        );
    }
}
