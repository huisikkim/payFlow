# 품질 이슈 신고 및 반품/교환 시스템 - 구현 완료 요약

## ✅ 구현 완료 내용

### 1. 백엔드 API (Spring Boot)
- **11개 REST API 엔드포인트** 구현 완료
- **DDD 패턴** 적용 (Domain, Application, Presentation 레이어 분리)
- **JWT 인증** 적용
- **빌드 성공** (컴파일 에러 없음)

### 2. 도메인 모델
```
qualityissue/
├── domain/
│   ├── QualityIssue.java           # 품질 이슈 엔티티
│   ├── IssueType.java              # 5가지 이슈 유형
│   ├── IssueStatus.java            # 8단계 상태
│   ├── RequestAction.java          # 환불/교환
│   └── QualityIssueRepository.java
├── application/
│   └── QualityIssueService.java    # 비즈니스 로직
└── presentation/
    ├── QualityIssueController.java # REST API
    └── dto/ (5개 DTO)
```

### 3. 주요 기능

#### 가게사장님 기능
- ✅ 품질 이슈 신고 (사진 첨부)
- ✅ 내 이슈 목록 조회
- ✅ 이슈 상세 조회

#### 유통업자 기능
- ✅ 대기 중인 이슈 조회
- ✅ 전체 이슈 조회
- ✅ 검토 시작
- ✅ 승인/거절
- ✅ 수거 예약
- ✅ 수거 완료
- ✅ 환불/교환 완료

### 4. 이슈 유형 (5가지)
1. **POOR_QUALITY** - 품질 불량
2. **WRONG_ITEM** - 오배송
3. **DAMAGED** - 파손
4. **EXPIRED** - 유통기한 임박/경과
5. **QUANTITY_MISMATCH** - 수량 불일치

### 5. 상태 흐름 (8단계)
```
SUBMITTED → REVIEWING → APPROVED → PICKUP_SCHEDULED → PICKED_UP → REFUNDED/EXCHANGED
                     ↘ REJECTED
```

---

## 📚 문서 목록

### 백엔드 개발자용
1. **QUALITY_ISSUE_GUIDE.md** (가장 상세)
   - 전체 API 문서
   - 요청/응답 예시
   - 도메인 모델
   - 비즈니스 로직
   - 향후 확장 가능 기능

2. **QUALITY_ISSUE_EXAMPLE.md**
   - 실제 사용 시나리오 3가지
   - 환불 케이스 (양파 썩음)
   - 교환 케이스 (감자 파손)
   - 거절 케이스
   - 타임라인 요약

3. **test-quality-issue-api.sh**
   - 자동화된 테스트 스크립트
   - 전체 플로우 테스트 (환불 + 교환)

### Flutter 개발자용
1. **FLUTTER_QUALITY_ISSUE_API.md** (가장 상세)
   - 전체 11개 API 엔드포인트
   - 요청/응답 예시
   - 데이터 모델 (Dart 코드)
   - API Service 클래스 (완전한 구현)
   - 가게사장님 화면 예시
   - 유통업자 화면 예시
   - 에러 처리
   - 프로세스 플로우
   - 테스트 가이드
   - 체크리스트

2. **FLUTTER_QUALITY_ISSUE_QUICK_START.md** (빠른 시작)
   - 5분 안에 시작하기
   - 최소한의 코드로 구현
   - 주요 API 목록
   - UI 예시
   - 인증 방법

---

## 🚀 시작하기

### 백엔드 서버 실행
```bash
cd payFlow
./gradlew bootRun
```

서버가 `http://localhost:8080`에서 실행됩니다.

### API 테스트
```bash
./test-quality-issue-api.sh
```

### Flutter 앱 개발
1. [FLUTTER_QUALITY_ISSUE_QUICK_START.md](./FLUTTER_QUALITY_ISSUE_QUICK_START.md) 읽기
2. 모델 클래스 복사
3. API Service 클래스 복사
4. UI 구현

---

## 📊 API 엔드포인트 요약

| # | API | 메서드 | 엔드포인트 | 역할 |
|---|-----|--------|-----------|------|
| 1 | 신고 | POST | `/api/quality-issues` | 가게사장님 |
| 2 | 상세 조회 | GET | `/api/quality-issues/{id}` | 공통 |
| 3 | 가게별 목록 | GET | `/api/quality-issues/store/{storeId}` | 가게사장님 |
| 4 | 대기 목록 | GET | `/api/quality-issues/distributor/{distributorId}/pending` | 유통업자 |
| 5 | 전체 목록 | GET | `/api/quality-issues/distributor/{distributorId}` | 유통업자 |
| 6 | 검토 시작 | POST | `/api/quality-issues/{id}/review` | 유통업자 |
| 7 | 승인 | POST | `/api/quality-issues/{id}/approve` | 유통업자 |
| 8 | 거절 | POST | `/api/quality-issues/{id}/reject` | 유통업자 |
| 9 | 수거 예약 | POST | `/api/quality-issues/{id}/schedule-pickup` | 유통업자 |
| 10 | 수거 완료 | POST | `/api/quality-issues/{id}/complete-pickup` | 유통업자 |
| 11 | 환불/교환 완료 | POST | `/api/quality-issues/{id}/complete-resolution` | 유통업자 |

---

## 🎯 핵심 포인트

### 1. 간편한 신고
- 최소한의 정보로 빠르게 신고
- 사진 여러 장 첨부 가능
- 5가지 이슈 유형 선택

### 2. 빠른 대응
- 유통업자가 사진으로 즉시 확인
- 당일 수거 예약
- 빠른 환불/교환 처리

### 3. 투명한 프로세스
- 8단계 상태로 명확한 추적
- 모든 단계에서 코멘트 기록
- 실시간 상태 업데이트

### 4. 100% 품질 보증
- 품질 문제 시 전액 환불
- 또는 새 제품으로 교환
- 고객 만족도 향상

### 5. 확장 가능한 구조
- DDD 패턴으로 명확한 구조
- 새로운 기능 추가 용이
- Kafka 이벤트 발행 준비 완료

---

## 🔮 향후 확장 가능 기능

### 1. 사진 업로드
- 실제 파일 업로드 API
- S3 또는 로컬 스토리지 저장
- 이미지 리사이징

### 2. 알림 시스템
- 품질 이슈 접수 알림
- 승인/거절 알림
- 수거 예정 알림
- 완료 알림

### 3. 통계 대시보드
- 품질 이슈 발생 빈도
- 이슈 유형별 통계
- 유통사별 품질 점수
- 평균 처리 시간

### 4. Kafka 이벤트
- QualityIssueSubmitted
- QualityIssueApproved
- QualityIssueRejected
- PickupScheduled
- RefundCompleted
- ExchangeCompleted

### 5. 웹 UI
- 가게사장님 대시보드
- 유통업자 대시보드
- 실시간 상태 추적
- 사진 갤러리

---

## 📞 문의

### 백엔드 API 문제
- 백엔드 팀에 문의
- 로그 확인: `./gradlew bootRun` 실행 로그

### Flutter 구현 문제
- [FLUTTER_QUALITY_ISSUE_API.md](./FLUTTER_QUALITY_ISSUE_API.md) 참고
- 프론트엔드 팀 내부 논의

---

## ✅ 체크리스트

### 백엔드 개발자
- [x] 도메인 모델 구현
- [x] 비즈니스 로직 구현
- [x] REST API 구현
- [x] JWT 인증 적용
- [x] 빌드 성공
- [x] 테스트 스크립트 작성
- [x] API 문서 작성
- [ ] Kafka 이벤트 발행 (선택)
- [ ] 웹 UI 구현 (선택)

### Flutter 개발자
- [ ] API Service 클래스 구현
- [ ] 데이터 모델 구현
- [ ] 로그인 및 토큰 관리
- [ ] 가게사장님 화면 (신고, 목록)
- [ ] 유통업자 화면 (대기 목록, 승인/거절)
- [ ] 에러 처리
- [ ] 로딩 상태 표시
- [ ] 사진 표시
- [ ] 상태별 UI 구분
- [ ] 새로고침 기능
- [ ] 상세 화면
- [ ] 수거 예약 화면

---

**구현 완료일: 2025-11-30**
**버전: 1.0.0**
