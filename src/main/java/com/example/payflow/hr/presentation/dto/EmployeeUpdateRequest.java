package com.example.payflow.hr.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeUpdateRequest {
    
    @NotNull
    private String name;
    
    @NotNull
    @Email
    private String email;
    
    @NotNull
    private String phone;
    
    private String address;
    
    private LocalDate birthDate;
    
    private String notes;
}
