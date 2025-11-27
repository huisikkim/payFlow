# 카탈로그 주문 결제 API 가이드

## ✅ 결제 승인 API가 이미 구현되어 있습니다!

백엔드에 **범용 결제 API**가 이미 구현되어 있으며, 카탈로그 주문에도 사용할 수 있습니다.

---

## 📋 결제 승인 API

### 1. 결제 승인 (Payment Confirm)

**엔드포인트:**
```
POST /api/payments/confirm
```

**헤더:**
```
Content-Type: application/json
```

**요청 Body:**
```json
{
  "paymentKey": "tgen_20231126143025_abc123",
  "orderId": "ORDER-1",
  "amount": 480000
}
```

**Dart 예시:**
```dart
Future<PaymentResponse> confirmPayment({
  required String paymentKey,
  required String orderId,
  required int amount,
}) async {
  final url = Uri.parse('http://10.0.2.2:8080/api/payments/confirm');
  
  final response = await http.post(
    url,
    headers: {
      'Content-Type': 'application/json',
    },
    body: jsonEncode({
      'paymentKey': paymentKey,
      'orderId': orderId,
      'amount': amount,
    }),
  );
  
  if (response.statusCode == 200) {
    return PaymentResponse.fromJson(
      jsonDecode(utf8.decode(response.bodyBytes))
    );
  } else {
    throw Exception('결제 승인 실패: ${response.body}');
  }
}
```

**응답:**
```json
{
  "paymentKey": "tgen_20231126143025_abc123",
  "orderId": "ORDER-1",
  "orderName": "경기미 20kg 외 2건",
  "amount": 480000,
  "status": "DONE",
  "method": "카드",
  "approvedAt": "2023-11-26T14:35:00"
}
```

---

### 2. 결제 정보 조회

**엔드포인트:**
```
GET /api/payments/{orderId}
```

**헤더:**
```
Content-Type: application/json
```

**Dart 예시:**
```dart
Future<PaymentResponse> getPayment(String orderId) async {
  final url = Uri.parse('http://10.0.2.2:8080/api/payments/$orderId');
  
  final response = await http.get(
    url,
    headers: {
      'Content-Type': 'application/json',
    },
  );
  
  if (response.statusCode == 200) {
    return PaymentResponse.fromJson(
      jsonDecode(utf8.decode(response.bodyBytes))
    );
  } else {
    throw Exception('결제 정보 조회 실패');
  }
}
```

**응답:** 결제 승인 API와 동일

---

## 💾 Dart 모델

### PaymentConfirmRequest (결제 승인 요청)
```dart
class PaymentConfirmRequest {
  final String paymentKey;
  final String orderId;
  final int amount;

  PaymentConfirmRequest({
    required this.paymentKey,
    required this.orderId,
    required this.amount,
  });

  Map<String, dynamic> toJson() {
    return {
      'paymentKey': paymentKey,
      'orderId': orderId,
      'amount': amount,
    };
  }
}
```

### PaymentResponse (결제 응답)
```dart
class PaymentResponse {
  final String paymentKey;
  final String orderId;
  final String orderName;
  final int amount;
  final String status;  // READY, DONE, FAILED, CANCELED
  final String? method;
  final DateTime? approvedAt;

  PaymentResponse({
    required this.paymentKey,
    required this.orderId,
    required this.orderName,
    required this.amount,
    required this.status,
    this.method,
    this.approvedAt,
  });

  factory PaymentResponse.fromJson(Map<String, dynamic> json) {
    return PaymentResponse(
      paymentKey: json['paymentKey'] ?? '',
      orderId: json['orderId'],
      orderName: json['orderName'],
      amount: json['amount'],
      status: json['status'],
      method: json['method'],
      approvedAt: json['approvedAt'] != null 
          ? DateTime.parse(json['approvedAt']) 
          : null,
    );
  }
}
```

---

## 🔄 전체 결제 플로우

### 1. 주문 생성
```dart
// 장바구니에서 주문 생성
final order = await orderService.createOrder(
  distributorId: 'distributor1',
  deliveryAddress: '서울시 강남구 테헤란로 123',
  deliveryPhone: '010-1234-5678',
  deliveryRequest: '문 앞에 놓아주세요',
);

// order.id = 1
// order.orderNumber = "ORD-20231126-143025-456"
// order.totalAmount = 480000
```

### 2. 결제 정보 생성
```dart
// 주문 정보를 기반으로 결제 정보 생성
final paymentInfo = PaymentInfo(
  orderId: "ORDER-${order.id}",  // "ORDER-1"
  orderName: _generateOrderName(order.items),
  amount: order.totalAmount,
  customerEmail: 'customer@example.com',
  customerName: '홍길동',
);
```

### 3. 토스페이먼츠 결제 호출
```dart
// WebView에서 토스페이먼츠 위젯 호출
final result = await Navigator.push(
  context,
  MaterialPageRoute(
    builder: (context) => PaymentWebView(paymentInfo: paymentInfo),
  ),
);

// 결제 성공 시 result:
// {
//   'success': true,
//   'paymentKey': 'tgen_20231126143025_abc123',
//   'orderId': 'ORDER-1',
//   'amount': '480000'
// }
```

### 4. 결제 승인 API 호출
```dart
if (result['success'] == true) {
  // 백엔드에 결제 승인 요청
  final paymentResponse = await confirmPayment(
    paymentKey: result['paymentKey'],
    orderId: result['orderId'],
    amount: int.parse(result['amount']),
  );
  
  // paymentResponse.status == "DONE" 이면 결제 완료
  if (paymentResponse.status == 'DONE') {
    // 결제 완료 화면으로 이동
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(
        builder: (context) => PaymentSuccessPage(
          order: order,
          payment: paymentResponse,
        ),
      ),
    );
  }
}
```

---

## 🎯 백엔드 처리 과정

결제 승인 API가 호출되면 백엔드에서 다음 작업을 수행합니다:

1. **토스페이먼츠 API 호출**
   - 클라이언트에서 받은 `paymentKey`로 토스페이먼츠에 승인 요청
   - 금액 검증 (주문 금액과 결제 금액 일치 확인)

2. **결제 정보 저장**
   - `payments` 테이블에 결제 정보 저장
   - 상태: `READY` → `DONE`

3. **주문 상태 업데이트**
   - 주문 상태를 `PENDING` → `CONFIRMED`로 변경
   - `confirmedAt` 타임스탬프 기록

4. **이벤트 로깅**
   - 결제 승인 이벤트 저장
   - 이벤트 소싱을 통한 상태 변경 추적

---

## 📊 결제 상태 (PaymentStatus)

| 상태 | 설명 |
|------|------|
| `READY` | 결제 준비 (생성됨) |
| `DONE` | 결제 완료 |
| `FAILED` | 결제 실패 |
| `CANCELED` | 결제 취소 |

---

## ⚠️ 주의사항

### 1. orderId 형식
```dart
// 주문 ID를 "ORDER-{숫자}" 형식으로 변환
final orderId = "ORDER-${order.id}";  // "ORDER-1"
```

### 2. 금액 검증
- 클라이언트에서 보낸 금액과 서버의 주문 금액이 일치해야 함
- 서버에서 최종 검증 수행

### 3. 결제 승인 필수
- 토스페이먼츠 결제 성공 후 **반드시 서버에서 승인** 필요
- 승인하지 않으면 자동 취소됨 (약 10분 후)

### 4. 에러 처리
```dart
try {
  final payment = await confirmPayment(...);
} catch (e) {
  if (e.toString().contains('결제 정보를 찾을 수 없습니다')) {
    // 결제 정보 없음
  } else if (e.toString().contains('결제 승인 실패')) {
    // 토스페이먼츠 승인 실패
  } else {
    // 기타 오류
  }
}
```

### 5. 인증 불필요
- 결제 승인 API는 **인증 토큰이 필요 없습니다**
- `paymentKey`가 인증 역할을 함

---

## 🧪 테스트 시나리오

### 1. 정상 결제 플로우
```bash
# 1. 주문 생성
POST /api/catalog-orders/create
→ orderId: 1

# 2. 토스페이먼츠 결제
→ paymentKey: tgen_xxx

# 3. 결제 승인
POST /api/payments/confirm
Body: {
  "paymentKey": "tgen_xxx",
  "orderId": "ORDER-1",
  "amount": 480000
}
→ status: "DONE"

# 4. 결제 정보 조회
GET /api/payments/ORDER-1
→ 결제 완료 정보 반환
```

### 2. 결제 실패 처리
```bash
# 잘못된 paymentKey로 승인 시도
POST /api/payments/confirm
Body: {
  "paymentKey": "invalid_key",
  "orderId": "ORDER-1",
  "amount": 480000
}
→ 400 Bad Request
→ 주문 상태: FAILED
```

---

## 💡 구현 팁

### 1. 결제 정보 미리 생성 (선택사항)
주문 생성 시 결제 정보를 미리 생성할 수도 있습니다:

```dart
// 주문 생성 후 즉시 결제 정보 생성
await paymentService.createPayment(
  orderId: "ORDER-${order.id}",
  orderName: order.orderName,
  amount: order.totalAmount,
  customerEmail: 'customer@example.com',
);
```

하지만 현재는 **결제 승인 시 자동으로 생성**되므로 불필요합니다.

### 2. 결제 상태 폴링
결제 완료 후 주문 상태를 확인하려면:

```dart
// 주문 상세 조회로 확인
final order = await orderService.getOrderDetail(orderId);
if (order.status == 'CONFIRMED') {
  // 결제 완료 및 주문 확정됨
}
```

### 3. 결제 내역 저장
로컬에 결제 내역을 저장하여 오프라인에서도 확인 가능:

```dart
// SharedPreferences 또는 SQLite에 저장
await savePaymentHistory(paymentResponse);
```

---

## 📞 요약

### 질문에 대한 답변

**Q1. 결제 승인을 위한 API 엔드포인트가 어떻게 되나요?**
```
POST /api/payments/confirm
```

**Q2. 다음 중 어떤 방식으로 구현되어 있나요?**
- ✅ **결제 승인 전용 API가 있습니다**
- 엔드포인트: `POST /api/payments/confirm`
- 요청 Body: `{ "paymentKey": "xxx", "orderId": "xxx", "amount": 123 }`

**Q3. 필요한 정보**

| 항목 | 값 |
|------|-----|
| **API 엔드포인트 URL** | `POST /api/payments/confirm` |
| **HTTP 메서드** | `POST` |
| **요청 Body 형식** | `{ "paymentKey": "string", "orderId": "string", "amount": number }` |
| **응답 형식** | `{ "paymentKey": "string", "orderId": "string", "orderName": "string", "amount": number, "status": "string", "method": "string", "approvedAt": "datetime" }` |
| **인증** | 불필요 (paymentKey가 인증 역할) |

---

## 📚 관련 문서

- **토스페이먼츠 연동 가이드**: `FLUTTER_PAYMENT_API_GUIDE.md`
- **토스페이먼츠 설정**: `TOSS_PAYMENTS_CONFIG.md`
- **카탈로그 주문 API**: `FLUTTER_CATALOG_ORDER_API.md`
- **백엔드 API 문서**: `BACKEND_API_DOCUMENTATION.md`
