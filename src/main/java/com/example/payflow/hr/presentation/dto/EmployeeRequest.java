package com.example.payflow.hr.presentation.dto;

import com.example.payflow.hr.domain.Position;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeRequest {
    
    @NotNull
    private String employeeNumber;
    
    @NotNull
    private String name;
    
    @NotNull
    @Email
    private String email;
    
    @NotNull
    private String phone;
    
    @NotNull
    private Long departmentId;
    
    @NotNull
    private Position position;
    
    @NotNull
    private LocalDate joinDate;
}
