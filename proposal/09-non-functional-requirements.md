# 9. 비기능 요구사항 & 보안

## 9.1 성능 (Performance)

### 9.1.1 응답 시간 목표

| API 유형 | 목표 응답 시간 | 최대 허용 시간 |
|---------|--------------|--------------|
| 단순 조회 (GET) | < 200ms | < 500ms |
| 목록 조회 (필터링) | < 500ms | < 1s |
| 생성/수정 (POST/PUT) | < 300ms | < 1s |
| 복잡한 검색 | < 1s | < 3s |
| 파일 업로드 | < 2s | < 5s |

### 9.1.2 페이지네이션

**기본 설정:**
```java
@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
Pageable pageable
```

**최대 페이지 크기:** 100

**커서 기반 페이지네이션 (선택):**
```
GET /api/v1/applicants?cursor=eyJpZCI6MTAwfQ==&size=20
```

### 9.1.3 인덱스 전략

**필수 인덱스:**
```sql
-- 지원자 검색
CREATE INDEX idx_application_posting_status ON application_tracking(job_posting_id, status);
CREATE INDEX idx_education_level_major ON education(education_level, major_id);
CREATE INDEX idx_career_dates ON career(applicant_id, start_date, end_date);
CREATE INDEX idx_career_skill_skill ON career_skill(skill_id);

-- 복합 인덱스
CREATE INDEX idx_applicant_search ON applicant(name, email);
CREATE INDEX idx_jobposting_search ON job_posting(company_id, status, position);
```

### 9.1.4 N+1 쿼리 방지

**Fetch Join 사용:**
```java
@Query("SELECT a FROM Applicant a " +
       "LEFT JOIN FETCH a.educations e " +
       "LEFT JOIN FETCH e.major " +
       "WHERE a.id = :id")
Optional<Applicant> findByIdWithEducations(@Param("id") Long id);
```

**EntityGraph 사용:**
```java
@EntityGraph(attributePaths = {"educations", "careers", "careers.skills"})
Optional<Applicant> findById(Long id);
```

### 9.1.5 캐싱 전략

**Redis 캐시:**
```java
@Cacheable(value = "applicants", key = "#id")
public ApplicantDetailResponse getApplicant(Long id) {
    // ...
}

@CacheEvict(value = "applicants", key = "#id")
public void updateApplicant(Long id, ApplicantUpdateRequest request) {
    // ...
}
```

**캐시 TTL:**
- 지원자 상세: 5분
- 공고 목록: 10분
- 통계 데이터: 1시간

### 9.1.6 대용량 조인 방지

**DTO Projection 사용:**
```java
@Query("SELECT new com.ainjob.dto.ApplicantSummaryDTO(" +
       "a.id, a.name, a.email, e.level, m.name) " +
       "FROM Applicant a " +
       "JOIN a.educations e " +
       "JOIN e.major m")
List<ApplicantSummaryDTO> findAllSummaries();
```

---

## 9.2 가용성 (Availability)

### 9.2.1 목표 SLA

- **Uptime**: 99.9% (월 43분 다운타임 허용)
- **RTO (Recovery Time Objective)**: 5분
- **RPO (Recovery Point Objective)**: 5분

### 9.2.2 장애 복구 방안

**자동 복구:**
```
EC2 Health Check 실패
  ↓
Auto Scaling Group이 새 인스턴스 시작
  ↓
ALB가 트래픽 라우팅
  ↓
복구 완료 (5분 이내)
```

**수동 복구:**
```
1. CloudWatch Alarm 발생
2. On-call 엔지니어 알림
3. 로그 분석 (CloudWatch Logs)
4. 롤백 또는 핫픽스 배포
5. 사후 분석 (Post-mortem)
```

### 9.2.3 롤링 배포

**배포 전략:**
```yaml
deployment:
  strategy: RollingUpdate
  maxUnavailable: 1
  maxSurge: 1
  
steps:
  1. 새 버전 빌드 & 테스트
  2. EC2-1 트래픽 제거
  3. EC2-1 배포 & Health Check
  4. EC2-1 트래픽 복구
  5. EC2-2 반복
```

**Health Check:**
```
GET /actuator/health
Response: {"status": "UP"}
```

### 9.2.4 Circuit Breaker (선택)

```java
@CircuitBreaker(name = "emailService", fallbackMethod = "sendEmailFallback")
public void sendEmail(String to, String subject, String body) {
    // 외부 이메일 서비스 호출
}

public void sendEmailFallback(String to, String subject, String body, Exception e) {
    // 큐에 저장 후 나중에 재시도
    emailQueueService.enqueue(to, subject, body);
}
```

---

## 9.3 보안 (Security)

### 9.3.1 인증 (Authentication)

**JWT 기반 인증:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/applicants/**").hasRole("HR")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter(), 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**토큰 관리:**
- Access Token: 15분 유효
- Refresh Token: 7일 유효
- Redis에 Refresh Token 저장
- 로그아웃 시 블랙리스트 등록

### 9.3.2 인가 (Authorization) - RBAC

**역할 정의:**
```java
public enum Role {
    APPLICANT,      // 지원자
    COMPANY_HR,     // 기업 HR 담당자
    COMPANY_ADMIN,  // 기업 관리자
    SYSTEM_ADMIN    // 시스템 관리자
}
```

**권한 매트릭스:**

| 기능 | APPLICANT | COMPANY_HR | COMPANY_ADMIN | SYSTEM_ADMIN |
|------|-----------|------------|---------------|--------------|
| 지원하기 | ✅ | ❌ | ❌ | ❌ |
| 내 지원 내역 조회 | ✅ | ❌ | ❌ | ✅ |
| 지원자 목록 조회 | ❌ | ✅ | ✅ | ✅ |
| 지원 상태 변경 | ❌ | ✅ | ✅ | ✅ |
| 공고 등록 | ❌ | ✅ | ✅ | ✅ |
| 공고 삭제 | ❌ | ❌ | ✅ | ✅ |
| 회사 정보 수정 | ❌ | ❌ | ✅ | ✅ |
| 시스템 설정 | ❌ | ❌ | ❌ | ✅ |

### 9.3.3 데이터 보호

**비밀번호 암호화:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

**민감 정보 암호화:**
```java
// 주민등록번호, 계좌번호 등
@Convert(converter = SensitiveDataConverter.class)
private String residentNumber;
```

**S3 파일 암호화:**
```yaml
aws:
  s3:
    encryption: AES256
    bucket-policy:
      - Effect: Deny
        Principal: "*"
        Action: "s3:GetObject"
        Condition:
          Bool:
            aws:SecureTransport: false
```

### 9.3.4 HTTPS 필수

**ALB SSL/TLS 설정:**
```
Certificate: AWS Certificate Manager (ACM)
Protocol: TLS 1.2+
Cipher Suite: ECDHE-RSA-AES128-GCM-SHA256
```

**HSTS 헤더:**
```java
http.headers()
    .httpStrictTransportSecurity()
    .maxAgeInSeconds(31536000)
    .includeSubDomains(true);
```

### 9.3.5 VPC & Security Group

**VPC 구조:**
```
Public Subnet (10.0.1.0/24)
  - ALB
  - NAT Gateway
  - Bastion Host

Private Subnet (10.0.2.0/24)
  - EC2 (API Server)
  - ElastiCache (Redis)

Private Subnet - DB (10.0.3.0/24)
  - RDS (PostgreSQL)
```

**Security Group 규칙:**
```yaml
ALB:
  Inbound:
    - Port: 443, Source: 0.0.0.0/0 (HTTPS)
  Outbound:
    - Port: 8080, Destination: EC2 Security Group

EC2:
  Inbound:
    - Port: 8080, Source: ALB Security Group
    - Port: 22, Source: Bastion Security Group
  Outbound:
    - Port: 5432, Destination: RDS Security Group
    - Port: 6379, Destination: Redis Security Group
    - Port: 443, Destination: 0.0.0.0/0 (AWS API, S3)

RDS:
  Inbound:
    - Port: 5432, Source: EC2 Security Group
  Outbound: None

Redis:
  Inbound:
    - Port: 6379, Source: EC2 Security Group
  Outbound: None
```

### 9.3.6 Secrets Manager

**민감 정보 관리:**
```yaml
# application.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}  # Secrets Manager에서 주입
  
aws:
  secretsmanager:
    secret-name: ainjob/prod/database
```

**자동 로테이션:**
```
RDS 비밀번호 → 30일마다 자동 로테이션
API Key → 90일마다 수동 로테이션
```

### 9.3.7 WAF (Web Application Firewall) - Advanced

**AWS WAF 규칙:**
```yaml
rules:
  - name: RateLimitRule
    priority: 1
    action: BLOCK
    condition: Requests > 1000/5min from same IP
  
  - name: SQLInjectionRule
    priority: 2
    action: BLOCK
    condition: SQL Injection pattern detected
  
  - name: XSSRule
    priority: 3
    action: BLOCK
    condition: XSS pattern detected
  
  - name: GeoBlockingRule
    priority: 4
    action: BLOCK
    condition: Country NOT IN [KR, US, JP]
```

### 9.3.8 Rate Limiting

**Spring Boot Rate Limiter:**
```java
@RateLimiter(name = "api", fallbackMethod = "rateLimitFallback")
@GetMapping("/applicants")
public ResponseEntity<?> getApplicants() {
    // ...
}

public ResponseEntity<?> rateLimitFallback(Exception e) {
    return ResponseEntity.status(429)
        .body(new ErrorResponse("TOO_MANY_REQUESTS", 
            "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요."));
}
```

**설정:**
```yaml
resilience4j:
  ratelimiter:
    instances:
      api:
        limitForPeriod: 100
        limitRefreshPeriod: 1m
        timeoutDuration: 0s
```

### 9.3.9 브루트포스 방어

**로그인 시도 제한:**
```java
@Service
public class LoginAttemptService {
    
    private final LoadingCache<String, Integer> attemptsCache;
    
    public LoginAttemptService() {
        attemptsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Integer>() {
                public Integer load(String key) {
                    return 0;
                }
            });
    }
    
    public void loginFailed(String email) {
        int attempts = attemptsCache.getUnchecked(email);
        attemptsCache.put(email, attempts + 1);
    }
    
    public boolean isBlocked(String email) {
        return attemptsCache.getUnchecked(email) >= 5;
    }
}
```

### 9.3.10 감사 로그 (Audit Log)

**중요 작업 로깅:**
```java
@Aspect
@Component
public class AuditAspect {
    
    @AfterReturning("@annotation(Auditable)")
    public void logAudit(JoinPoint joinPoint) {
        String user = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        String action = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        auditLogRepository.save(new AuditLog(
            user, action, args, LocalDateTime.now()
        ));
    }
}
```

**로그 항목:**
- 사용자 ID
- 작업 유형 (CREATE, UPDATE, DELETE)
- 대상 리소스
- 변경 전/후 값
- IP 주소
- 타임스탬프

---

## 9.4 모니터링 & 알람

### CloudWatch 메트릭

**기본 메트릭:**
- CPU Utilization
- Memory Utilization
- Disk I/O
- Network I/O

**커스텀 메트릭:**
```java
@Component
public class MetricsPublisher {
    
    private final CloudWatchAsyncClient cloudWatch;
    
    public void publishApplicationMetric(String metricName, double value) {
        PutMetricDataRequest request = PutMetricDataRequest.builder()
            .namespace("AINJOB/Application")
            .metricData(MetricDatum.builder()
                .metricName(metricName)
                .value(value)
                .timestamp(Instant.now())
                .build())
            .build();
        
        cloudWatch.putMetricData(request);
    }
}
```

**알람 설정:**
```yaml
alarms:
  - name: HighCPU
    metric: CPUUtilization
    threshold: 80
    period: 5m
    action: SNS notification
  
  - name: HighErrorRate
    metric: 5XXError
    threshold: 10
    period: 1m
    action: SNS notification + Auto Scaling
  
  - name: LowDiskSpace
    metric: DiskSpaceUtilization
    threshold: 90
    period: 5m
    action: SNS notification
```

---

## 9.5 백업 & 복구

### RDS 백업
```
자동 백업: 매일 03:00 (KST)
보관 기간: 7일
스냅샷: 주 1회 (일요일)
스냅샷 보관: 30일
```

### S3 버전 관리
```
버전 관리: 활성화
Lifecycle Policy:
  - 90일 후 Glacier로 이동
  - 365일 후 삭제
```
