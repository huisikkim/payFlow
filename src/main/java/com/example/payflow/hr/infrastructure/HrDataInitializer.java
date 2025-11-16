package com.example.payflow.hr.infrastructure;

import com.example.payflow.hr.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class HrDataInitializer implements CommandLineRunner {
    
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    
    @Override
    @Transactional
    public void run(String... args) {
        // 이미 데이터가 있으면 스킵
        if (departmentRepository.count() > 0) {
            log.info("HR 데이터가 이미 존재합니다. 초기화를 스킵합니다.");
            return;
        }
        
        log.info("HR 샘플 데이터 초기화 시작...");
        
        // 부서 생성
        Department ceo = departmentRepository.save(
            Department.create("CEO", "대표이사실", "최고 경영진", null)
        );
        
        Department dev = departmentRepository.save(
            Department.create("DEV", "개발팀", "소프트웨어 개발 및 유지보수", ceo)
        );
        
        Department backend = departmentRepository.save(
            Department.create("DEV-BE", "백엔드팀", "서버 및 API 개발", dev)
        );
        
        Department frontend = departmentRepository.save(
            Department.create("DEV-FE", "프론트엔드팀", "웹 및 모바일 UI 개발", dev)
        );
        
        Department hr = departmentRepository.save(
            Department.create("HR", "인사팀", "인사 관리 및 채용", ceo)
        );
        
        Department finance = departmentRepository.save(
            Department.create("FIN", "재무팀", "재무 관리 및 회계", ceo)
        );
        
        // 직원 생성
        employeeRepository.save(
            Employee.create("EMP001", "김대표", "ceo@company.com", "010-1111-1111",
                ceo, Position.CEO, LocalDate.of(2020, 1, 1))
        );
        
        employeeRepository.save(
            Employee.create("EMP002", "이기술", "cto@company.com", "010-2222-2222",
                dev, Position.CTO, LocalDate.of(2020, 3, 1))
        );
        
        employeeRepository.save(
            Employee.create("EMP003", "박백엔드", "backend1@company.com", "010-3333-3333",
                backend, Position.GENERAL_MANAGER, LocalDate.of(2021, 1, 15))
        );
        
        employeeRepository.save(
            Employee.create("EMP004", "최프론트", "frontend1@company.com", "010-4444-4444",
                frontend, Position.MANAGER, LocalDate.of(2021, 6, 1))
        );
        
        employeeRepository.save(
            Employee.create("EMP005", "정개발", "dev1@company.com", "010-5555-5555",
                backend, Position.SENIOR, LocalDate.of(2022, 3, 1))
        );
        
        employeeRepository.save(
            Employee.create("EMP006", "강인사", "hr1@company.com", "010-6666-6666",
                hr, Position.MANAGER, LocalDate.of(2021, 9, 1))
        );
        
        employeeRepository.save(
            Employee.create("EMP007", "윤재무", "finance1@company.com", "010-7777-7777",
                finance, Position.GENERAL_MANAGER, LocalDate.of(2020, 6, 1))
        );
        
        employeeRepository.save(
            Employee.create("EMP008", "신입사원", "newbie@company.com", "010-8888-8888",
                backend, Position.STAFF, LocalDate.of(2024, 1, 2))
        );
        
        log.info("HR 샘플 데이터 초기화 완료!");
        log.info("- 부서: {} 개", departmentRepository.count());
        log.info("- 직원: {} 명", employeeRepository.count());
    }
}
