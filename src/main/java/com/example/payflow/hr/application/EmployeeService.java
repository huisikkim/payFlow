package com.example.payflow.hr.application;

import com.example.payflow.hr.domain.*;
import com.example.payflow.hr.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
            .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));
        
        Employee employee = Employee.create(
            request.getEmployeeNumber(),
            request.getName(),
            request.getEmail(),
            request.getPhone(),
            department,
            request.getPosition(),
            request.getJoinDate()
        );
        
        Employee saved = employeeRepository.save(employee);
        return EmployeeResponse.from(saved);
    }
    
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));
        
        employee.update(
            request.getName(),
            request.getEmail(),
            request.getPhone(),
            request.getAddress(),
            request.getBirthDate(),
            request.getNotes()
        );
        
        return EmployeeResponse.from(employee);
    }
    
    @Transactional
    public void changeDepartment(Long employeeId, Long departmentId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));
        
        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));
        
        employee.changeDepartment(department);
    }
    
    @Transactional
    public void promoteEmployee(Long employeeId, Position position) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));
        
        employee.promote(position);
    }
    
    @Transactional
    public void resignEmployee(Long employeeId, java.time.LocalDate resignDate) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));
        
        employee.resign(resignDate);
    }
    
    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("직원을 찾을 수 없습니다."));
        
        return EmployeeResponse.from(employee);
    }
    
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAllWithDepartment()
            .stream()
            .map(EmployeeResponse::from)
            .collect(Collectors.toList());
    }
    
    public List<EmployeeResponse> getActiveEmployees() {
        return employeeRepository.findAllActiveWithDepartment()
            .stream()
            .map(EmployeeResponse::from)
            .collect(Collectors.toList());
    }
    
    public List<EmployeeResponse> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findActiveByDepartmentId(departmentId)
            .stream()
            .map(EmployeeResponse::from)
            .collect(Collectors.toList());
    }
}
