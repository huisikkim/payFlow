package com.example.payflow.hr.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    Optional<Department> findByCode(String code);
    
    List<Department> findByParentIsNull();
    
    List<Department> findByParentId(Long parentId);
    
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.parent ORDER BY d.code")
    List<Department> findAllWithParent();
}
