package com.example.payflow.hr.application;

import com.example.payflow.hr.domain.*;
import com.example.payflow.hr.presentation.dto.OrganizationChartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {
    
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    
    public List<OrganizationChartResponse> getOrganizationChart() {
        List<Department> rootDepartments = departmentRepository.findByParentIsNull();
        
        return rootDepartments.stream()
            .map(this::buildOrganizationChart)
            .collect(Collectors.toList());
    }
    
    private OrganizationChartResponse buildOrganizationChart(Department department) {
        List<Employee> employees = employeeRepository.findActiveByDepartmentId(department.getId());
        List<Department> children = departmentRepository.findByParentId(department.getId());
        
        List<OrganizationChartResponse> childCharts = children.stream()
            .map(this::buildOrganizationChart)
            .collect(Collectors.toList());
        
        return OrganizationChartResponse.from(department, employees, childCharts);
    }
}
