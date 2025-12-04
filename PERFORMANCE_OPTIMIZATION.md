# 성능 최적화 가이드

## 현재 문제점

### 1. 과도한 컴포넌트 로딩
- **666개의 Java 파일**, 217개의 Spring Bean, 63개의 JPA Repository
- 모든 컴포넌트가 부팅 시 스캔되고 초기화됨
- **해결책**: Lazy Initialization 활성화

### 2. JPA/Hibernate 초기화 부담
- 63개 Repository 동시 로드
- `ddl-auto=update`로 매번 스키마 검증
- **해결책**: `validate`로 변경, open-in-view 비활성화

### 3. Spring Security 과부하
- 100개 이상의 URL 패턴 매칭
- **해결책**: 패턴 단순화, 불필요한 규칙 제거

### 4. 외부 서비스 연결
- Kafka 연결 시도로 타임아웃 발생 가능
- **해결책**: 타임아웃 설정, fail-fast 비활성화

## 적용 방법

### 1. Cloud 프로파일 사용
```bash
# application-cloud.properties 파일 생성됨
# 배포 시 환경변수 설정
SPRING_PROFILES_ACTIVE=cloud
```

### 2. JVM 메모리 설정
```bash
# Procfile 또는 배포 스크립트에 추가
java -Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
     -Dspring.profiles.active=cloud -jar app.jar
```

### 3. 주요 최적화 설정

#### Lazy Initialization (가장 효과적)
```properties
spring.main.lazy-initialization=true
```
- 부팅 시간 50-70% 단축
- 실제 사용되는 Bean만 로드

#### HikariCP 연결 풀
```properties
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2
```
- 메모리 사용량 감소
- 클라우드 환경에 적합

#### Tomcat 스레드 풀
```properties
server.tomcat.threads.max=50
server.tomcat.threads.min-spare=10
```
- 메모리 사용량 감소
- 동시 처리 제한

## 추가 개선 사항

### 1. Security 설정 리팩토링
현재 SecurityConfig의 100개 이상 URL 패턴을 그룹화:

```java
// 개발용 엔드포인트 그룹화
private static final String[] DEV_ENDPOINTS = {
    "/api/test/**", "/api/saga/**", "/h2-console/**",
    "/parlevel/**", "/sourcing/**", "/escrow/**"
};

// 공개 엔드포인트 그룹화
private static final String[] PUBLIC_ENDPOINTS = {
    "/api/auth/**", "/login", "/signup", 
    "/css/**", "/js/**", "/images/**"
};

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
        .requestMatchers(DEV_ENDPOINTS).permitAll()
        .anyRequest().authenticated()
    );
}
```

### 2. 불필요한 의존성 제거
build.gradle에서 사용하지 않는 의존성 확인:
- Kafka Streams (사용 안 하면 제거)
- WebFlux (Toss API만 사용 시 RestTemplate로 대체)
- WebSocket (사용 안 하면 제거)

### 3. 컴포넌트 스캔 범위 제한
```java
@SpringBootApplication(scanBasePackages = {
    "com.example.payflow.common",
    "com.example.payflow.security",
    // 실제 사용하는 패키지만 명시
})
```

### 4. 데이터베이스 최적화
```properties
# 인덱스 추가 (자주 조회되는 컬럼)
# 쿼리 최적화 (N+1 문제 해결)
spring.jpa.properties.hibernate.default_batch_fetch_size=10
```

## 측정 및 모니터링

### 부팅 시간 측정
```bash
# 로그에서 부팅 시간 확인
grep "Started PayFlowApplication" boot-run.log
```

### 메모리 사용량 확인
```bash
# JVM 메모리 상태
jmap -heap <PID>

# 힙 덤프 생성
jmap -dump:format=b,file=heap.bin <PID>
```

## 예상 효과

| 최적화 항목 | 부팅 시간 개선 | 메모리 절약 |
|------------|--------------|------------|
| Lazy Initialization | 50-70% | 30-40% |
| HikariCP 최적화 | 5-10% | 20-30% |
| Tomcat 최적화 | 5-10% | 15-25% |
| JPA 최적화 | 10-15% | 10-15% |
| **총합** | **70-105%** | **75-110%** |

## 클라우드타입 배포 시 권장사항

1. **메모리 할당**: 최소 512MB, 권장 1GB
2. **프로파일 설정**: `SPRING_PROFILES_ACTIVE=cloud`
3. **헬스체크 타임아웃**: 60초 이상 설정
4. **Kafka 연결**: 외부 Kafka 사용 시 네트워크 지연 고려
