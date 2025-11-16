package com.example.payflow.hr.application;

import com.example.payflow.hr.domain.Department;
import com.example.payflow.hr.domain.DepartmentRepository;
import com.example.payflow.hr.presentation.dto.DepartmentRequest;
import com.example.payflow.hr.presentation.dto.DepartmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        Department parent = null;
        if (request.getParentId() != null) {
            parent = departmentRepository.findById(request.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("상위 부서를 찾을 수 없습니다."));
        }
        
        Department department = Department.create(
            request.getCode(),
            request.getName(),
            request.getDescription(),
            parent
        );
        
        Department saved = departmentRepository.save(department);
        return DepartmentResponse.from(saved);
    }
    
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));
        
        department.update(request.getName(), request.getDescription());
        
        return DepartmentResponse.from(department);
    }
    
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));
        
        departmentRepository.delete(department);
    }
    
    public DepartmentResponse getDepartment(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));
        
        return DepartmentResponse.from(department);
    }
    
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAllWithParent()
            .stream()
            .map(DepartmentResponse::from)
            .collect(Collectors.toList());
    }
    
    public List<DepartmentResponse> getRootDepartments() {
        return departmentRepository.findByParentIsNull()
            .stream()
            .map(DepartmentResponse::from)
            .collect(Collectors.toList());
    }
    
    public List<DepartmentResponse> getChildDepartments(Long parentId) {
        return departmentRepository.findByParentId(parentId)
            .stream()
            .map(DepartmentResponse::from)
            .collect(Collectors.toList());
    }
}
