package com.example.payflow.recruitment.infrastructure;

import com.example.payflow.hr.domain.Department;
import com.example.payflow.hr.domain.DepartmentRepository;
import com.example.payflow.hr.domain.Position;
import com.example.payflow.recruitment.application.*;
import com.example.payflow.recruitment.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 온톨로지 기반 채용 시스템 초기 데이터
 */
@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class RecruitmentDataInitializer implements CommandLineRunner {
    
    private final SkillService skillService;
    private final CandidateService candidateService;
    private final RecruitmentService recruitmentService;
    private final ApplicationService applicationService;
    private final DepartmentRepository departmentRepository;
    
    @Override
    public void run(String... args) {
        log.info("🎯 온톨로지 기반 채용 시스템 초기화 시작...");
        
        try {
            // 1. 기술 온톨로지 구축
            initializeSkillOntology();
            
            // 2. 지원자 데이터
            initializeCandidates();
            
            // 3. 채용 공고
            initializeJobPostings();
            
            // 4. 지원 데이터
            initializeApplications();
            
            log.info("✅ 온톨로지 기반 채용 시스템 초기화 완료!");
        } catch (Exception e) {
            log.warn("⚠️ 채용 시스템 초기화 중 오류 (이미 초기화됨): {}", e.getMessage());
        }
    }
    
    private void initializeSkillOntology() {
        log.info("📚 기술 온톨로지 구축 중...");
        
        // 프로그래밍 언어
        Skill java = skillService.createSkill("Java", SkillCategory.PROGRAMMING_LANGUAGE, "객체지향 프로그래밍 언어");
        Skill kotlin = skillService.createSkill("Kotlin", SkillCategory.PROGRAMMING_LANGUAGE, "JVM 기반 모던 언어");
        Skill python = skillService.createSkill("Python", SkillCategory.PROGRAMMING_LANGUAGE, "범용 프로그래밍 언어");
        Skill javascript = skillService.createSkill("JavaScript", SkillCategory.PROGRAMMING_LANGUAGE, "웹 프로그래밍 언어");
        
        // 프레임워크
        Skill spring = skillService.createSkill("Spring", SkillCategory.FRAMEWORK, "Java 엔터프라이즈 프레임워크");
        Skill springBoot = skillService.createSkill("Spring Boot", SkillCategory.FRAMEWORK, "Spring 기반 마이크로서비스");
        Skill django = skillService.createSkill("Django", SkillCategory.FRAMEWORK, "Python 웹 프레임워크");
        Skill react = skillService.createSkill("React", SkillCategory.FRAMEWORK, "JavaScript UI 라이브러리");
        
        // 데이터베이스
        Skill mysql = skillService.createSkill("MySQL", SkillCategory.DATABASE, "관계형 데이터베이스");
        Skill postgresql = skillService.createSkill("PostgreSQL", SkillCategory.DATABASE, "고급 관계형 DB");
        Skill mongodb = skillService.createSkill("MongoDB", SkillCategory.DATABASE, "NoSQL 문서 DB");
        Skill redis = skillService.createSkill("Redis", SkillCategory.DATABASE, "인메모리 캐시");
        
        // 클라우드
        Skill aws = skillService.createSkill("AWS", SkillCategory.CLOUD, "Amazon Web Services");
        Skill docker = skillService.createSkill("Docker", SkillCategory.DEVOPS, "컨테이너 플랫폼");
        Skill kubernetes = skillService.createSkill("Kubernetes", SkillCategory.DEVOPS, "컨테이너 오케스트레이션");
        
        // 방법론
        Skill msa = skillService.createSkill("MSA", SkillCategory.METHODOLOGY, "마이크로서비스 아키텍처");
        Skill ddd = skillService.createSkill("DDD", SkillCategory.METHODOLOGY, "도메인 주도 설계");
        Skill agile = skillService.createSkill("Agile", SkillCategory.METHODOLOGY, "애자일 방법론");
        
        // 온톨로지 관계 설정 (유사 기술)
        skillService.addSimilarSkill(java.getId(), kotlin.getId());
        skillService.addSimilarSkill(spring.getId(), springBoot.getId());
        skillService.addSimilarSkill(python.getId(), django.getId());
        skillService.addSimilarSkill(mysql.getId(), postgresql.getId());
        skillService.addSimilarSkill(docker.getId(), kubernetes.getId());
        
        log.info("✅ 기술 온톨로지 구축 완료 (18개 기술)");
    }
    
    private void initializeCandidates() {
        log.info("👥 지원자 데이터 생성 중...");
        
        // 지원자 1: 백엔드 개발자 (경력 5년)
        Candidate candidate1 = candidateService.createCandidate(
            "김개발", "kim.dev@example.com", "010-1111-2222",
            EducationLevel.BACHELOR, "서울대학교", "컴퓨터공학"
        );
        
        Skill java = skillService.getSkillByName("Java");
        Skill spring = skillService.getSkillByName("Spring");
        Skill springBoot = skillService.getSkillByName("Spring Boot");
        Skill mysql = skillService.getSkillByName("MySQL");
        Skill aws = skillService.getSkillByName("AWS");
        Skill msa = skillService.getSkillByName("MSA");
        
        candidateService.addSkillToCandidate(candidate1.getId(), java.getId(), 
            ProficiencyLevel.EXPERT, 5, "Java 백엔드 개발 5년");
        candidateService.addSkillToCandidate(candidate1.getId(), spring.getId(), 
            ProficiencyLevel.ADVANCED, 5, "Spring Framework 전문");
        candidateService.addSkillToCandidate(candidate1.getId(), springBoot.getId(), 
            ProficiencyLevel.EXPERT, 4, "Spring Boot 마이크로서비스");
        candidateService.addSkillToCandidate(candidate1.getId(), mysql.getId(), 
            ProficiencyLevel.ADVANCED, 5, "MySQL 쿼리 최적화");
        candidateService.addSkillToCandidate(candidate1.getId(), aws.getId(), 
            ProficiencyLevel.INTERMEDIATE, 3, "AWS EC2, RDS 운영");
        candidateService.addSkillToCandidate(candidate1.getId(), msa.getId(), 
            ProficiencyLevel.ADVANCED, 3, "MSA 설계 및 구현");
        
        candidateService.addExperienceToCandidate(candidate1.getId(),
            "네이버", "백엔드 개발자", LocalDate.of(2019, 3, 1), LocalDate.of(2022, 12, 31),
            false, "결제 시스템 개발", "TPS 3배 향상");
        candidateService.addExperienceToCandidate(candidate1.getId(),
            "카카오", "시니어 백엔드 개발자", LocalDate.of(2023, 1, 1), null,
            true, "MSA 전환 프로젝트 리드", "모놀리스 → MSA 전환 성공");
        
        // 지원자 2: 프론트엔드 개발자 (경력 3년)
        Candidate candidate2 = candidateService.createCandidate(
            "이프론트", "lee.front@example.com", "010-3333-4444",
            EducationLevel.BACHELOR, "연세대학교", "소프트웨어학"
        );
        
        Skill javascript = skillService.getSkillByName("JavaScript");
        Skill react = skillService.getSkillByName("React");
        
        candidateService.addSkillToCandidate(candidate2.getId(), javascript.getId(), 
            ProficiencyLevel.ADVANCED, 3, "JavaScript ES6+");
        candidateService.addSkillToCandidate(candidate2.getId(), react.getId(), 
            ProficiencyLevel.EXPERT, 3, "React 전문 개발자");
        
        candidateService.addExperienceToCandidate(candidate2.getId(),
            "토스", "프론트엔드 개발자", LocalDate.of(2021, 6, 1), null,
            true, "금융 서비스 UI/UX 개발", "사용자 만족도 40% 향상");
        
        // 지원자 3: 풀스택 개발자 (경력 7년)
        Candidate candidate3 = candidateService.createCandidate(
            "박풀스택", "park.fullstack@example.com", "010-5555-6666",
            EducationLevel.MASTER, "KAIST", "전산학"
        );
        
        Skill python = skillService.getSkillByName("Python");
        Skill django = skillService.getSkillByName("Django");
        Skill docker = skillService.getSkillByName("Docker");
        Skill kubernetes = skillService.getSkillByName("Kubernetes");
        
        candidateService.addSkillToCandidate(candidate3.getId(), python.getId(), 
            ProficiencyLevel.EXPERT, 7, "Python 백엔드 개발");
        candidateService.addSkillToCandidate(candidate3.getId(), django.getId(), 
            ProficiencyLevel.EXPERT, 7, "Django 프레임워크 전문");
        candidateService.addSkillToCandidate(candidate3.getId(), javascript.getId(), 
            ProficiencyLevel.ADVANCED, 5, "JavaScript 프론트엔드");
        candidateService.addSkillToCandidate(candidate3.getId(), react.getId(), 
            ProficiencyLevel.ADVANCED, 4, "React 개발");
        candidateService.addSkillToCandidate(candidate3.getId(), docker.getId(), 
            ProficiencyLevel.EXPERT, 5, "Docker 컨테이너화");
        candidateService.addSkillToCandidate(candidate3.getId(), kubernetes.getId(), 
            ProficiencyLevel.ADVANCED, 3, "K8s 운영");
        
        candidateService.addExperienceToCandidate(candidate3.getId(),
            "쿠팡", "풀스택 개발자", LocalDate.of(2017, 1, 1), null,
            true, "이커머스 플랫폼 개발", "일 거래액 100억 시스템 구축");
        
        log.info("✅ 지원자 3명 생성 완료");
    }
    
    private void initializeJobPostings() {
        log.info("📢 채용 공고 생성 중...");
        
        Department devDept = departmentRepository.findAll().stream()
            .filter(d -> d.getName().contains("개발"))
            .findFirst()
            .orElse(departmentRepository.findAll().get(0));
        
        // 공고 1: 백엔드 개발자
        JobPosting job1 = recruitmentService.createJobPosting(
            "백엔드 개발자 (Java/Spring)",
            "MSA 기반 결제 시스템 개발",
            devDept.getId(),
            Position.SENIOR,
            2,
            LocalDate.now(),
            LocalDate.now().plusMonths(2)
        );
        
        Skill java = skillService.getSkillByName("Java");
        Skill spring = skillService.getSkillByName("Spring Boot");
        Skill mysql = skillService.getSkillByName("MySQL");
        Skill msa = skillService.getSkillByName("MSA");
        Skill aws = skillService.getSkillByName("AWS");
        
        recruitmentService.addRequirement(job1.getId(), java.getId(), 
            RequirementType.REQUIRED, ProficiencyLevel.ADVANCED, 3, "Java 3년 이상");
        recruitmentService.addRequirement(job1.getId(), spring.getId(), 
            RequirementType.REQUIRED, ProficiencyLevel.ADVANCED, 3, "Spring Boot 경험");
        recruitmentService.addRequirement(job1.getId(), mysql.getId(), 
            RequirementType.REQUIRED, ProficiencyLevel.INTERMEDIATE, 2, "RDBMS 경험");
        recruitmentService.addRequirement(job1.getId(), msa.getId(), 
            RequirementType.PREFERRED, ProficiencyLevel.INTERMEDIATE, 1, "MSA 경험 우대");
        recruitmentService.addRequirement(job1.getId(), aws.getId(), 
            RequirementType.PREFERRED, ProficiencyLevel.INTERMEDIATE, 1, "클라우드 경험 우대");
        
        recruitmentService.publishJobPosting(job1.getId());
        
        // 공고 2: 프론트엔드 개발자
        JobPosting job2 = recruitmentService.createJobPosting(
            "프론트엔드 개발자 (React)",
            "사용자 경험 중심의 웹 서비스 개발",
            devDept.getId(),
            Position.STAFF,
            1,
            LocalDate.now(),
            LocalDate.now().plusMonths(1)
        );
        
        Skill javascript = skillService.getSkillByName("JavaScript");
        Skill react = skillService.getSkillByName("React");
        
        recruitmentService.addRequirement(job2.getId(), javascript.getId(), 
            RequirementType.REQUIRED, ProficiencyLevel.ADVANCED, 2, "JavaScript 2년 이상");
        recruitmentService.addRequirement(job2.getId(), react.getId(), 
            RequirementType.REQUIRED, ProficiencyLevel.ADVANCED, 2, "React 실무 경험");
        
        recruitmentService.publishJobPosting(job2.getId());
        
        log.info("✅ 채용 공고 2개 생성 완료");
    }
    
    private void initializeApplications() {
        log.info("📝 지원 데이터 생성 중...");
        
        Candidate candidate1 = candidateService.getCandidateByEmail("kim.dev@example.com");
        Candidate candidate2 = candidateService.getCandidateByEmail("lee.front@example.com");
        Candidate candidate3 = candidateService.getCandidateByEmail("park.fullstack@example.com");
        
        JobPosting job1 = recruitmentService.getActiveJobPostings().stream()
            .filter(j -> j.getTitle().contains("백엔드"))
            .findFirst()
            .orElseThrow();
        
        JobPosting job2 = recruitmentService.getActiveJobPostings().stream()
            .filter(j -> j.getTitle().contains("프론트엔드"))
            .findFirst()
            .orElseThrow();
        
        // 지원 1: 김개발 → 백엔드 공고 (높은 매칭)
        applicationService.applyForJob(candidate1.getId(), job1.getId(), 
            "5년간 Java/Spring 백엔드 개발 경험이 있으며, MSA 전환 프로젝트를 리드한 경험이 있습니다.");
        
        // 지원 2: 이프론트 → 프론트엔드 공고 (높은 매칭)
        applicationService.applyForJob(candidate2.getId(), job2.getId(), 
            "React 전문 개발자로 3년간 금융 서비스 UI를 개발했습니다.");
        
        // 지원 3: 박풀스택 → 백엔드 공고 (중간 매칭 - Python 개발자)
        applicationService.applyForJob(candidate3.getId(), job1.getId(), 
            "Python 백엔드 개발 7년 경력이 있으며, Java로 전환하고 싶습니다.");
        
        // 지원 4: 박풀스택 → 프론트엔드 공고 (높은 매칭)
        applicationService.applyForJob(candidate3.getId(), job2.getId(), 
            "풀스택 개발자로 React 개발 경험이 4년 있습니다.");
        
        log.info("✅ 지원 4건 생성 완료");
    }
}
