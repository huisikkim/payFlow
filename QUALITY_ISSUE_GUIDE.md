# 🔍 품질 이슈 신고 및 반품/교환 시스템

가게사장님이 상품 품질 문제를 빠르게 신고하고, 유통업자가 즉시 대응할 수 있는 **간편 반품/교환 시스템**입니다.

## 주요 특징

### 1. 간편한 품질 이슈 신고 (가게사장님)
- ✅ **빠른 신고**: 주문 ID, 품목 정보만으로 즉시 신고
- ✅ **사진 업로드**: 문제 상품 사진을 여러 장 첨부 가능
- ✅ **이슈 유형 선택**:
  - 품질 불량 (POOR_QUALITY)
  - 오배송 (WRONG_ITEM)
  - 파손 (DAMAGED)
  - 유통기한 임박/경과 (EXPIRED)
  - 수량 불일치 (QUANTITY_MISMATCH)
- ✅ **요청 액션**: 환불 또는 교환 선택

### 2. 유통업자 대응 시스템
- ✅ **실시간 알림**: 품질 이슈 접수 즉시 확인
- ✅ **사진 확인**: 가게사장님이 올린 사진으로 문제 파악
- ✅ **승인/거절**: 검토 후 승인 또는 거절 처리
- ✅ **당일 수거**: 승인 시 당일 수거 예약
- ✅ **빠른 처리**: 환불 또는 교환 완료

### 3. 품질 보증 정책
- ✅ **100% 환불 보장**: 품질 문제 시 전액 환불
- ✅ **당일 수거**: 승인 후 당일 수거 서비스
- ✅ **빠른 재배송**: 교환 시 신속한 재배송
- ✅ **투명한 프로세스**: 모든 단계 실시간 추적

## 프로세스 흐름

### 환불 프로세스
```
1. 가게사장님 품질 이슈 신고 (사진 첨부)
   ↓
2. 유통업자 사진 확인 및 검토
   ↓
3. 유통업자 승인 (또는 거절)
   ↓
4. 당일 수거 예약
   ↓
5. 수거 완료
   ↓
6. 환불 처리 완료
```

### 교환 프로세스
```
1. 가게사장님 품질 이슈 신고 (사진 첨부)
   ↓
2. 유통업자 사진 확인 및 검토
   ↓
3. 유통업자 승인 (또는 거절)
   ↓
4. 당일 수거 예약
   ↓
5. 수거 완료
   ↓
6. 새 제품 재배송
   ↓
7. 교환 완료
```

## API 엔드포인트

### 가게사장님 API

#### 품질 이슈 신고
```bash
POST /api/quality-issues
{
  "orderId": 123,
  "itemId": 456,
  "itemName": "양파 10kg",
  "storeId": "STORE_001",
  "storeName": "홍길동 식당",
  "distributorId": "DIST_001",
  "issueType": "POOR_QUALITY",
  "photoUrls": [
    "https://example.com/photo1.jpg",
    "https://example.com/photo2.jpg"
  ],
  "description": "양파가 썩었습니다. 절반 이상이 사용 불가능한 상태입니다.",
  "requestAction": "REFUND"
}
```

**응답:**
```json
{
  "id": 1,
  "orderId": 123,
  "itemId": 456,
  "itemName": "양파 10kg",
  "storeId": "STORE_001",
  "storeName": "홍길동 식당",
  "distributorId": "DIST_001",
  "issueType": "POOR_QUALITY",
  "issueTypeDescription": "품질 불량",
  "photoUrls": [
    "https://example.com/photo1.jpg",
    "https://example.com/photo2.jpg"
  ],
  "description": "양파가 썩었습니다. 절반 이상이 사용 불가능한 상태입니다.",
  "requestAction": "REFUND",
  "requestActionDescription": "환불",
  "status": "SUBMITTED",
  "statusDescription": "접수됨",
  "submittedAt": "2025-11-30T10:30:00"
}
```

#### 내 품질 이슈 목록 조회
```bash
GET /api/quality-issues/store/{storeId}
```

#### 품질 이슈 상세 조회
```bash
GET /api/quality-issues/{issueId}
```

### 유통업자 API

#### 대기 중인 품질 이슈 조회
```bash
GET /api/quality-issues/distributor/{distributorId}/pending
```

#### 전체 품질 이슈 조회
```bash
GET /api/quality-issues/distributor/{distributorId}
```

#### 검토 시작
```bash
POST /api/quality-issues/{issueId}/review
```

#### 승인
```bash
POST /api/quality-issues/{issueId}/approve
{
  "comment": "확인했습니다. 품질 문제가 맞습니다. 환불 처리하겠습니다."
}
```

#### 거절
```bash
POST /api/quality-issues/{issueId}/reject
{
  "comment": "사진 확인 결과 정상 제품으로 보입니다."
}
```

#### 수거 예약
```bash
POST /api/quality-issues/{issueId}/schedule-pickup
{
  "pickupTime": "2025-11-30T14:00:00"
}
```

#### 수거 완료
```bash
POST /api/quality-issues/{issueId}/complete-pickup
```

#### 환불/교환 완료
```bash
POST /api/quality-issues/{issueId}/complete-resolution
{
  "note": "환불 처리 완료했습니다. 다음 주문 시 할인 쿠폰을 제공하겠습니다."
}
```

## 이슈 상태 (Status)

| 상태 | 설명 | 다음 단계 |
|------|------|-----------|
| SUBMITTED | 접수됨 | 검토 시작 |
| REVIEWING | 검토 중 | 승인 또는 거절 |
| APPROVED | 승인됨 | 수거 예약 |
| REJECTED | 거절됨 | 종료 |
| PICKUP_SCHEDULED | 수거 예정 | 수거 완료 |
| PICKED_UP | 수거 완료 | 환불/교환 완료 |
| REFUNDED | 환불 완료 | 종료 |
| EXCHANGED | 교환 완료 | 종료 |

## 이슈 유형 (Issue Type)

| 유형 | 설명 | 예시 |
|------|------|------|
| POOR_QUALITY | 품질 불량 | 양파가 썩음, 고기가 상함 |
| WRONG_ITEM | 오배송 | 주문한 것과 다른 제품 배송 |
| DAMAGED | 파손 | 포장 파손, 제품 깨짐 |
| EXPIRED | 유통기한 임박/경과 | 유통기한이 지났거나 임박 |
| QUANTITY_MISMATCH | 수량 불일치 | 주문 수량과 배송 수량 불일치 |

## 요청 액션 (Request Action)

| 액션 | 설명 |
|------|------|
| REFUND | 환불 요청 |
| EXCHANGE | 교환 요청 |

## 테스트

### 전체 플로우 테스트
```bash
./test-quality-issue-api.sh
```

이 스크립트는 다음을 자동으로 테스트합니다:
1. 사용자 로그인
2. 품질 이슈 신고 (환불)
3. 품질 이슈 상세 조회
4. 가게별 품질 이슈 목록 조회
5. 유통사별 대기 중인 품질 이슈 조회
6. 품질 이슈 검토 시작
7. 품질 이슈 승인
8. 수거 예약
9. 수거 완료
10. 환불 완료
11. 품질 이슈 신고 (교환)
12. 교환 승인 및 완료
13. 유통사별 전체 품질 이슈 조회

### 수동 테스트 예시

#### 1. 환불 케이스
```bash
# 로그인
TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "password"}' \
  | jq -r '.accessToken')

# 품질 이슈 신고
curl -X POST "http://localhost:8080/api/quality-issues" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "orderId": 123,
    "itemId": 456,
    "itemName": "양파 10kg",
    "storeId": "STORE_001",
    "storeName": "홍길동 식당",
    "distributorId": "DIST_001",
    "issueType": "POOR_QUALITY",
    "photoUrls": ["https://example.com/photo1.jpg"],
    "description": "양파가 썩었습니다.",
    "requestAction": "REFUND"
  }'
```

#### 2. 교환 케이스
```bash
curl -X POST "http://localhost:8080/api/quality-issues" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "orderId": 124,
    "itemId": 457,
    "itemName": "감자 20kg",
    "storeId": "STORE_001",
    "storeName": "홍길동 식당",
    "distributorId": "DIST_001",
    "issueType": "DAMAGED",
    "photoUrls": ["https://example.com/potato1.jpg"],
    "description": "포장이 파손되어 감자가 흙투성이입니다.",
    "requestAction": "EXCHANGE"
  }'
```

## 도메인 모델

```
qualityissue/
├── domain/
│   ├── QualityIssue.java           # 품질 이슈 엔티티
│   ├── IssueType.java              # 이슈 유형 (품질불량, 오배송 등)
│   ├── IssueStatus.java            # 이슈 상태 (접수, 검토중, 승인 등)
│   ├── RequestAction.java          # 요청 액션 (환불, 교환)
│   └── QualityIssueRepository.java
├── application/
│   └── QualityIssueService.java    # 비즈니스 로직
└── presentation/
    ├── QualityIssueController.java # REST API
    └── dto/
        ├── SubmitIssueRequest.java
        ├── QualityIssueResponse.java
        ├── ReviewIssueRequest.java
        ├── SchedulePickupRequest.java
        └── CompleteResolutionRequest.java
```

## 비즈니스 로직

### 상태 전이 규칙
- SUBMITTED → REVIEWING: 검토 시작
- REVIEWING → APPROVED: 승인
- REVIEWING → REJECTED: 거절
- APPROVED → PICKUP_SCHEDULED: 수거 예약
- PICKUP_SCHEDULED → PICKED_UP: 수거 완료
- PICKED_UP → REFUNDED: 환불 완료 (환불 요청인 경우)
- PICKED_UP → EXCHANGED: 교환 완료 (교환 요청인 경우)

### 제약 조건
- 승인된 이슈만 수거 예약 가능
- 수거 예정 상태에서만 수거 완료 처리 가능
- 환불 요청은 환불 완료로만 처리 가능
- 교환 요청은 교환 완료로만 처리 가능

## 향후 확장 가능 기능

### 1. 사진 업로드 기능
- 실제 파일 업로드 API
- 이미지 리사이징
- S3 또는 로컬 스토리지 저장

### 2. 알림 시스템
- 품질 이슈 접수 시 유통업자에게 알림
- 승인/거절 시 가게사장님에게 알림
- 수거 예정 시간 알림
- 환불/교환 완료 알림

### 3. 통계 대시보드
- 품질 이슈 발생 빈도
- 이슈 유형별 통계
- 유통사별 품질 점수
- 평균 처리 시간

### 4. Kafka 이벤트 발행
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

## 기술 스택

- **Backend**: Spring Boot 3.5.7, Java 17
- **Database**: H2 (개발), JPA/Hibernate
- **Security**: Spring Security, JWT
- **Architecture**: DDD (Domain-Driven Design)
- **Pattern**: Repository Pattern, Service Layer

## 핵심 포인트

1. **간편한 신고**: 최소한의 정보로 빠르게 신고
2. **사진 증거**: 문제 상품 사진으로 명확한 증거 제시
3. **빠른 대응**: 당일 수거 및 처리
4. **투명한 프로세스**: 모든 단계 실시간 추적
5. **100% 보장**: 품질 문제 시 전액 환불 또는 교환
6. **DDD 패턴**: 명확한 도메인 모델과 비즈니스 로직 분리
7. **확장 가능**: 알림, 통계, 이벤트 발행 등 쉽게 추가 가능

## 실무 적용 가능성

이 시스템은 실제 B2B 식자재 유통 플랫폼에서 다음과 같이 활용 가능:
- 가게사장님 만족도 향상
- 유통업자 신뢰도 향상
- 품질 관리 자동화
- 고객 이탈 방지
- 분쟁 해결 프로세스 표준화
