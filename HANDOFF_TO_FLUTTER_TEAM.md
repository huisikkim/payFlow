# Flutter 팀 인계 문서 - 견적 요청 API

## 📦 전달 내용

백엔드에서 **견적 요청 API**를 완성했습니다. 이제 Flutter 앱에서 이 API를 호출하여 UI를 구현하면 됩니다.

---

## 📄 문서 목록

### 1. **FLUTTER_API_SUMMARY.md** ⭐ 먼저 읽으세요!
- API 엔드포인트 7개 요약
- Dart 모델 코드
- 요청/응답 예시
- UI 구현 체크리스트

### 2. **FLUTTER_QUOTE_REQUEST_API.md** 📖 상세 명세
- 각 API별 상세 설명
- 완전한 Dart 코드 예시
- 에러 처리 방법
- 완성된 서비스 클래스 코드

### 3. **BACKEND_API_DOCUMENTATION.md** 🔍 전체 백엔드 문서
- 전체 백엔드 API 문서
- "6. 견적 요청 API" 섹션 참고

---

## 🎯 구현해야 할 기능

### 매장 앱 (4개 화면)

#### 1. 견적 요청 생성 화면
```
- 유통업체 선택 (추천 목록에서)
- 품목 선택 (체크박스 또는 칩)
- 추가 요청사항 입력
- [견적 요청하기] 버튼

API: POST /api/matching/quote-request
```

#### 2. 견적 요청 목록 화면
```
- 상태별 탭 (전체/대기중/수락됨/거절됨/완료됨)
- 각 견적 카드:
  - 유통업체명
  - 요청 품목
  - 상태 (색상 구분)
  - 요청 날짜
- 탭하면 상세 화면으로 이동

API: GET /api/matching/quote-requests/store
```

#### 3. 견적 상세 화면 (매장)
```
- 유통업체 정보
- 요청 품목 및 메시지
- 응답 내용 (있으면)
- 예상 금액 (수락 시)
- 액션 버튼:
  - PENDING: [취소하기]
  - ACCEPTED: [완료 처리]
  - REJECTED/COMPLETED: 버튼 없음

API: 
- GET /api/matching/quote-request/{id}
- PUT /api/matching/quote-request/{id}/complete
- DELETE /api/matching/quote-request/{id}
```

### 유통업체 앱 (2개 화면)

#### 4. 견적 요청 목록 화면 (유통업체)
```
- 대기중 요청 우선 표시
- 각 견적 카드:
  - 매장명
  - 요청 품목
  - 요청 메시지 미리보기
  - 요청 날짜
- 탭하면 응답 화면으로 이동

API: GET /api/matching/quote-requests/distributor
```

#### 5. 견적 응답 화면 (유통업체)
```
- 매장 정보 표시
- 요청 품목 및 메시지 표시
- 수락/거절 선택 (라디오 버튼)
- 예상 금액 입력 (수락 시)
- 응답 메시지 입력 (필수)
- [응답 보내기] 버튼

API: PUT /api/matching/quote-request/{id}/respond
```

---

## 🚀 빠른 시작 가이드

### Step 1: Dart 모델 생성
```dart
// lib/models/quote_request.dart
class QuoteRequest {
  final int id;
  final String storeId;
  final String storeName;
  final String distributorId;
  final String distributorName;
  final String requestedProducts;
  final String? message;
  final String status;
  final int? estimatedAmount;
  final String? distributorResponse;
  final DateTime requestedAt;
  final DateTime? respondedAt;

  // fromJson 메소드 포함
}
```

### Step 2: API 서비스 생성
```dart
// lib/services/quote_request_service.dart
class QuoteRequestService {
  final String baseUrl = 'http://YOUR_SERVER_IP:8080';
  
  Future<QuoteRequest> createQuoteRequest(...) async { }
  Future<List<QuoteRequest>> getStoreQuoteRequests() async { }
  Future<List<QuoteRequest>> getDistributorQuoteRequests() async { }
  Future<QuoteRequest> respondToQuoteRequest(...) async { }
  Future<QuoteRequest> completeQuoteRequest(int id) async { }
  Future<void> cancelQuoteRequest(int id) async { }
}
```

### Step 3: UI 구현
```dart
// lib/screens/quote_request_list_screen.dart
// lib/screens/quote_request_create_screen.dart
// lib/screens/quote_request_detail_screen.dart
// lib/screens/quote_response_screen.dart
```

---

## 📋 API 엔드포인트 요약

| 기능 | 메소드 | 엔드포인트 | 사용자 |
|------|--------|-----------|--------|
| 견적 요청 생성 | POST | `/api/matching/quote-request` | 매장 |
| 매장 견적 목록 | GET | `/api/matching/quote-requests/store` | 매장 |
| 유통업체 견적 목록 | GET | `/api/matching/quote-requests/distributor` | 유통업체 |
| 견적 상세 조회 | GET | `/api/matching/quote-request/{id}` | 둘 다 |
| 견적 응답 | PUT | `/api/matching/quote-request/{id}/respond` | 유통업체 |
| 견적 완료 | PUT | `/api/matching/quote-request/{id}/complete` | 매장 |
| 견적 취소 | DELETE | `/api/matching/quote-request/{id}` | 매장 |

---

## 🎨 UI/UX 가이드

### 상태별 색상
```dart
Color getStatusColor(String status) {
  switch (status) {
    case 'PENDING': return Colors.orange;    // 🟠 대기중
    case 'ACCEPTED': return Colors.green;    // 🟢 수락됨
    case 'REJECTED': return Colors.red;      // 🔴 거절됨
    case 'COMPLETED': return Colors.blue;    // 🔵 완료됨
    default: return Colors.grey;
  }
}
```

### 상태별 텍스트
```dart
String getStatusText(String status) {
  switch (status) {
    case 'PENDING': return '대기중';
    case 'ACCEPTED': return '수락됨';
    case 'REJECTED': return '거절됨';
    case 'COMPLETED': return '완료됨';
    default: return '알 수 없음';
  }
}
```

---

## ⚠️ 중요 사항

### 1. UTF-8 인코딩 필수
한글 깨짐 방지:
```dart
final data = jsonDecode(utf8.decode(response.bodyBytes));
```

### 2. 인증 토큰
모든 요청에 JWT 토큰 포함:
```dart
headers: {
  'Authorization': 'Bearer $accessToken',
  'Content-Type': 'application/json',
}
```

### 3. 상태 전이 규칙
- PENDING → ACCEPTED/REJECTED (유통업체만)
- ACCEPTED → COMPLETED (매장만)
- PENDING만 취소 가능 (매장만)

### 4. 에러 처리
```dart
- 401: 토큰 만료 → 재로그인
- 403: 권한 없음
- 404: 리소스 없음
- 400: 잘못된 요청
```

---

## 🧪 테스트 방법

### 백엔드 테스트
```bash
# 서버 실행 확인
curl http://localhost:8080/api/matching/recommend

# 견적 요청 API 테스트
./test-quote-request-api.sh
```

### Flutter 테스트 시나리오
1. 매장 로그인 → 견적 요청 생성 → 목록 확인
2. 유통업체 로그인 → 받은 견적 확인 → 수락 응답
3. 매장 로그인 → 수락된 견적 확인 → 완료 처리

---

## 📊 데이터 흐름

```
[매장 앱]
  ↓ POST /api/matching/quote-request
[백엔드] 견적 요청 생성 (PENDING)
  ↓
[유통업체 앱]
  ↓ GET /api/matching/quote-requests/distributor
[유통업체 앱] 견적 목록 확인
  ↓ PUT /api/matching/quote-request/{id}/respond
[백엔드] 견적 응답 (ACCEPTED/REJECTED)
  ↓
[매장 앱]
  ↓ GET /api/matching/quote-requests/store
[매장 앱] 응답 확인
  ↓ PUT /api/matching/quote-request/{id}/complete
[백엔드] 견적 완료 (COMPLETED)
```

---

## 💡 개발 팁

1. **상태 관리**
   - Provider 또는 Riverpod 사용 권장
   - 견적 목록 캐싱으로 성능 향상

2. **새로고침**
   - Pull-to-refresh 구현
   - 주기적 폴링 (30초마다) 또는 WebSocket

3. **로딩 상태**
   - API 호출 중 로딩 인디케이터
   - 중복 요청 방지

4. **에러 처리**
   - 공통 에러 핸들러 함수
   - 사용자 친화적 메시지

---

## 📞 백엔드 담당자

- **서버 주소**: `http://localhost:8080` (개발)
- **API 문서**: `BACKEND_API_DOCUMENTATION.md`
- **테스트 스크립트**: `test-quote-request-api.sh`

API 관련 문의사항이나 버그 발견 시 백엔드 팀에 연락주세요.

---

## ✅ 체크리스트

### 백엔드 (완료)
- [x] 견적 요청 엔티티 생성
- [x] 견적 요청 API 7개 구현
- [x] 권한 체크 및 상태 전이 로직
- [x] 테스트 스크립트 작성
- [x] API 문서 작성

### 프론트엔드 (TODO)
- [ ] Dart 모델 생성
- [ ] API 서비스 클래스 작성
- [ ] 매장 앱 UI 구현 (3개 화면)
- [ ] 유통업체 앱 UI 구현 (2개 화면)
- [ ] 에러 처리 및 로딩 상태
- [ ] 통합 테스트

---

## 🎉 시작하세요!

1. `FLUTTER_API_SUMMARY.md` 읽기
2. `FLUTTER_QUOTE_REQUEST_API.md`에서 코드 복사
3. UI 구현 시작
4. 테스트 및 피드백

