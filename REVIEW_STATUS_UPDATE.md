# 주문 조회 API 업데이트 - 리뷰 작성 여부 필드 추가

## 📋 변경 사항 요약

주문 조회 API 응답에 **리뷰 작성 여부 필드**를 추가했습니다.

---

## 🎯 대상 API

### 1. 가게사장님 주문 목록
```
GET /api/catalog-orders/my
```

### 2. 유통업자 주문 목록
```
GET /api/catalog-orders/distributor
```

---

## ✨ 추가된 필드

### hasStoreReview (boolean)
- **설명**: 가게사장님이 유통업자를 평가하는 리뷰를 작성했는지 여부
- **타입**: `boolean` (null 아님)
- **기본값**: `false`
- **값**:
  - `true`: 리뷰 작성 완료
  - `false`: 리뷰 미작성 (작성 가능)

### hasDistributorReview (boolean)
- **설명**: 유통업자가 가게사장님을 평가하는 리뷰를 작성했는지 여부
- **타입**: `boolean` (null 아님)
- **기본값**: `false`
- **값**:
  - `true`: 리뷰 작성 완료
  - `false`: 리뷰 미작성 (작성 가능)

---

## 📝 응답 예시

### Before (이전)
```json
{
  "id": 123,
  "orderNumber": "ORD-20251127-155507-742",
  "status": "DELIVERED",
  "statusDescription": "배송완료",
  "totalAmount": 50000,
  "orderedAt": "2025-11-27T15:55:07",
  "deliveredAt": "2025-11-28T10:30:00"
}
```

### After (변경 후)
```json
{
  "id": 123,
  "orderNumber": "ORD-20251127-155507-742",
  "status": "DELIVERED",
  "statusDescription": "배송완료",
  "totalAmount": 50000,
  "orderedAt": "2025-11-27T15:55:07",
  "deliveredAt": "2025-11-28T10:30:00",
  
  // ✨ 추가된 필드
  "hasStoreReview": true,
  "hasDistributorReview": false
}
```

---

## 💡 사용 방법

### 가게사장님 앱 (Flutter)

```dart
// 주문 목록에서 리뷰 버튼 표시
Widget buildReviewButton(Order order) {
  // 배송 완료 전에는 버튼 숨김
  if (order.status != OrderStatus.DELIVERED) {
    return SizedBox.shrink();
  }
  
  // 이미 리뷰 작성한 경우
  if (order.hasStoreReview) {
    return Chip(
      label: Text('리뷰 등록 완료'),
      backgroundColor: Colors.green[100],
      avatar: Icon(Icons.check_circle, color: Colors.green),
    );
  }
  
  // 리뷰 작성 가능한 경우
  return ElevatedButton(
    onPressed: () => navigateToReviewPage(order),
    child: Text('리뷰 작성'),
  );
}
```

### 유통업자 앱 (Flutter)

```dart
// 주문 목록에서 리뷰 버튼 표시
Widget buildReviewButton(Order order) {
  // 배송 완료 전에는 버튼 숨김
  if (order.status != OrderStatus.DELIVERED) {
    return SizedBox.shrink();
  }
  
  // 이미 리뷰 작성한 경우
  if (order.hasDistributorReview) {
    return Chip(
      label: Text('리뷰 등록 완료'),
      backgroundColor: Colors.green[100],
      avatar: Icon(Icons.check_circle, color: Colors.green),
    );
  }
  
  // 리뷰 작성 가능한 경우
  return ElevatedButton(
    onPressed: () => navigateToReviewPage(order),
    child: Text('리뷰 작성'),
  );
}
```

---

## 🎨 UI 구현 가이드

### 주문 목록 화면

**배송 완료 전**:
```
┌─────────────────────────────┐
│ 주문번호: ORD-xxx           │
│ 상태: 배송중                │
│ 금액: 50,000원              │
│                             │
│ (리뷰 버튼 없음)            │
└─────────────────────────────┘
```

**배송 완료 + 리뷰 미작성**:
```
┌─────────────────────────────┐
│ 주문번호: ORD-xxx           │
│ 상태: 배송완료              │
│ 금액: 50,000원              │
│                             │
│ [  리뷰 작성  ]  ← 버튼     │
└─────────────────────────────┘
```

**배송 완료 + 리뷰 작성 완료**:
```
┌─────────────────────────────┐
│ 주문번호: ORD-xxx           │
│ 상태: 배송완료              │
│ 금액: 50,000원              │
│                             │
│ ✓ 리뷰 등록 완료  ← 배지    │
└─────────────────────────────┘
```

---

## ✅ 장점

### 1. 불필요한 API 호출 방지
- 리뷰 작성 여부를 미리 알 수 있어 중복 작성 시도 방지
- 서버 부하 감소

### 2. 사용자 경험 개선
- "리뷰 작성" vs "리뷰 등록 완료" 상태를 명확히 표시
- 사용자가 리뷰 작성 여부를 한눈에 파악 가능

### 3. 에러 감소
- 서버에서 "이미 리뷰를 작성한 주문입니다" 에러 발생 빈도 감소
- 클라이언트에서 사전에 검증 가능

### 4. UI 일관성
- 모든 주문 목록에서 일관된 리뷰 상태 표시
- 직관적인 사용자 인터페이스

---

## ⚠️ 주의사항

1. **필드 타입**: `boolean`이며 `null`이 아닙니다
2. **기본값**: `false` (리뷰가 없으면 false)
3. **표시 조건**: 배송 완료(`DELIVERED`) 상태에서만 리뷰 버튼 표시
4. **새로고침**: 리뷰 작성 후 주문 목록을 새로고침하여 최신 상태 반영

---

## 🔄 데이터 모델 업데이트

### Dart (Flutter)

```dart
class Order {
  final int id;
  final String orderNumber;
  final OrderStatus status;
  final String statusDescription;
  final int totalAmount;
  final DateTime orderedAt;
  final DateTime? deliveredAt;
  
  // ✨ 추가된 필드
  final bool hasStoreReview;        // 가게사장님이 리뷰 작성했는지
  final bool hasDistributorReview;  // 유통업자가 리뷰 작성했는지
  
  Order({
    required this.id,
    required this.orderNumber,
    required this.status,
    required this.statusDescription,
    required this.totalAmount,
    required this.orderedAt,
    this.deliveredAt,
    required this.hasStoreReview,
    required this.hasDistributorReview,
  });
  
  factory Order.fromJson(Map<String, dynamic> json) {
    return Order(
      id: json['id'],
      orderNumber: json['orderNumber'],
      status: OrderStatus.values.byName(json['status']),
      statusDescription: json['statusDescription'],
      totalAmount: json['totalAmount'],
      orderedAt: DateTime.parse(json['orderedAt']),
      deliveredAt: json['deliveredAt'] != null 
          ? DateTime.parse(json['deliveredAt']) 
          : null,
      hasStoreReview: json['hasStoreReview'] ?? false,
      hasDistributorReview: json['hasDistributorReview'] ?? false,
    );
  }
}
```

---

## 📚 관련 문서

- **상세 가이드**: `FLUTTER_DELIVERY_API_GUIDE.md` 섹션 17 참고
- **빠른 시작**: `FLUTTER_QUICK_START.md` 리뷰 섹션 참고
- **리뷰 API**: `FLUTTER_DELIVERY_API_GUIDE.md` 섹션 11 참고

---

## 🚀 배포 상태

- ✅ 백엔드 구현 완료
- ✅ API 문서 업데이트 완료
- ⏳ 프론트엔드 구현 대기 중

---

## 💬 문의사항

API 관련 문의사항이나 추가 요청사항이 있으면 백엔드 팀에 연락 주세요.
