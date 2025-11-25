# 견적 요청 API - Flutter 개발 가이드

## 🎯 개요

매장과 유통업체 간의 견적 요청 및 응답 기능을 위한 REST API입니다.

**Base URL**: `http://localhost:8080` (개발) / `http://YOUR_SERVER_IP:8080` (프로덕션)

---

## 🔐 인증

모든 API는 JWT 토큰 인증이 필요합니다.

```dart
// 헤더에 토큰 포함
headers: {
  'Authorization': 'Bearer $accessToken',
  'Content-Type': 'application/json',
}
```

---

## 📋 API 목록

### 1. 견적 요청 생성 (매장 → 유통업체)

**엔드포인트**: `POST /api/matching/quote-request`  
**권한**: ROLE_STORE_OWNER (매장 사장님만)

#### 요청 예시
```dart
final response = await http.post(
  Uri.parse('$baseUrl/api/matching/quote-request'),
  headers: {
    'Authorization': 'Bearer $storeToken',
    'Content-Type': 'application/json',
  },
  body: jsonEncode({
    'distributorId': 'distributor1',
    'requestedProducts': '쌀/곡물,채소,육류',
    'message': '매주 월요일 오전 배송 가능한지 확인 부탁드립니다.',
  }),
);
```

#### 요청 Body
```json
{
  "distributorId": "distributor1",
  "requestedProducts": "쌀/곡물,채소,육류",
  "message": "매주 월요일 오전 배송 가능한지 확인 부탁드립니다."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| distributorId | String | ✅ | 유통업체 ID |
| requestedProducts | String | ✅ | 요청 품목 (콤마로 구분) |
| message | String | ❌ | 추가 요청사항 |

#### 응답 (200 OK)
```json
{
  "id": 1,
  "storeId": "store_owner1",
  "storeName": "맛있는 한식당",
  "distributorId": "distributor1",
  "distributorName": "신선식자재 유통",
  "requestedProducts": "쌀/곡물,채소,육류",
  "message": "매주 월요일 오전 배송 가능한지 확인 부탁드립니다.",
  "status": "PENDING",
  "estimatedAmount": null,
  "distributorResponse": null,
  "requestedAt": "2025-11-26T10:30:00",
  "respondedAt": null
}
```

#### Dart 모델
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

  QuoteRequest({
    required this.id,
    required this.storeId,
    required this.storeName,
    required this.distributorId,
    required this.distributorName,
    required this.requestedProducts,
    this.message,
    required this.status,
    this.estimatedAmount,
    this.distributorResponse,
    required this.requestedAt,
    this.respondedAt,
  });

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

### 2. 매장의 견적 요청 목록 조회

**엔드포인트**: `GET /api/matching/quote-requests/store`  
**권한**: ROLE_STORE_OWNER

#### 요청 예시
```dart
final response = await http.get(
  Uri.parse('$baseUrl/api/matching/quote-requests/store'),
  headers: {
    'Authorization': 'Bearer $storeToken',
  },
);

if (response.statusCode == 200) {
  final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
  final quotes = data.map((json) => QuoteRequest.fromJson(json)).toList();
}
```

#### 응답 (200 OK)
```json
[
  {
    "id": 1,
    "storeId": "store_owner1",
    "storeName": "맛있는 한식당",
    "distributorId": "distributor1",
    "distributorName": "신선식자재 유통",
    "requestedProducts": "쌀/곡물,채소,육류",
    "message": "매주 월요일 오전 배송 가능한지 확인 부탁드립니다.",
    "status": "ACCEPTED",
    "estimatedAmount": 500000,
    "distributorResponse": "매주 월요일 오전 8시 배송 가능합니다.",
    "requestedAt": "2025-11-26T10:30:00",
    "respondedAt": "2025-11-26T11:00:00"
  }
]
```

**참고**: 최신 순으로 정렬되어 반환됩니다.

---

### 3. 유통업체의 견적 요청 목록 조회

**엔드포인트**: `GET /api/matching/quote-requests/distributor`  
**권한**: ROLE_DISTRIBUTOR

#### 요청 예시
```dart
final response = await http.get(
  Uri.parse('$baseUrl/api/matching/quote-requests/distributor'),
  headers: {
    'Authorization': 'Bearer $distributorToken',
  },
);

if (response.statusCode == 200) {
  final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
  final quotes = data.map((json) => QuoteRequest.fromJson(json)).toList();
}
```

#### 응답
매장 목록 조회와 동일한 형식

---

### 4. 견적 요청 상세 조회

**엔드포인트**: `GET /api/matching/quote-request/{id}`  
**권한**: ROLE_STORE_OWNER, ROLE_DISTRIBUTOR

#### 요청 예시
```dart
final response = await http.get(
  Uri.parse('$baseUrl/api/matching/quote-request/$quoteId'),
  headers: {
    'Authorization': 'Bearer $token',
  },
);

if (response.statusCode == 200) {
  final quote = QuoteRequest.fromJson(
    jsonDecode(utf8.decode(response.bodyBytes))
  );
}
```

#### 응답
단일 견적 요청 객체 반환

---

### 5. 견적 요청 응답 (유통업체)

**엔드포인트**: `PUT /api/matching/quote-request/{id}/respond`  
**권한**: ROLE_DISTRIBUTOR

#### 요청 예시 - 수락
```dart
final response = await http.put(
  Uri.parse('$baseUrl/api/matching/quote-request/$quoteId/respond'),
  headers: {
    'Authorization': 'Bearer $distributorToken',
    'Content-Type': 'application/json',
  },
  body: jsonEncode({
    'status': 'ACCEPTED',
    'estimatedAmount': 500000,
    'response': '매주 월요일 오전 8시 배송 가능합니다. 최소 주문금액은 10만원입니다.',
  }),
);
```

#### 요청 예시 - 거절
```dart
final response = await http.put(
  Uri.parse('$baseUrl/api/matching/quote-request/$quoteId/respond'),
  headers: {
    'Authorization': 'Bearer $distributorToken',
    'Content-Type': 'application/json',
  },
  body: jsonEncode({
    'status': 'REJECTED',
    'estimatedAmount': null,
    'response': '죄송합니다. 현재 재고가 부족하여 배송이 어렵습니다.',
  }),
);
```

#### 요청 Body
```json
{
  "status": "ACCEPTED",
  "estimatedAmount": 500000,
  "response": "매주 월요일 오전 8시 배송 가능합니다."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| status | String | ✅ | "ACCEPTED" 또는 "REJECTED" |
| estimatedAmount | int | ❌ | 예상 금액 (수락 시 권장) |
| response | String | ✅ | 응답 메시지 |

#### 응답 (200 OK)
업데이트된 견적 요청 객체 반환

#### 에러
- `403`: 권한 없음 (다른 유통업체의 견적)
- `400`: 이미 응답한 견적 요청

---

### 6. 견적 완료 처리 (매장)

**엔드포인트**: `PUT /api/matching/quote-request/{id}/complete`  
**권한**: ROLE_STORE_OWNER

#### 요청 예시
```dart
final response = await http.put(
  Uri.parse('$baseUrl/api/matching/quote-request/$quoteId/complete'),
  headers: {
    'Authorization': 'Bearer $storeToken',
  },
);

if (response.statusCode == 200) {
  final completedQuote = QuoteRequest.fromJson(
    jsonDecode(utf8.decode(response.bodyBytes))
  );
  // 상태가 COMPLETED로 변경됨
}
```

#### 응답 (200 OK)
상태가 COMPLETED로 변경된 견적 요청 객체

#### 에러
- `403`: 권한 없음 (다른 매장의 견적)
- `400`: 수락된 견적만 완료 가능

---

### 7. 견적 요청 취소 (매장)

**엔드포인트**: `DELETE /api/matching/quote-request/{id}`  
**권한**: ROLE_STORE_OWNER

#### 요청 예시
```dart
final response = await http.delete(
  Uri.parse('$baseUrl/api/matching/quote-request/$quoteId'),
  headers: {
    'Authorization': 'Bearer $storeToken',
  },
);

if (response.statusCode == 200) {
  // 견적 요청이 삭제됨
  print('견적 요청이 취소되었습니다.');
}
```

#### 응답 (200 OK)
```
견적 요청이 취소되었습니다.
```

#### 에러
- `403`: 권한 없음 (다른 매장의 견적)
- `400`: 대기중인 견적만 취소 가능

---

## 📊 견적 상태 (Status)

| 상태 | 설명 | 다음 가능 상태 |
|------|------|---------------|
| `PENDING` | 대기중 | ACCEPTED, REJECTED, 취소 |
| `ACCEPTED` | 수락됨 | COMPLETED |
| `REJECTED` | 거절됨 | 종료 |
| `COMPLETED` | 완료됨 | 종료 |

### 상태별 UI 표시 예시
```dart
String getStatusText(String status) {
  switch (status) {
    case 'PENDING':
      return '대기중';
    case 'ACCEPTED':
      return '수락됨';
    case 'REJECTED':
      return '거절됨';
    case 'COMPLETED':
      return '완료됨';
    default:
      return '알 수 없음';
  }
}

Color getStatusColor(String status) {
  switch (status) {
    case 'PENDING':
      return Colors.orange;
    case 'ACCEPTED':
      return Colors.green;
    case 'REJECTED':
      return Colors.red;
    case 'COMPLETED':
      return Colors.blue;
    default:
      return Colors.grey;
  }
}
```

---

## 🔄 사용 시나리오

### 매장 앱 흐름
```
1. 추천 유통업체 조회 (GET /api/matching/recommend)
   ↓
2. 견적 요청 생성 (POST /api/matching/quote-request)
   ↓
3. 견적 요청 목록 확인 (GET /api/matching/quote-requests/store)
   ↓
4. 수락된 견적 확인 후 완료 처리 (PUT /api/matching/quote-request/{id}/complete)
```

### 유통업체 앱 흐름
```
1. 받은 견적 요청 목록 확인 (GET /api/matching/quote-requests/distributor)
   ↓
2. 견적 요청 상세 확인 (GET /api/matching/quote-request/{id})
   ↓
3. 견적 응답 (PUT /api/matching/quote-request/{id}/respond)
   - 수락: status = "ACCEPTED", estimatedAmount 포함
   - 거절: status = "REJECTED", response에 거절 사유
```

---

## 🛠️ 에러 처리

### 공통 에러 코드
```dart
Future<void> handleQuoteRequest() async {
  try {
    final response = await http.post(...);
    
    if (response.statusCode == 200) {
      // 성공
    } else if (response.statusCode == 401) {
      // 토큰 만료 → 재로그인 필요
      navigateToLogin();
    } else if (response.statusCode == 403) {
      // 권한 없음
      showError('권한이 없습니다.');
    } else if (response.statusCode == 404) {
      // 리소스 없음
      showError('견적 요청을 찾을 수 없습니다.');
    } else if (response.statusCode == 400) {
      // 잘못된 요청
      final error = jsonDecode(response.body);
      showError(error['message'] ?? '잘못된 요청입니다.');
    } else {
      // 서버 오류
      showError('서버 오류가 발생했습니다.');
    }
  } catch (e) {
    // 네트워크 오류
    showError('네트워크 연결을 확인해주세요.');
  }
}
```

---

## 💡 UI/UX 권장사항

### 매장 앱
1. **견적 요청 생성 화면**
   - 유통업체 선택 (추천 목록에서)
   - 품목 선택 (체크박스 또는 칩)
   - 추가 요청사항 입력 (텍스트 필드)

2. **견적 요청 목록 화면**
   - 상태별 필터링 (전체/대기중/수락됨/거절됨/완료됨)
   - 상태별 색상 구분
   - 최신 순 정렬

3. **견적 상세 화면**
   - 유통업체 정보
   - 요청 품목 및 메시지
   - 응답 내용 (수락/거절 시)
   - 예상 금액 (수락 시)
   - 액션 버튼:
     - PENDING: 취소 버튼
     - ACCEPTED: 완료 처리 버튼
     - REJECTED/COMPLETED: 버튼 없음

### 유통업체 앱
1. **견적 요청 목록 화면**
   - 대기중 요청 우선 표시
   - 매장 정보 미리보기
   - 요청 품목 표시

2. **견적 응답 화면**
   - 수락/거절 선택
   - 예상 금액 입력 (수락 시)
   - 응답 메시지 입력 (필수)
   - 확인 다이얼로그

---

## 📱 완성된 서비스 예시

```dart
class QuoteRequestService {
  final String baseUrl;
  final String token;

  QuoteRequestService({required this.baseUrl, required this.token});

  // 견적 요청 생성
  Future<QuoteRequest> createQuoteRequest({
    required String distributorId,
    required String requestedProducts,
    String? message,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/matching/quote-request'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'distributorId': distributorId,
        'requestedProducts': requestedProducts,
        'message': message,
      }),
    );

    if (response.statusCode == 200) {
      return QuoteRequest.fromJson(
        jsonDecode(utf8.decode(response.bodyBytes))
      );
    } else {
      throw Exception('견적 요청 생성 실패');
    }
  }

  // 매장의 견적 요청 목록 조회
  Future<List<QuoteRequest>> getStoreQuoteRequests() async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/matching/quote-requests/store'),
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((json) => QuoteRequest.fromJson(json)).toList();
    } else {
      throw Exception('견적 요청 목록 조회 실패');
    }
  }

  // 유통업체의 견적 요청 목록 조회
  Future<List<QuoteRequest>> getDistributorQuoteRequests() async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/matching/quote-requests/distributor'),
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((json) => QuoteRequest.fromJson(json)).toList();
    } else {
      throw Exception('견적 요청 목록 조회 실패');
    }
  }

  // 견적 응답 (유통업체)
  Future<QuoteRequest> respondToQuoteRequest({
    required int quoteId,
    required String status, // ACCEPTED or REJECTED
    int? estimatedAmount,
    required String response,
  }) async {
    final res = await http.put(
      Uri.parse('$baseUrl/api/matching/quote-request/$quoteId/respond'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'status': status,
        'estimatedAmount': estimatedAmount,
        'response': response,
      }),
    );

    if (res.statusCode == 200) {
      return QuoteRequest.fromJson(
        jsonDecode(utf8.decode(res.bodyBytes))
      );
    } else {
      throw Exception('견적 응답 실패');
    }
  }

  // 견적 완료 처리 (매장)
  Future<QuoteRequest> completeQuoteRequest(int quoteId) async {
    final response = await http.put(
      Uri.parse('$baseUrl/api/matching/quote-request/$quoteId/complete'),
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      return QuoteRequest.fromJson(
        jsonDecode(utf8.decode(response.bodyBytes))
      );
    } else {
      throw Exception('견적 완료 처리 실패');
    }
  }

  // 견적 요청 취소 (매장)
  Future<void> cancelQuoteRequest(int quoteId) async {
    final response = await http.delete(
      Uri.parse('$baseUrl/api/matching/quote-request/$quoteId'),
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode != 200) {
      throw Exception('견적 요청 취소 실패');
    }
  }
}
```

---

## 🔗 관련 API

견적 요청 전에 사용할 API:
- `GET /api/matching/recommend`: 맞춤 유통업체 추천
- `GET /api/matching/search/product?keyword=쌀`: 품목별 유통업체 검색
- `GET /api/matching/search/region?keyword=서울`: 지역별 유통업체 검색

---
