# 견적 요청 API 가이드

## 📋 개요

매장과 유통업체 간의 견적 요청 및 응답 기능을 제공하는 API입니다.

## 🔄 워크플로우

```
1. [매장] 추천 유통업체 조회
   ↓
2. [매장] 견적 요청 생성 (PENDING)
   ↓
3. [유통업체] 견적 요청 목록 확인
   ↓
4. [유통업체] 견적 응답
   ├─ ACCEPTED (수락) → [매장] 완료 처리 → COMPLETED
   └─ REJECTED (거절) → 종료
   
※ [매장] 대기중 요청 취소 가능 (PENDING 상태만)
```

## 🎯 주요 기능

### 1. 견적 요청 생성 (매장)
- **엔드포인트**: `POST /api/matching/quote-request`
- **권한**: ROLE_STORE_OWNER
- **요청 데이터**:
  - `distributorId`: 유통업체 ID
  - `requestedProducts`: 요청 품목 (콤마 구분)
  - `message`: 추가 요청사항

### 2. 견적 요청 목록 조회
- **매장**: `GET /api/matching/quote-requests/store`
- **유통업체**: `GET /api/matching/quote-requests/distributor`

### 3. 견적 응답 (유통업체)
- **엔드포인트**: `PUT /api/matching/quote-request/{id}/respond`
- **권한**: ROLE_DISTRIBUTOR
- **응답 데이터**:
  - `status`: ACCEPTED 또는 REJECTED
  - `estimatedAmount`: 예상 금액
  - `response`: 응답 메시지

### 4. 견적 완료 처리 (매장)
- **엔드포인트**: `PUT /api/matching/quote-request/{id}/complete`
- **권한**: ROLE_STORE_OWNER
- **조건**: 수락된 견적만 완료 가능

### 5. 견적 요청 취소 (매장)
- **엔드포인트**: `DELETE /api/matching/quote-request/{id}`
- **권한**: ROLE_STORE_OWNER
- **조건**: 대기중인 견적만 취소 가능

## 📊 견적 상태 (QuoteStatus)

| 상태 | 설명 | 다음 가능 상태 |
|------|------|---------------|
| PENDING | 대기중 | ACCEPTED, REJECTED, 취소 |
| ACCEPTED | 수락됨 | COMPLETED |
| REJECTED | 거절됨 | 종료 |
| COMPLETED | 완료됨 | 종료 |

## 🧪 테스트

```bash
# 견적 요청 API 전체 테스트
./test-quote-request-api.sh
```

테스트 시나리오:
1. ✅ 견적 요청 생성
2. ✅ 견적 요청 목록 조회 (매장/유통업체)
3. ✅ 견적 요청 상세 조회
4. ✅ 견적 응답 (수락)
5. ✅ 견적 완료 처리
6. ✅ 견적 응답 (거절)
7. ✅ 견적 요청 취소

## 💡 사용 예시

### 매장 사용 흐름
```bash
# 1. 추천 유통업체 조회
curl -X GET "http://localhost:8080/api/matching/recommend?limit=10" \
  -H "Authorization: Bearer {STORE_TOKEN}"

# 2. 견적 요청 생성
curl -X POST http://localhost:8080/api/matching/quote-request \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {STORE_TOKEN}" \
  -d '{
    "distributorId": "distributor1",
    "requestedProducts": "쌀/곡물,채소,육류",
    "message": "매주 월요일 오전 배송 가능한지 확인 부탁드립니다."
  }'

# 3. 내 견적 요청 목록 확인
curl -X GET http://localhost:8080/api/matching/quote-requests/store \
  -H "Authorization: Bearer {STORE_TOKEN}"

# 4. 수락된 견적 완료 처리
curl -X PUT http://localhost:8080/api/matching/quote-request/1/complete \
  -H "Authorization: Bearer {STORE_TOKEN}"
```

### 유통업체 사용 흐름
```bash
# 1. 받은 견적 요청 목록 확인
curl -X GET http://localhost:8080/api/matching/quote-requests/distributor \
  -H "Authorization: Bearer {DISTRIBUTOR_TOKEN}"

# 2. 견적 응답 (수락)
curl -X PUT http://localhost:8080/api/matching/quote-request/1/respond \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {DISTRIBUTOR_TOKEN}" \
  -d '{
    "status": "ACCEPTED",
    "estimatedAmount": 500000,
    "response": "매주 월요일 오전 8시 배송 가능합니다."
  }'

# 3. 견적 응답 (거절)
curl -X PUT http://localhost:8080/api/matching/quote-request/2/respond \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {DISTRIBUTOR_TOKEN}" \
  -d '{
    "status": "REJECTED",
    "estimatedAmount": null,
    "response": "죄송합니다. 현재 재고가 부족합니다."
  }'
```

## 🔒 권한 및 보안

- 매장은 자신의 견적 요청만 조회/수정/삭제 가능
- 유통업체는 자신에게 온 견적 요청만 응답 가능
- JWT 토큰 기반 인증 필요
- 상태 전이 규칙 엄격히 적용

## 📁 구현 파일

### 도메인
- `QuoteRequest.java`: 견적 요청 엔티티
- `QuoteRequestRepository.java`: 견적 요청 리포지토리

### 서비스
- `QuoteRequestService.java`: 견적 요청 비즈니스 로직

### 컨트롤러
- `MatchingController.java`: 견적 요청 API 엔드포인트

### DTO
- `QuoteRequestDto.java`: 견적 요청 생성 DTO
- `QuoteResponseDto.java`: 견적 응답 DTO
