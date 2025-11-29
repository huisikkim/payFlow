# 정산 시스템 API 문서 (Flutter 개발자용)

## 📋 목차
1. [개요](#개요)
2. [API 엔드포인트](#api-엔드포인트)
3. [데이터 모델](#데이터-모델)
4. [화면 구성 제안](#화면-구성-제안)
5. [사용 예시](#사용-예시)

---

## 개요

### 기능 설명
가게사장님과 유통업자 간의 거래 정산을 관리하는 시스템입니다.

### 주요 기능
- ✅ 개별 정산 내역 조회
- ✅ 일일 정산 통계 조회
- ✅ 기간별 정산 리포트
- ✅ 미수금 관리
- ✅ 정산 완료 처리

### Base URL
```
http://localhost:8080
```

---

## API 엔드포인트

### 1. 개별 정산 관리

#### 1.1 가게별 정산 목록 조회
**가게사장님이 자신의 정산 내역을 확인할 때 사용**

```http
GET /api/settlements/store/{storeId}
```

**Path Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| storeId | String | ✅ | 가게 ID |

**Response:**
```json
[
  {
    "settlementId": "SETTLE_abc123",
    "storeId": "store1",
    "distributorId": "dist1",
    "orderId": "ORD-20241129-143022-123",
    "settlementAmount": 150000,
    "outstandingAmount": 150000,
    "paidAmount": 0,
    "status": "PENDING",
    "settlementDate": "2024-11-29T14:30:22",
    "completedAt": null
  }
]
```

**Status 값:**
- `PENDING`: 정산 대기
- `PROCESSING`: 정산 처리중
- `COMPLETED`: 정산 완료
- `FAILED`: 정산 실패

---

#### 1.2 유통업자별 정산 목록 조회
**유통업자가 자신의 정산 내역을 확인할 때 사용**

```http
GET /api/settlements/distributor/{distributorId}
```

**Path Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| distributorId | String | ✅ | 유통업자 ID |

**Response:** (1.1과 동일)

---

#### 1.3 정산 상세 조회

```http
GET /api/settlements/{settlementId}
```

**Path Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| settlementId | String | ✅ | 정산 ID |

**Response:**
```json
{
  "settlementId": "SETTLE_abc123",
  "storeId": "store1",
  "distributorId": "dist1",
  "orderId": "ORD-20241129-143022-123",
  "settlementAmount": 150000,
  "outstandingAmount": 150000,
  "paidAmount": 0,
  "status": "PENDING",
  "settlementDate": "2024-11-29T14:30:22",
  "completedAt": null
}
```

---

#### 1.4 정산 완료 처리
**유통업자가 입금 완료 후 정산을 완료 처리할 때 사용**

```http
POST /api/settlements/{settlementId}/complete
Content-Type: application/json
```

**Path Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| settlementId | String | ✅ | 정산 ID |

**Request Body:**
```json
{
  "paidAmount": 150000
}
```

**Response:** `200 OK` (Body 없음)

---

#### 1.5 총 미수금 조회
**가게사장님이 전체 미수금을 확인할 때 사용**

```http
GET /api/settlements/store/{storeId}/outstanding
```

**Response:**
```json
{
  "totalOutstanding": 450000
}
```

---

### 2. 일일 정산 조회

#### 2.1 가게별 일일 정산 조회
**가게사장님이 일별 매출 현황을 확인할 때 사용**

```http
GET /api/daily-settlements/store/{storeId}?startDate={startDate}&endDate={endDate}
```

**Path Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| storeId | String | ✅ | 가게 ID |

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| startDate | String (YYYY-MM-DD) | ❌ | 30일 전 | 조회 시작일 |
| endDate | String (YYYY-MM-DD) | ❌ | 오늘 | 조회 종료일 |

**Response:**
```json
[
  {
    "id": 1,
    "settlementDate": "2024-11-29",
    "storeId": "store1",
    "distributorId": "dist1",
    "orderCount": 5,
    "totalSalesAmount": 750000,
    "totalSettlementAmount": 750000,
    "totalPaidAmount": 300000,
    "totalOutstandingAmount": 450000,
    "catalogOrderCount": 3,
    "catalogSalesAmount": 450000,
    "ingredientOrderCount": 2,
    "ingredientSalesAmount": 300000,
    "paymentRate": 40.0,
    "createdAt": "2024-11-29T14:30:22",
    "updatedAt": "2024-11-29T18:45:10"
  }
]
```

**필드 설명:**
- `orderCount`: 총 주문 건수
- `totalSalesAmount`: 총 매출액
- `totalPaidAmount`: 지불 완료 금액
- `totalOutstandingAmount`: 미수금
- `catalogOrderCount`: 카탈로그 주문 건수
- `catalogSalesAmount`: 카탈로그 매출액
- `ingredientOrderCount`: 식자재 주문 건수
- `ingredientSalesAmount`: 식자재 매출액
- `paymentRate`: 결제율 (%)

---

#### 2.2 유통업자별 일일 정산 조회
**유통업자가 일별 매출 현황을 확인할 때 사용**

```http
GET /api/daily-settlements/distributor/{distributorId}?startDate={startDate}&endDate={endDate}
```

**Parameters:** (2.1과 동일)
**Response:** (2.1과 동일)

---

### 3. 정산 통계

#### 3.1 가게별 정산 통계
**가게사장님이 기간별 통계를 확인할 때 사용**

```http
GET /api/daily-settlements/store/{storeId}/statistics?startDate={startDate}&endDate={endDate}
```

**Path Parameters:**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| storeId | String | ✅ | 가게 ID |

**Query Parameters:**
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| startDate | String (YYYY-MM-DD) | ❌ | 이번 달 1일 | 조회 시작일 |
| endDate | String (YYYY-MM-DD) | ❌ | 오늘 | 조회 종료일 |

**Response:**
```json
{
  "type": "STORE",
  "id": "store1",
  "totalOrderCount": 45,
  "totalSalesAmount": 6750000,
  "totalPaidAmount": 4500000,
  "totalOutstandingAmount": 2250000,
  "catalogOrderCount": 30,
  "catalogSalesAmount": 4500000,
  "ingredientOrderCount": 15,
  "ingredientSalesAmount": 2250000,
  "paymentRate": 66.67
}
```

---

#### 3.2 유통업자별 정산 통계
**유통업자가 기간별 통계를 확인할 때 사용**

```http
GET /api/daily-settlements/distributor/{distributorId}/statistics?startDate={startDate}&endDate={endDate}
```

**Parameters:** (3.1과 동일)
**Response:**
```json
{
  "type": "DISTRIBUTOR",
  "id": "dist1",
  "totalOrderCount": 45,
  "totalSalesAmount": 6750000,
  "totalPaidAmount": 4500000,
  "totalOutstandingAmount": 2250000,
  "catalogOrderCount": 30,
  "catalogSalesAmount": 4500000,
  "ingredientOrderCount": 15,
  "ingredientSalesAmount": 2250000,
  "paymentRate": 66.67
}
```

---

## 데이터 모델

### SettlementResponse (개별 정산)
```dart
class SettlementResponse {
  final String settlementId;
  final String storeId;
  final String distributorId;
  final String orderId;
  final int settlementAmount;
  final int outstandingAmount;
  final int paidAmount;
  final String status; // PENDING, PROCESSING, COMPLETED, FAILED
  final DateTime settlementDate;
  final DateTime? completedAt;

  SettlementResponse({
    required this.settlementId,
    required this.storeId,
    required this.distributorId,
    required this.orderId,
    required this.settlementAmount,
    required this.outstandingAmount,
    required this.paidAmount,
    required this.status,
    required this.settlementDate,
    this.completedAt,
  });

  factory SettlementResponse.fromJson(Map<String, dynamic> json) {
    return SettlementResponse(
      settlementId: json['settlementId'],
      storeId: json['storeId'],
      distributorId: json['distributorId'],
      orderId: json['orderId'],
      settlementAmount: json['settlementAmount'],
      outstandingAmount: json['outstandingAmount'],
      paidAmount: json['paidAmount'],
      status: json['status'],
      settlementDate: DateTime.parse(json['settlementDate']),
      completedAt: json['completedAt'] != null 
          ? DateTime.parse(json['completedAt']) 
          : null,
    );
  }
}
```

### DailySettlementResponse (일일 정산)
```dart
class DailySettlementResponse {
  final int id;
  final DateTime settlementDate;
  final String storeId;
  final String distributorId;
  final int orderCount;
  final int totalSalesAmount;
  final int totalSettlementAmount;
  final int totalPaidAmount;
  final int totalOutstandingAmount;
  final int catalogOrderCount;
  final int catalogSalesAmount;
  final int ingredientOrderCount;
  final int ingredientSalesAmount;
  final double paymentRate;
  final DateTime createdAt;
  final DateTime updatedAt;

  DailySettlementResponse({
    required this.id,
    required this.settlementDate,
    required this.storeId,
    required this.distributorId,
    required this.orderCount,
    required this.totalSalesAmount,
    required this.totalSettlementAmount,
    required this.totalPaidAmount,
    required this.totalOutstandingAmount,
    required this.catalogOrderCount,
    required this.catalogSalesAmount,
    required this.ingredientOrderCount,
    required this.ingredientSalesAmount,
    required this.paymentRate,
    required this.createdAt,
    required this.updatedAt,
  });

  factory DailySettlementResponse.fromJson(Map<String, dynamic> json) {
    return DailySettlementResponse(
      id: json['id'],
      settlementDate: DateTime.parse(json['settlementDate']),
      storeId: json['storeId'],
      distributorId: json['distributorId'],
      orderCount: json['orderCount'],
      totalSalesAmount: json['totalSalesAmount'],
      totalSettlementAmount: json['totalSettlementAmount'],
      totalPaidAmount: json['totalPaidAmount'],
      totalOutstandingAmount: json['totalOutstandingAmount'],
      catalogOrderCount: json['catalogOrderCount'],
      catalogSalesAmount: json['catalogSalesAmount'],
      ingredientOrderCount: json['ingredientOrderCount'],
      ingredientSalesAmount: json['ingredientSalesAmount'],
      paymentRate: json['paymentRate'].toDouble(),
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: DateTime.parse(json['updatedAt']),
    );
  }
}
```

### SettlementStatisticsResponse (정산 통계)
```dart
class SettlementStatisticsResponse {
  final String type; // STORE or DISTRIBUTOR
  final String id;
  final int totalOrderCount;
  final int totalSalesAmount;
  final int totalPaidAmount;
  final int totalOutstandingAmount;
  final int catalogOrderCount;
  final int catalogSalesAmount;
  final int ingredientOrderCount;
  final int ingredientSalesAmount;
  final double paymentRate;

  SettlementStatisticsResponse({
    required this.type,
    required this.id,
    required this.totalOrderCount,
    required this.totalSalesAmount,
    required this.totalPaidAmount,
    required this.totalOutstandingAmount,
    required this.catalogOrderCount,
    required this.catalogSalesAmount,
    required this.ingredientOrderCount,
    required this.ingredientSalesAmount,
    required this.paymentRate,
  });

  factory SettlementStatisticsResponse.fromJson(Map<String, dynamic> json) {
    return SettlementStatisticsResponse(
      type: json['type'],
      id: json['id'],
      totalOrderCount: json['totalOrderCount'],
      totalSalesAmount: json['totalSalesAmount'],
      totalPaidAmount: json['totalPaidAmount'],
      totalOutstandingAmount: json['totalOutstandingAmount'],
      catalogOrderCount: json['catalogOrderCount'],
      catalogSalesAmount: json['catalogSalesAmount'],
      ingredientOrderCount: json['ingredientOrderCount'],
      ingredientSalesAmount: json['ingredientSalesAmount'],
      paymentRate: json['paymentRate'].toDouble(),
    );
  }
}
```

---

## 화면 구성 제안

### 1. 가게사장님용 화면

#### 1.1 정산 대시보드 (메인)
**표시 정보:**
- 📊 이번 달 총 매출액
- 💰 지불 완료 금액
- ⚠️ 미수금 (강조 표시)
- 📈 결제율 (프로그레스 바)
- 📦 주문 건수 (카탈로그/식자재 분리)

**API 호출:**
```dart
GET /api/daily-settlements/store/{storeId}/statistics
  ?startDate=2024-11-01
  &endDate=2024-11-30
```

#### 1.2 일일 정산 내역
**표시 정보:**
- 📅 날짜별 정산 리스트
- 각 날짜별 매출액, 지불액, 미수금
- 차트 (선 그래프 또는 막대 그래프)

**API 호출:**
```dart
GET /api/daily-settlements/store/{storeId}
  ?startDate=2024-11-01
  &endDate=2024-11-30
```

#### 1.3 개별 정산 내역
**표시 정보:**
- 정산 ID
- 주문 번호 (클릭 시 주문 상세로 이동)
- 정산 금액
- 상태 (대기/완료)
- 정산 일시

**API 호출:**
```dart
GET /api/settlements/store/{storeId}
```

---

### 2. 유통업자용 화면

#### 2.1 정산 대시보드 (메인)
**표시 정보:**
- 📊 이번 달 총 매출액
- 💰 받을 금액 (미수금)
- ✅ 정산 완료 금액
- 📈 정산 완료율
- 📦 주문 건수

**API 호출:**
```dart
GET /api/daily-settlements/distributor/{distributorId}/statistics
  ?startDate=2024-11-01
  &endDate=2024-11-30
```

#### 2.2 정산 처리
**기능:**
- 미정산 내역 리스트
- 정산 완료 버튼
- 입금 금액 입력

**API 호출:**
```dart
// 목록 조회
GET /api/settlements/distributor/{distributorId}

// 정산 완료 처리
POST /api/settlements/{settlementId}/complete
Body: {"paidAmount": 150000}
```

---

## 사용 예시

### 예시 1: 가게사장님 대시보드 로딩

```dart
class SettlementService {
  final String baseUrl = 'http://localhost:8080';
  
  Future<SettlementStatisticsResponse> getStoreStatistics(
    String storeId,
    DateTime startDate,
    DateTime endDate,
  ) async {
    final response = await http.get(
      Uri.parse(
        '$baseUrl/api/daily-settlements/store/$storeId/statistics'
        '?startDate=${startDate.toIso8601String().split('T')[0]}'
        '&endDate=${endDate.toIso8601String().split('T')[0]}'
      ),
    );
    
    if (response.statusCode == 200) {
      return SettlementStatisticsResponse.fromJson(
        json.decode(response.body)
      );
    } else {
      throw Exception('정산 통계 조회 실패');
    }
  }
}

// 사용
final service = SettlementService();
final now = DateTime.now();
final firstDayOfMonth = DateTime(now.year, now.month, 1);

final statistics = await service.getStoreStatistics(
  'store1',
  firstDayOfMonth,
  now,
);

print('총 매출: ${statistics.totalSalesAmount}원');
print('미수금: ${statistics.totalOutstandingAmount}원');
print('결제율: ${statistics.paymentRate}%');
```

---

### 예시 2: 일일 정산 차트 데이터

```dart
Future<List<DailySettlementResponse>> getDailySettlements(
  String storeId,
  DateTime startDate,
  DateTime endDate,
) async {
  final response = await http.get(
    Uri.parse(
      '$baseUrl/api/daily-settlements/store/$storeId'
      '?startDate=${startDate.toIso8601String().split('T')[0]}'
      '&endDate=${endDate.toIso8601String().split('T')[0]}'
    ),
  );
  
  if (response.statusCode == 200) {
    final List<dynamic> jsonList = json.decode(response.body);
    return jsonList
        .map((json) => DailySettlementResponse.fromJson(json))
        .toList();
  } else {
    throw Exception('일일 정산 조회 실패');
  }
}

// 차트 데이터 변환
final settlements = await getDailySettlements(
  'store1',
  DateTime.now().subtract(Duration(days: 30)),
  DateTime.now(),
);

final chartData = settlements.map((s) => {
  'date': s.settlementDate,
  'sales': s.totalSalesAmount,
  'paid': s.totalPaidAmount,
  'outstanding': s.totalOutstandingAmount,
}).toList();
```

---

### 예시 3: 정산 완료 처리

```dart
Future<void> completeSettlement(
  String settlementId,
  int paidAmount,
) async {
  final response = await http.post(
    Uri.parse('$baseUrl/api/settlements/$settlementId/complete'),
    headers: {'Content-Type': 'application/json'},
    body: json.encode({'paidAmount': paidAmount}),
  );
  
  if (response.statusCode == 200) {
    print('정산 완료 처리 성공');
  } else {
    throw Exception('정산 완료 처리 실패');
  }
}

// 사용
await completeSettlement('SETTLE_abc123', 150000);
```

---

## UI/UX 권장사항

### 색상 가이드
- 💚 **지불 완료**: Green (#4CAF50)
- 🟡 **정산 대기**: Yellow/Orange (#FF9800)
- 🔴 **미수금**: Red (#F44336)
- 🔵 **총 매출**: Blue (#2196F3)

### 아이콘 추천
- 💰 매출액: `Icons.attach_money`
- 📊 통계: `Icons.bar_chart`
- ✅ 완료: `Icons.check_circle`
- ⏳ 대기: `Icons.pending`
- 📅 날짜: `Icons.calendar_today`
- 📦 주문: `Icons.shopping_cart`

### 차트 라이브러리 추천
- `fl_chart`: 선 그래프, 막대 그래프
- `syncfusion_flutter_charts`: 고급 차트
- `charts_flutter`: Google Charts

---

## 에러 처리

### HTTP 상태 코드
- `200`: 성공
- `400`: 잘못된 요청 (파라미터 오류)
- `404`: 리소스를 찾을 수 없음
- `500`: 서버 오류

### 에러 응답 예시
```json
{
  "timestamp": "2024-11-29T14:30:22",
  "status": 404,
  "error": "Not Found",
  "message": "정산을 찾을 수 없습니다: SETTLE_abc123",
  "path": "/api/settlements/SETTLE_abc123"
}
```

---

## 테스트 데이터

### 테스트용 ID
- 가게 ID: `store1`, `store2`
- 유통업자 ID: `dist1`, `dist2`

### 테스트 시나리오
1. 주문 생성 → 결제 완료 → 정산 자동 생성 확인
2. 일일 정산 조회 → 차트 표시
3. 정산 완료 처리 → 미수금 감소 확인

---

## 문의사항

API 관련 문의사항이 있으면 백엔드 개발팀에 연락주세요.

**참고 문서:**
- `SETTLEMENT_GUIDE.md`: 전체 시스템 가이드
- `SETTLEMENT_SUMMARY.md`: 구현 요약
- `test-settlement-flow.sh`: API 테스트 스크립트
