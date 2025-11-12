package com.example.payflow.chatbot.infrastructure;

import com.example.payflow.chatbot.domain.Job;
import com.example.payflow.chatbot.domain.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobDataInitializer implements CommandLineRunner {

    private final JobRepository jobRepository;

    @Override
    public void run(String... args) {
        if (jobRepository.count() > 0) {
            log.info("Job data already initialized. Skipping...");
            return;
        }

        log.info("Initializing job data...");

        List<Job> jobs = List.of(
            // 서울 IT
            new Job("네이버", "백엔드 개발자", "서울", "IT",
                40000000L, 60000000L, "Java/Spring 기반 백엔드 개발",
                "JUNIOR", "FULL_TIME", LocalDateTime.now().plusMonths(1)),
            
            new Job("카카오", "프론트엔드 개발자", "서울", "IT",
                45000000L, 65000000L, "React/Vue.js 프론트엔드 개발",
                "JUNIOR", "FULL_TIME", LocalDateTime.now().plusMonths(1)),
            
            new Job("라인", "풀스택 개발자", "서울", "IT",
                50000000L, 80000000L, "Java/React 풀스택 개발",
                "SENIOR", "FULL_TIME", LocalDateTime.now().plusMonths(2)),
            
            new Job("쿠팡", "DevOps 엔지니어", "서울", "IT",
                55000000L, 85000000L, "AWS 기반 인프라 운영",
                "SENIOR", "FULL_TIME", LocalDateTime.now().plusMonths(1)),
            
            new Job("토스", "백엔드 개발자", "서울", "금융",
                60000000L, 90000000L, "핀테크 백엔드 개발",
                "SENIOR", "FULL_TIME", LocalDateTime.now().plusMonths(2)),

            // 경기 IT
            new Job("삼성전자", "소프트웨어 엔지니어", "경기", "IT",
                45000000L, 70000000L, "임베디드 소프트웨어 개발",
                "JUNIOR", "FULL_TIME", LocalDateTime.now().plusMonths(1)),
            
            new Job("LG전자", "AI 개발자", "경기", "IT",
                50000000L, 75000000L, "머신러닝/딥러닝 연구개발",
                "SENIOR", "FULL_TIME", LocalDateTime.now().plusMonths(2)),

            // 서울 금융
            new Job("KB국민은행", "IT 개발자", "서울", "금융",
                40000000L, 60000000L, "은행 시스템 개발 및 운영",
                "JUNIOR", "FULL_TIME", LocalDateTime.now().plusMonths(1)),
            
            new Job("신한은행", "데이터 분석가", "서울", "금융",
                45000000L, 65000000L, "금융 데이터 분석 및 리포팅",
                "JUNIOR", "FULL_TIME", LocalDateTime.now().plusMonths(1)),
            
            new Job("하나은행", "보안 엔지니어", "서울", "금융",
                50000000L, 75000000L, "금융 보안 시스템 구축",
                "SENIOR", "FULL_TIME", LocalDateTime.now().plusMonths(2)),

            // 부산
            new Job("부산은행", "IT 개발자", "부산", "금융",
                35000000L, 55000000L, "은행 시스템 개발",
                "JUNIOR", "FULL_TIME", LocalDateTime.now().plusMonths(1)),
            
            new Job("현대중공업", "소프트웨어 엔지니어", "부산", "제조",
                40000000L, 60000000L, "제조 시스템 개발",
                "JUNIOR", "FULL_TIME", LocalDateTime.now().plusMonths(1)),

            // 스타트업
            new Job("당근마켓", "백엔드 개발자", "서울", "스타트업",
                45000000L, 70000000L, "커뮤니티 플랫폼 백엔드 개발",
                "JUNIOR", "FULL_TIME", LocalDateTime.now().plusMonths(1)),
            
            new Job("야놀자", "풀스택 개발자", "서울", "스타트업",
                50000000L, 75000000L, "여행 플랫폼 개발",
                "SENIOR", "FULL_TIME", LocalDateTime.now().plusMonths(2)),
            
            new Job("마켓컬리", "데이터 엔지니어", "서울", "스타트업",
                45000000L, 70000000L, "데이터 파이프라인 구축",
                "JUNIOR", "FULL_TIME", LocalDateTime.now().plusMonths(1)),

            // 게임
            new Job("넷마블", "게임 서버 개발자", "서울", "게임",
                45000000L, 70000000L, "모바일 게임 서버 개발",
                "JUNIOR", "FULL_TIME", LocalDateTime.now().plusMonths(1)),
            
            new Job("엔씨소프트", "게임 클라이언트 개발자", "경기", "게임",
                50000000L, 80000000L, "PC/모바일 게임 클라이언트 개발",
                "SENIOR", "FULL_TIME", LocalDateTime.now().plusMonths(2)),

            // 교육
            new Job("메가스터디", "교육 플랫폼 개발자", "서울", "교육",
                35000000L, 55000000L, "온라인 교육 플랫폼 개발",
                "JUNIOR", "FULL_TIME", LocalDateTime.now().plusMonths(1)),
            
            new Job("대교", "콘텐츠 개발자", "서울", "교육",
                30000000L, 50000000L, "교육 콘텐츠 개발",
                "ENTRY", "FULL_TIME", LocalDateTime.now().plusMonths(1)),

            // 의료
            new Job("서울대병원", "의료 IT 개발자", "서울", "의료",
                40000000L, 60000000L, "병원 정보 시스템 개발",
                "JUNIOR", "FULL_TIME", LocalDateTime.now().plusMonths(1)),
            
            new Job("삼성서울병원", "헬스케어 데이터 분석가", "서울", "의료",
                45000000L, 65000000L, "의료 데이터 분석",
                "JUNIOR", "FULL_TIME", LocalDateTime.now().plusMonths(2))
        );

        jobRepository.saveAll(jobs);
        log.info("Initialized {} job postings", jobs.size());
    }
}
