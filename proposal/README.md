# AINJOB HR SaaS 플랫폼 - 기술 아키텍처 제안서

## 📋 문서 구조

본 제안서는 AINJOB HR SaaS 플랫폼의 시스템 설계와 의사결정을 다룹니다.

### 📁 주요 문서 목록

0. **[요약 (Executive Summary)](./00-executive-summary.md)** ⭐ 시작하기
1. [기술 스택](./01-tech-stack.md)
2. [시스템 아키텍처 설계](./02-system-architecture.md)
3. [도메인 모델 & DDD 설계](./03-domain-design.md)
4. [필터 매칭 로직 설계](./04-filter-matching.md)
5. [데이터베이스 & ERD 설계](./05-database-erd.md)
6. **[SQL 설계 의도 및 쿼리 로직 설명](./06-sql-design-rationale.md)** ⭐ 중요
7. [더미 데이터 & 쿼리 결과](./07-dummy-data-queries.md)
8. [핵심 기능 & API 설계](./08-api-design.md)
9. [비기능 요구사항 & 보안](./09-non-functional-requirements.md)
10. [시퀀스 다이어그램](./10-sequence-diagrams.md)
11. [개발 조직 & 협업 프로세스](./11-team-collaboration.md)
12. [개발 일정표](./12-development-schedule.md)

### 📊 다이어그램

- [AWS 아키텍처 다이어그램](./diagrams/aws-architecture.md)

### 💾 SQL 파일

- [01. 테이블 생성 스크립트](./sql/01-create-tables.sql)
- [02. 더미 데이터 삽입 (기업, 스킬, 공고)](./sql/02-insert-dummy-data.sql)
- [03. 학력 및 경력 데이터](./sql/03-insert-education-career.sql)
- [04. 경력별 스킬 데이터](./sql/04-insert-career-skills.sql)
- [05. 이력서 및 지원 데이터](./sql/05-insert-resume-application.sql)
- [06. 합격자 조회 쿼리](./sql/06-qualified-applicants-query.sql)

---

## 🎯 프로젝트 개요

**AINJOB**은 기업회원이 적합한 지원자를 채용 공고를 통해 접수·관리하는 HR SaaS 플랫폼입니다.

### 핵심 목표

- **팀 규모**: 4-5명 (BE 2-3명, FE 1-2명, 디자이너 1명)
- **개발 기간**: 6-8개월
- **아키텍처**: 실무 적용 가능한 현실적 설계
- **기술 스택**: Java 17/Spring Boot 3.1, PostgreSQL, AWS

### 평가 기준

- ✅ **기술 설계력**: 아키텍처·DB·API 설계의 일관성
- ✅ **구조적 사고**: 모듈화, 도메인 분리, 데이터 흐름
- ✅ **실무 현실감**: 8개월 안에 구현 가능한 수준
- ✅ **리더십**: 협업/코드리뷰/일정관리 아이디어
- ✅ **문서 전달력**: 구조, 명확성, 도식 활용

---

## 🚀 빠른 시작

### 1. 요약 문서 읽기
[00-executive-summary.md](./00-executive-summary.md)에서 전체 제안서의 핵심 내용을 확인하세요.

### 2. SQL 실행 (PostgreSQL)

```bash
# 데이터베이스 생성
createdb ainjob

# 테이블 생성
psql -U postgres -d ainjob -f sql/01-create-tables.sql

# 더미 데이터 삽입
psql -U postgres -d ainjob -f sql/02-insert-dummy-data.sql
psql -U postgres -d ainjob -f sql/03-insert-education-career.sql
psql -U postgres -d ainjob -f sql/04-insert-career-skills.sql
psql -U postgres -d ainjob -f sql/05-insert-resume-application.sql

# 합격자 조회
psql -U postgres -d ainjob -f sql/06-qualified-applicants-query.sql
```

### 3. 주요 문서 탐색

**설계 문서:**
- [시스템 아키텍처](./02-system-architecture.md) - AWS 구성, 모듈러 모놀리스
- [도메인 설계](./03-domain-design.md) - DDD, Aggregate, Domain Event
- [필터 매칭](./04-filter-matching.md) - Filter Matching vs Soft Matching

**구현 문서:**
- [데이터베이스 설계](./05-database-erd.md) - ERD, 테이블 구조, 인덱스
- [SQL 설계 의도](./06-sql-design-rationale.md) - 쿼리 로직, 설계 이유, 최적화 전략
- [API 설계](./08-api-design.md) - REST API, 요청/응답 스펙
- [보안 & 성능](./09-non-functional-requirements.md) - JWT, RBAC, 캐싱

**프로세스 문서:**
- [팀 협업](./11-team-collaboration.md) - Git Flow, 코드 리뷰, 협업 도구
- [개발 일정](./12-development-schedule.md) - 8개월 일정, 마일스톤

---

## 📈 주요 설계 결정

### 아키텍처: 모듈러 모놀리스
- 팀 규모(4-5명)와 기간(6-8개월)에 최적화
- 빠른 개발 속도 + 향후 MSA 전환 가능

### 기술 스택
- **Backend**: Java 17 + Spring Boot 3.1
- **Database**: PostgreSQL 15 (복잡한 쿼리, JSON 지원)
- **Infrastructure**: AWS (EC2, RDS, S3, CloudFront)
- **CI/CD**: GitHub Actions

### 필터 매칭 시스템
- **Filter Matching**: 필수 조건 (학력, 전공, 스킬, 경력)
- **Soft Matching**: 온톨로지 기반 유사도 (가산점)

---

## 📊 데이터 구조

### 주요 Aggregate
1. **Applicant** (지원자) - Education, Career, CareerSkill
2. **Resume** (이력서) - 버전 관리, S3 저장
3. **JobPosting** (채용 공고) - Qualification, JobPostingSkill
4. **ApplicationTracking** (지원 추적) - StatusHistory

### 더미 데이터
- **기업**: 2개
- **채용 공고**: 4개 (백엔드 2개, 프런트엔드 2개)
- **지원자**: 25명 (백엔드 12명, 프런트엔드 13명)
- **합격자**: 백엔드 6명, 프런트엔드 6명

---

## 🔒 보안 & 성능

### 보안
- JWT 인증 (Access Token 15분, Refresh Token 7일)
- RBAC (4개 역할: APPLICANT, COMPANY_HR, COMPANY_ADMIN, SYSTEM_ADMIN)
- WAF, Rate Limiting, Secrets Manager

### 성능
- Redis 캐싱 (지원자 상세 5분, 공고 목록 10분)
- 인덱스 최적화 (학력, 전공, 경력, 스킬, 상태)
- N+1 쿼리 방지 (Fetch Join, EntityGraph)

---

## 📅 개발 일정 (8개월)

| Phase | 기간 | 주요 내용 |
|-------|------|----------|
| Phase 1 | 0-2개월 | 기반 구축 (인프라, 프로젝트 셋업, 기본 기능) |
| Phase 2 | 2-5개월 | 핵심 기능 (지원자 관리, 지원 추적, 고급 기능) |
| Phase 3 | 5-8개월 | 고도화 (보안 강화, 성능 최적화, 출시 준비) |

---

## 📦 제출물

### 문서
- ✅ 기술 아키텍처 제안서 (12개 문서, 약 150페이지)
- ✅ AWS 아키텍처 다이어그램 (Mermaid)
- ✅ ERD (Mermaid)
- ✅ 시퀀스 다이어그램 (6개 시나리오)

### SQL
- ✅ 테이블 생성 스크립트 (12개 테이블)
- ✅ 더미 데이터 (25명 지원자, 완전한 이력)
- ✅ 합격자 조회 쿼리 (백엔드/프런트엔드)

### 기타
- ✅ API 명세 (OpenAPI 스타일)
- ✅ 개발 일정표 (8개월, 32주)
- ✅ 팀 협업 프로세스 (Git Flow, 코드 리뷰)


