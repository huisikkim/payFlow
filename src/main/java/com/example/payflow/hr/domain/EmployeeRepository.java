package com.example.payflow.hr.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByEmployeeNumber(String employeeNumber);
    
    Optional<Employee> findByEmail(String email);
    
    List<Employee> findByDepartmentId(Long departmentId);
    
    List<Employee> findByStatus(EmploymentStatus status);
    
    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId AND e.status = 'ACTIVE'")
    List<Employee> findActiveByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.status = 'ACTIVE' ORDER BY e.employeeNumber")
    List<Employee> findAllActiveWithDepartment();
    
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department ORDER BY e.employeeNumber")
    List<Employee> findAllWithDepartment();
}
