package com.example.payflow.hr.presentation.dto;

import com.example.payflow.hr.domain.Department;
import com.example.payflow.hr.domain.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class OrganizationChartResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private List<EmployeeResponse> employees;
    private List<OrganizationChartResponse> children;
    
    public static OrganizationChartResponse from(Department department, 
                                                  List<Employee> employees,
                                                  List<OrganizationChartResponse> children) {
        return new OrganizationChartResponse(
            department.getId(),
            department.getCode(),
            department.getName(),
            department.getDescription(),
            employees.stream().map(EmployeeResponse::from).collect(Collectors.toList()),
            children
        );
    }
}
