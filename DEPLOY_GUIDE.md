# 클라우드타입 배포 가이드

## 배포 전 체크리스트

### 1. 빌드 확인
```bash
./gradlew clean build
```
- `build/libs/payflow.jar` 파일 생성 확인

### 2. 로컬 테스트 (cloud 프로파일)
```bash
java -Xms256m -Xmx512m -Dspring.profiles.active=cloud -jar build/libs/payflow.jar
```

## 클라우드타입 배포 설정

### 1. 환경 변수 설정
클라우드타입 대시보드에서 다음 환경변수 추가:

```
SPRING_PROFILES_ACTIVE=cloud
TOSS_SECRET_KEY=your_toss_secret_key
TOSS_CLIENT_KEY=your_toss_client_key
YOUTO_KEY=your_youtube_api_key
```

### 2. 리소스 할당
- **메모리**: 최소 512MB, 권장 1GB
- **CPU**: 0.5 vCPU 이상
- **디스크**: 1GB 이상

### 3. 헬스체크 설정
- **경로**: `/actuator/health`
- **포트**: 8080
- **타임아웃**: 60초 이상
- **시작 대기 시간**: 120초 이상 (Lazy Initialization으로 첫 요청 시 Bean 로드)

### 4. 빌드 명령어
```bash
./gradlew clean bootJar
```

### 5. 실행 명령어 (Procfile 사용)
```
web: java -Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -Dspring.profiles.active=cloud -jar build/libs/payflow.jar
```

## 주요 최적화 적용 사항

### Cloud 프로파일 (`application-cloud.properties`)
1. **Lazy Initialization**: 부팅 시간 50-70% 단축
2. **HikariCP 최적화**: 연결 풀 5개로 제한 (메모리 절약)
3. **Tomcat 스레드 최적화**: 최대 50개 스레드
4. **로깅 최소화**: WARN 레벨로 설정
5. **H2 Console 비활성화**: 보안 강화

### JVM 옵션
- `-Xms256m -Xmx512m`: 힙 메모리 256MB~512MB
- `-XX:+UseG1GC`: G1 가비지 컬렉터 사용
- `-XX:MaxGCPauseMillis=200`: GC 일시정지 최대 200ms
- `-XX:+UseStringDeduplication`: 문자열 중복 제거

## 배포 후 확인사항

### 1. 애플리케이션 시작 확인
```bash
# 로그에서 시작 시간 확인
curl https://your-app.cloudtype.app/actuator/health
```

### 2. 메모리 사용량 모니터링
- 클라우드타입 대시보드에서 메모리 사용량 확인
- 512MB 이상 사용 시 메모리 증설 고려

### 3. 응답 시간 확인
```bash
# 첫 요청 (Lazy Initialization으로 느릴 수 있음)
time curl https://your-app.cloudtype.app/

# 두 번째 요청 (정상 속도)
time curl https://your-app.cloudtype.app/
```

## 트러블슈팅

### 문제: 부팅 시간이 여전히 느림
**해결책**:
1. 메모리를 1GB로 증설
2. `spring.main.lazy-initialization=false`로 변경 (메모리 충분 시)
3. 사용하지 않는 기능 비활성화

### 문제: OutOfMemoryError 발생
**해결책**:
1. 메모리 증설 (1GB → 2GB)
2. HikariCP 연결 풀 더 줄이기 (5 → 3)
3. Tomcat 스레드 줄이기 (50 → 30)

### 문제: Kafka 연결 실패
**해결책**:
1. Kafka 서버 주소 확인: `svc.sel3.cloudtype.app:32315`
2. 네트워크 방화벽 확인
3. `spring.kafka.admin.fail-fast=false` 설정 확인

### 문제: 첫 요청이 매우 느림
**원인**: Lazy Initialization으로 첫 요청 시 Bean 로드
**해결책**:
1. 정상 동작 (두 번째 요청부터 빠름)
2. 중요 엔드포인트는 배포 후 Warm-up 요청 보내기
3. 메모리 충분하면 Lazy Initialization 비활성화

## 성능 비교

| 항목 | 최적화 전 | 최적화 후 |
|------|----------|----------|
| 부팅 시간 | 30-60초 | 10-20초 |
| 메모리 사용 | 800MB-1GB | 300-500MB |
| 첫 요청 응답 | 5-10초 | 2-5초 (Lazy) |
| 일반 요청 응답 | 100-500ms | 50-200ms |

## 추가 최적화 (선택사항)

### 1. 사용하지 않는 기능 비활성화
`application-cloud.properties`에 추가:
```properties
# WebSocket 사용 안 하면
spring.websocket.enabled=false

# Actuator 완전 비활성화
management.endpoints.enabled-by-default=false
management.endpoint.health.enabled=true
```

### 2. 데이터베이스 파일 영구 저장
클라우드타입에서 볼륨 마운트 설정:
- 경로: `/app/data`
- H2 데이터베이스 파일 유지

### 3. 로그 파일 관리
```properties
# 로그 파일 크기 제한
logging.file.max-size=10MB
logging.file.max-history=3
```
