package com.example.payflow.hr.presentation.dto;

import com.example.payflow.hr.domain.Employee;
import com.example.payflow.hr.domain.EmploymentStatus;
import com.example.payflow.hr.domain.Position;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String employeeNumber;
    private String name;
    private String email;
    private String phone;
    private Long departmentId;
    private String departmentName;
    private Position position;
    private String positionName;
    private LocalDate joinDate;
    private LocalDate resignDate;
    private EmploymentStatus status;
    private String address;
    private LocalDate birthDate;
    private String notes;
    private String profileImageUrl;
    
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
            employee.getId(),
            employee.getEmployeeNumber(),
            employee.getName(),
            employee.getEmail(),
            employee.getPhone(),
            employee.getDepartment() != null ? employee.getDepartment().getId() : null,
            employee.getDepartment() != null ? employee.getDepartment().getName() : null,
            employee.getPosition(),
            employee.getPosition().getDisplayName(),
            employee.getJoinDate(),
            employee.getResignDate(),
            employee.getStatus(),
            employee.getAddress(),
            employee.getBirthDate(),
            employee.getNotes(),
            employee.getProfileImageUrl()
        );
    }
}
