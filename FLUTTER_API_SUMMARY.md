# Flutter 개발자를 위한 견적 요청 API 요약

## 🚀 빠른 시작

### Base URL
```
http://localhost:8080 (개발)
http://YOUR_SERVER_IP:8080 (프로덕션)
```

### 인증
모든 요청에 JWT 토큰 필요:
```dart
headers: {
  'Authorization': 'Bearer $accessToken',
  'Content-Type': 'application/json',
}
```

---

## 📋 API 엔드포인트 (7개)

### 매장용 API (4개)

| 기능 | 메소드 | 엔드포인트 | 권한 |
|------|--------|-----------|------|
| 견적 요청 생성 | POST | `/api/matching/quote-request` | STORE_OWNER |
| 내 견적 목록 조회 | GET | `/api/matching/quote-requests/store` | STORE_OWNER |
| 견적 완료 처리 | PUT | `/api/matching/quote-request/{id}/complete` | STORE_OWNER |
| 견적 요청 취소 | DELETE | `/api/matching/quote-request/{id}` | STORE_OWNER |

### 유통업체용 API (1개)

| 기능 | 메소드 | 엔드포인트 | 권한 |
|------|--------|-----------|------|
| 받은 견적 목록 조회 | GET | `/api/matching/quote-requests/distributor` | DISTRIBUTOR |
| 견적 응답 (수락/거절) | PUT | `/api/matching/quote-request/{id}/respond` | DISTRIBUTOR |

### 공통 API (1개)

| 기능 | 메소드 | 엔드포인트 | 권한 |
|------|--------|-----------|------|
| 견적 상세 조회 | GET | `/api/matching/quote-request/{id}` | STORE_OWNER, DISTRIBUTOR |

---

## 💾 Dart 모델

```dart
class QuoteRequest {
  final int id;
  final String storeId;
  final String storeName;
  final String distributorId;
  final String distributorName;
  final String requestedProducts;
  final String? message;
  final String status; // PENDING, ACCEPTED, REJECTED, COMPLETED
  final int? estimatedAmount;
  final String? distributorResponse;
  final DateTime requestedAt;
  final DateTime? respondedAt;

  factory QuoteRequest.fromJson(Map<String, dynamic> json) {
    return QuoteRequest(
      id: json['id'],
      storeId: json['storeId'],
      storeName: json['storeName'],
      distributorId: json['distributorId'],
      distributorName: json['distributorName'],
      requestedProducts: json['requestedProducts'],
      message: json['message'],
      status: json['status'],
      estimatedAmount: json['estimatedAmount'],
      distributorResponse: json['distributorResponse'],
      requestedAt: DateTime.parse(json['requestedAt']),
      respondedAt: json['respondedAt'] != null 
          ? DateTime.parse(json['respondedAt']) 
          : null,
    );
  }
}
```

---

## 🔄 워크플로우

### 매장 앱
```
1. 추천 유통업체 조회
   GET /api/matching/recommend
   
2. 견적 요청 생성
   POST /api/matching/quote-request
   Body: { distributorId, requestedProducts, message }
   
3. 견적 목록 확인
   GET /api/matching/quote-requests/store
   
4. 수락된 견적 완료 처리
   PUT /api/matching/quote-request/{id}/complete
```

### 유통업체 앱
```
1. 받은 견적 목록 확인
   GET /api/matching/quote-requests/distributor
   
2. 견적 응답 (수락)
   PUT /api/matching/quote-request/{id}/respond
   Body: { status: "ACCEPTED", estimatedAmount: 500000, response: "..." }
   
   또는 견적 응답 (거절)
   PUT /api/matching/quote-request/{id}/respond
   Body: { status: "REJECTED", response: "..." }
```

---

## 📊 견적 상태

| 상태 | 한글 | 색상 | 설명 |
|------|------|------|------|
| PENDING | 대기중 | 🟠 Orange | 유통업체 응답 대기 |
| ACCEPTED | 수락됨 | 🟢 Green | 유통업체가 수락 |
| REJECTED | 거절됨 | 🔴 Red | 유통업체가 거절 |
| COMPLETED | 완료됨 | 🔵 Blue | 매장이 완료 처리 |

---

## 🎯 주요 요청 예시

### 1. 견적 요청 생성 (매장)
```dart
POST /api/matching/quote-request

{
  "distributorId": "distributor1",
  "requestedProducts": "쌀/곡물,채소,육류",
  "message": "매주 월요일 오전 배송 가능한지 확인 부탁드립니다."
}
```

### 2. 견적 응답 - 수락 (유통업체)
```dart
PUT /api/matching/quote-request/1/respond

{
  "status": "ACCEPTED",
  "estimatedAmount": 500000,
  "response": "매주 월요일 오전 8시 배송 가능합니다."
}
```

### 3. 견적 응답 - 거절 (유통업체)
```dart
PUT /api/matching/quote-request/1/respond

{
  "status": "REJECTED",
  "estimatedAmount": null,
  "response": "죄송합니다. 현재 재고가 부족합니다."
}
```

---

## ⚠️ 주의사항

1. **UTF-8 인코딩 필수**
   ```dart
   jsonDecode(utf8.decode(response.bodyBytes))
   ```

2. **상태 전이 규칙**
   - PENDING → ACCEPTED/REJECTED (유통업체만)
   - ACCEPTED → COMPLETED (매장만)
   - PENDING 상태만 취소 가능 (매장만)

3. **권한 체크**
   - 매장은 자신의 견적만 조회/수정/삭제
   - 유통업체는 자신에게 온 견적만 응답

4. **에러 처리**
   - 401: 토큰 만료 → 재로그인
   - 403: 권한 없음
   - 404: 리소스 없음
   - 400: 잘못된 요청

---

## 📱 UI 구현 체크리스트

### 매장 앱
- [ ] 견적 요청 생성 화면
  - [ ] 유통업체 선택
  - [ ] 품목 선택 (체크박스/칩)
  - [ ] 추가 요청사항 입력
- [ ] 견적 요청 목록 화면
  - [ ] 상태별 필터링
  - [ ] 상태별 색상 구분
  - [ ] 최신 순 정렬
- [ ] 견적 상세 화면
  - [ ] 유통업체 정보 표시
  - [ ] 응답 내용 표시
  - [ ] 취소 버튼 (PENDING)
  - [ ] 완료 버튼 (ACCEPTED)

### 유통업체 앱
- [ ] 견적 요청 목록 화면
  - [ ] 대기중 요청 우선 표시
  - [ ] 매장 정보 표시
- [ ] 견적 응답 화면
  - [ ] 수락/거절 선택
  - [ ] 예상 금액 입력
  - [ ] 응답 메시지 입력
  - [ ] 확인 다이얼로그

---

## 📚 상세 문서

- **전체 API 명세**: `FLUTTER_QUOTE_REQUEST_API.md`
- **백엔드 문서**: `BACKEND_API_DOCUMENTATION.md`
- **사용 가이드**: `QUOTE_REQUEST_API_GUIDE.md`

---

## 🧪 테스트

백엔드 테스트 스크립트로 API 동작 확인:
```bash
./test-quote-request-api.sh
```

---

## 💡 개발 팁

1. **서비스 클래스 분리**
   - `QuoteRequestService`: API 호출 로직
   - `QuoteRequestProvider`: 상태 관리 (Provider/Riverpod)

2. **에러 처리 통일**
   - 공통 에러 핸들러 함수 작성
   - 사용자 친화적 에러 메시지

3. **로딩 상태 관리**
   - API 호출 중 로딩 인디케이터 표시
   - 중복 요청 방지

4. **새로고침**
   - Pull-to-refresh 구현
   - 자동 새로고침 (주기적 폴링 또는 WebSocket)

---

