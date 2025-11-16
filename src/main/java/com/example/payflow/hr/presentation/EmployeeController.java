package com.example.payflow.hr.presentation;

import com.example.payflow.hr.application.EmployeeService;
import com.example.payflow.hr.domain.Position;
import com.example.payflow.hr.presentation.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hr/employees")
@RequiredArgsConstructor
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/department")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeDepartment(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        employeeService.changeDepartment(id, body.get("departmentId"));
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> promoteEmployee(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Position position = Position.valueOf(body.get("position"));
        employeeService.promoteEmployee(id, position);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/resign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resignEmployee(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        LocalDate resignDate = LocalDate.parse(body.get("resignDate"));
        employeeService.resignEmployee(id, resignDate);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployee(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
            @RequestParam(required = false, defaultValue = "false") boolean includeResigned) {
        List<EmployeeResponse> employees = includeResigned 
            ? employeeService.getAllEmployees()
            : employeeService.getActiveEmployees();
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByDepartment(
            @PathVariable Long departmentId) {
        List<EmployeeResponse> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }
}
