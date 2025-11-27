# 결제 흐름 문제 해결 가이드

## 🔍 문제 분석

### 발생한 에러
```
java.lang.IllegalArgumentException: 결제 정보를 찾을 수 없습니다
```

### 원인
1. 주문 생성 시 결제 정보(`payments` 테이블)가 자동으로 생성되지 않음
2. 토스페이먼츠에 전달한 `orderId`와 결제 승인 시 사용한 `orderId`가 다름

---

## ✅ 해결 방법 (백엔드 수정 완료)

### 1. 주문 생성 시 결제 정보 자동 생성

**수정 내용:**
- `CatalogOrderService.createOrderFromCart()` 메서드에서 주문 생성 후 결제 정보도 함께 생성
- `orderId`로 주문번호(`ORD-20251127-035844-153`)를 사용

**코드:**
```java
// 주문 생성 후
DistributorOrder savedOrder = orderRepository.save(order);

// 결제 정보 생성
String orderName = generateOrderName(savedOrder);  // "경기미 20kg 외 2건"
paymentService.createPayment(
    savedOrder.getOrderNumber(),  // "ORD-20251127-035844-153"
    orderName,
    savedOrder.getTotalAmount(),
    storeId + "@store.com"
);
```

### 2. 결제 승인 시 결제 정보 자동 생성 (Fallback)

**수정 내용:**
- `PaymentService.confirmPayment()` 메서드에서 결제 정보가 없으면 자동 생성
- 이전 주문에 대한 결제도 처리 가능

**코드:**
```java
Payment payment = paymentRepository.findByOrderId(request.getOrderId())
    .orElseGet(() -> {
        // 결제 정보가 없으면 자동 생성
        Payment newPayment = new Payment(
            request.getOrderId(),
            "주문 " + request.getOrderId(),
            request.getAmount(),
            "customer@example.com"
        );
        return paymentRepository.save(newPayment);
    });
```

---

## 📋 올바른 결제 흐름

### ✅ 정답: 방법 B (자동 생성)

```
1. 주문 생성
   POST /api/catalog-orders/create
   → orderId: "ORD-20251127-035844-153"
   → 결제 정보 자동 생성 (payments 테이블)

2. 토스페이먼츠 결제
   → orderId: "ORD-20251127-035844-153" (주문번호 그대로 사용)
   → paymentKey: "tviva20251127124326xgKf1"

3. 결제 승인
   POST /api/payments/confirm
   Body: {
     "paymentKey": "tviva20251127124326xgKf1",
     "orderId": "ORD-20251127-035844-153",  // 주문번호 사용
     "amount": 1000
   }
```

---

## 🎯 Flutter 클라이언트 수정 사항

### 문제가 있던 코드

```dart
// ❌ 잘못된 방법: 다른 orderId 사용
final paymentInfo = PaymentInfo(
  orderId: "ORDER-${DateTime.now().millisecondsSinceEpoch}",  // 새로 생성
  orderName: orderName,
  amount: order.totalAmount,
  customerEmail: email,
  customerName: name,
);

// 토스페이먼츠 결제
await tossPayments.requestPayment('카드', {
  orderId: paymentInfo.orderId,  // "ORDER-1732689537123"
  // ...
});

// 결제 승인
await confirmPayment(
  paymentKey: result['paymentKey'],
  orderId: order.orderNumber,  // "ORD-20251127-035844-153" (다른 값!)
  amount: order.totalAmount,
);
```

### ✅ 올바른 코드

```dart
// 1. 주문 생성
final order = await orderService.createOrder(
  distributorId: 'distributor1',
  deliveryAddress: '서울시 강남구 테헤란로 123',
  deliveryPhone: '010-1234-5678',
  deliveryRequest: '문 앞에 놓아주세요',
);

// order.orderNumber = "ORD-20251127-035844-153"
// order.totalAmount = 1000

// 2. 결제 정보 생성 (주문번호를 orderId로 사용)
final paymentInfo = PaymentInfo(
  orderId: order.orderNumber,  // ✅ 주문번호 그대로 사용
  orderName: _generateOrderName(order.items),
  amount: order.totalAmount,
  customerEmail: 'customer@example.com',
  customerName: '홍길동',
);

// 3. 토스페이먼츠 결제
await tossPayments.requestPayment('카드', {
  orderId: paymentInfo.orderId,  // "ORD-20251127-035844-153"
  orderName: paymentInfo.orderName,
  amount: paymentInfo.amount,
  customerName: paymentInfo.customerName,
  customerEmail: paymentInfo.customerEmail,
  successUrl: window.location.origin + '/payment/success',
  failUrl: window.location.origin + '/payment/fail',
});

// 4. 결제 승인 (동일한 orderId 사용)
final paymentResponse = await confirmPayment(
  paymentKey: result['paymentKey'],
  orderId: order.orderNumber,  // ✅ 동일한 주문번호 사용
  amount: order.totalAmount,
);
```

---

## 💾 수정된 Dart 코드

### PaymentInfo 생성

```dart
class PaymentInfo {
  final String orderId;
  final String orderName;
  final int amount;
  final String customerEmail;
  final String customerName;

  PaymentInfo({
    required this.orderId,
    required this.orderName,
    required this.amount,
    required this.customerEmail,
    required this.customerName,
  });
  
  // ✅ 주문 정보에서 결제 정보 생성
  static PaymentInfo fromOrder(Order order, String email, String name) {
    return PaymentInfo(
      orderId: order.orderNumber,  // ✅ 주문번호를 orderId로 사용
      orderName: _generateOrderName(order.items),
      amount: order.totalAmount,
      customerEmail: email,
      customerName: name,
    );
  }
  
  static String _generateOrderName(List<OrderItem> items) {
    if (items.isEmpty) return "주문";
    if (items.length == 1) return items[0].productName;
    return "${items[0].productName} 외 ${items.length - 1}건";
  }
}
```

### 전체 결제 플로우

```dart
Future<void> processPayment(Order order) async {
  try {
    // 1. 결제 정보 생성 (주문번호 사용)
    final paymentInfo = PaymentInfo.fromOrder(
      order,
      'customer@example.com',
      '홍길동',
    );
    
    print('결제 정보: orderId=${paymentInfo.orderId}, amount=${paymentInfo.amount}');
    
    // 2. 토스페이먼츠 결제 호출
    final result = await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => PaymentWebView(paymentInfo: paymentInfo),
      ),
    );
    
    if (result == null) {
      _showMessage('결제가 취소되었습니다');
      return;
    }
    
    if (result['success'] == true) {
      print('토스 결제 성공: paymentKey=${result['paymentKey']}, orderId=${result['orderId']}');
      
      // 3. 결제 승인 API 호출
      final paymentResponse = await confirmPayment(
        paymentKey: result['paymentKey'],
        orderId: result['orderId'],  // 토스에서 받은 orderId (주문번호와 동일)
        amount: int.parse(result['amount']),
      );
      
      print('결제 승인 성공: status=${paymentResponse.status}');
      
      // 4. 결제 완료 확인
      if (paymentResponse.status == 'DONE') {
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
    } else {
      _showMessage('결제 실패: ${result['message']}');
    }
  } catch (e) {
    print('결제 처리 오류: $e');
    _showMessage('결제 처리 중 오류: $e');
  }
}

Future<PaymentResponse> confirmPayment({
  required String paymentKey,
  required String orderId,
  required int amount,
}) async {
  final url = Uri.parse('http://10.0.2.2:8080/api/payments/confirm');
  
  print('결제 승인 요청: paymentKey=$paymentKey, orderId=$orderId, amount=$amount');
  
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
    print('결제 승인 실패: ${response.body}');
    throw Exception('결제 승인 실패: ${response.body}');
  }
}
```

---

## 🔍 디버깅 팁

### 1. 로그 확인

```dart
// 주문 생성 후
print('주문 생성: orderNumber=${order.orderNumber}, amount=${order.totalAmount}');

// 결제 정보 생성 후
print('결제 정보: orderId=${paymentInfo.orderId}');

// 토스 결제 성공 후
print('토스 결제: paymentKey=${result['paymentKey']}, orderId=${result['orderId']}');

// 결제 승인 요청 전
print('결제 승인 요청: orderId=$orderId, amount=$amount');
```

### 2. orderId 일치 확인

```dart
// 모든 단계에서 동일한 orderId 사용 확인
assert(paymentInfo.orderId == order.orderNumber);
assert(result['orderId'] == order.orderNumber);
```

### 3. 백엔드 로그 확인

```bash
# 주문 생성 로그
주문 생성 완료: ORD-20251127-035844-153 (매장: 김가게, 유통업체: 김유통)
결제 생성: orderId=ORD-20251127-035844-153, processingTime=5ms

# 결제 승인 로그
에스크로 결제 승인 요청: transactionId=ORD-20251127-035844-153, paymentKey=tviva20251127124326xgKf1
결제 승인 완료: orderId=ORD-20251127-035844-153, paymentKey=tviva20251127124326xgKf1
```

---

## 📊 데이터베이스 확인

### payments 테이블 확인

```sql
-- 결제 정보가 생성되었는지 확인
SELECT * FROM payments WHERE order_id = 'ORD-20251127-035844-153';

-- 결과 예시:
-- id | payment_key | order_id | order_name | amount | status | method | approved_at
-- 1  | NULL        | ORD-...  | 경기미...  | 1000   | READY  | NULL   | NULL
```

### distributor_orders 테이블 확인

```sql
-- 주문 정보 확인
SELECT * FROM distributor_orders WHERE order_number = 'ORD-20251127-035844-153';

-- 결과 예시:
-- id | store_id | distributor_id | order_number | total_amount | status
-- 1  | 김가게   | 김유통         | ORD-...      | 1000         | PENDING
```

---

## ⚠️ 주의사항

### 1. orderId는 주문번호를 사용

```dart
// ✅ 올바른 방법
orderId: order.orderNumber  // "ORD-20251127-035844-153"

// ❌ 잘못된 방법
orderId: "ORDER-${order.id}"  // "ORDER-1"
orderId: "ORDER-${DateTime.now().millisecondsSinceEpoch}"  // "ORDER-1732689537123"
```

### 2. 모든 단계에서 동일한 orderId 사용

```
주문 생성 → orderId: "ORD-20251127-035844-153"
토스 결제 → orderId: "ORD-20251127-035844-153" (동일)
결제 승인 → orderId: "ORD-20251127-035844-153" (동일)
```

### 3. 금액 일치 확인

```dart
// 주문 금액과 결제 금액이 일치해야 함
assert(paymentInfo.amount == order.totalAmount);
assert(int.parse(result['amount']) == order.totalAmount);
```

---

## 🎯 질문에 대한 답변

### Q1. 결제 정보는 언제 저장되나요?

**답변:** 주문 생성 시 자동으로 결제 정보가 생성됩니다. (백엔드 수정 완료)

```java
// CatalogOrderService.createOrderFromCart()
DistributorOrder savedOrder = orderRepository.save(order);

// 결제 정보 자동 생성
paymentService.createPayment(
    savedOrder.getOrderNumber(),  // orderId
    orderName,
    savedOrder.getTotalAmount(),
    customerEmail
);
```

### Q2. orderId 매핑 문제인가요?

**답변:** 네, 맞습니다. 토스페이먼츠와 결제 승인 API에 **동일한 orderId**를 사용해야 합니다.

- ✅ 올바른 방법: 주문번호(`ORD-20251127-035844-153`)를 모든 단계에서 사용
- ❌ 잘못된 방법: 토스에는 `ORDER-1732689537123`, 승인에는 `ORD-20251127-035844-153` 사용

### Q3. 올바른 결제 흐름은 무엇인가요?

**답변:** **방법 B** - 주문 생성 → 토스 결제 → 결제 승인 (결제 정보 자동 생성)

```
1. 주문 생성
   POST /api/catalog-orders/create
   → orderId: "ORD-20251127-035844-153"
   → 결제 정보 자동 생성 ✅

2. 토스페이먼츠 결제
   → orderId: "ORD-20251127-035844-153" (동일한 값 사용)
   → paymentKey: "tviva20251127124326xgKf1"

3. 결제 승인
   POST /api/payments/confirm
   → orderId: "ORD-20251127-035844-153" (동일한 값 사용)
   → status: "DONE"
```

---

## ✅ 체크리스트

Flutter 클라이언트에서 확인할 사항:

- [ ] 주문 생성 후 `order.orderNumber` 값 확인
- [ ] `PaymentInfo.orderId`에 `order.orderNumber` 사용
- [ ] 토스페이먼츠 호출 시 동일한 `orderId` 사용
- [ ] 결제 승인 API 호출 시 동일한 `orderId` 사용
- [ ] 모든 단계에서 금액 일치 확인
- [ ] 로그로 각 단계의 `orderId` 값 확인

---

## 📞 추가 지원

문제가 계속되면 다음 정보를 제공해주세요:

1. 주문 생성 응답 전체 JSON
2. 토스페이먼츠 성공 콜백 파라미터
3. 결제 승인 요청 Body
4. 백엔드 에러 로그

이 정보로 정확한 원인을 파악할 수 있습니다.
