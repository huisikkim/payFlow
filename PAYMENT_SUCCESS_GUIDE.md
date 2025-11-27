# 결제 성공 후 처리 가이드

## ✅ 문제 해결 완료!

토스페이먼츠 결제 승인이 성공했습니다! 🎉

### 로그 분석
```
✅ 주문 생성: ORD-20251127-152346-940
✅ 결제 정보 생성: orderId=ORD-20251127-152346-940
✅ 토스페이먼츠 결제 승인 성공: paymentKey=tviva20251127152352DHaq8
✅ 결제 승인 완료
```

---

## 🔧 백엔드 수정 사항

### 1. PaymentService 수정
주문 확정 로직을 제거하여 카탈로그 주문과 충돌하지 않도록 수정했습니다.

**변경 전:**
```java
// 주문 확정 처리
orderService.confirmOrder(payment.getOrderId());  // ❌ 잘못된 테이블 참조
```

**변경 후:**
```java
// 주문 확정 처리는 별도로 수행 (카탈로그 주문은 CatalogOrderService에서 처리)
// orderService.confirmOrder(payment.getOrderId());  // ✅ 주석 처리
```

### 2. 주문 확정 API 추가
결제 완료 후 주문 상태를 업데이트하는 API를 추가했습니다.

**엔드포인트:**
```
POST /api/catalog-orders/{orderId}/confirm
```

---

## 📋 업데이트된 결제 플로우

### 전체 플로우
```
1. 주문 생성
   POST /api/catalog-orders/create
   → orderId: 1, orderNumber: "ORD-20251127-152346-940"
   → status: "PENDING"

2. 토스페이먼츠 결제
   → paymentKey: "tviva20251127152352DHaq8"
   → orderId: "ORD-20251127-152346-940"

3. 결제 승인
   POST /api/payments/confirm
   → status: "DONE"

4. 주문 확정 (새로 추가) ⭐
   POST /api/catalog-orders/{orderId}/confirm
   → status: "CONFIRMED"
```

---

## 💻 Flutter 구현

### 1. 결제 승인 후 주문 확정

```dart
Future<void> processPayment(Order order) async {
  try {
    // 1. 결제 정보 생성
    final paymentInfo = PaymentInfo.fromOrder(
      order,
      'customer@example.com',
      '홍길동',
    );
    
    // 2. 토스페이먼츠 결제
    final result = await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => PaymentWebView(paymentInfo: paymentInfo),
      ),
    );
    
    if (result == null || result['success'] != true) {
      _showMessage('결제가 취소되었습니다');
      return;
    }
    
    // 3. 결제 승인
    final paymentResponse = await confirmPayment(
      paymentKey: result['paymentKey'],
      orderId: result['orderId'],
      amount: int.parse(result['amount']),
    );
    
    if (paymentResponse.status != 'DONE') {
      _showMessage('결제 승인 실패');
      return;
    }
    
    print('✅ 결제 승인 성공: ${paymentResponse.paymentKey}');
    
    // 4. 주문 확정 (새로 추가) ⭐
    final confirmedOrder = await confirmOrder(order.id);
    
    print('✅ 주문 확정 성공: ${confirmedOrder.status}');
    
    // 5. 결제 완료 화면으로 이동
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(
        builder: (context) => PaymentSuccessPage(
          order: confirmedOrder,
          payment: paymentResponse,
        ),
      ),
    );
  } catch (e) {
    print('❌ 결제 처리 오류: $e');
    _showMessage('결제 처리 중 오류: $e');
  }
}
```

### 2. 주문 확정 API 호출

```dart
Future<Order> confirmOrder(int orderId) async {
  final url = Uri.parse('http://10.0.2.2:8080/api/catalog-orders/$orderId/confirm');
  
  print('주문 확정 요청: orderId=$orderId');
  
  final response = await http.post(
    url,
    headers: {
      'Authorization': 'Bearer $token',
      'Content-Type': 'application/json',
    },
  );
  
  if (response.statusCode == 200) {
    final order = Order.fromJson(
      jsonDecode(utf8.decode(response.bodyBytes))
    );
    print('주문 확정 성공: status=${order.status}');
    return order;
  } else {
    throw Exception('주문 확정 실패: ${response.body}');
  }
}
```

---

## 📊 주문 상태 변화

### 상태 전이
```
PENDING (주문대기)
   ↓ 결제 승인 완료
CONFIRMED (주문확정)
   ↓ 유통업체 처리
PREPARING (상품준비중)
   ↓ 배송 시작
SHIPPED (배송중)
   ↓ 배송 완료
DELIVERED (배송완료)
```

---

## 🎯 주문 확정 API 상세

### 엔드포인트
```
POST /api/catalog-orders/{orderId}/confirm
```

### 헤더
```
Authorization: Bearer {매장 로그인 토큰}
Content-Type: application/json
```

### 요청 예시
```dart
POST /api/catalog-orders/1/confirm
Authorization: Bearer eyJhbGci...
```

### 응답
```json
{
  "id": 1,
  "storeId": "김가게",
  "distributorId": "김유통선생님",
  "orderNumber": "ORD-20251127-152346-940",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "경기미 20kg",
      "unitPrice": 48000,
      "unit": "포",
      "quantity": 10,
      "subtotal": 480000
    }
  ],
  "totalAmount": 480000,
  "totalQuantity": 10,
  "status": "CONFIRMED",
  "statusDescription": "주문확정",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "orderedAt": "2023-11-27T15:23:46",
  "confirmedAt": "2023-11-27T15:24:05"
}
```

---

## 🔍 에러 처리

### 1. 주문 확정 실패
```dart
try {
  final confirmedOrder = await confirmOrder(order.id);
} catch (e) {
  if (e.toString().contains('주문을 찾을 수 없습니다')) {
    _showMessage('주문 정보를 찾을 수 없습니다');
  } else if (e.toString().contains('접근 권한이 없습니다')) {
    _showMessage('주문 확정 권한이 없습니다');
  } else if (e.toString().contains('대기 중인 주문만')) {
    _showMessage('이미 확정된 주문입니다');
  } else {
    _showMessage('주문 확정 실패: $e');
  }
}
```

### 2. 결제는 성공했지만 주문 확정 실패
```dart
// 결제 승인은 성공했으므로 사용자에게 안내
if (paymentResponse.status == 'DONE') {
  try {
    await confirmOrder(order.id);
  } catch (e) {
    // 주문 확정 실패 시에도 결제는 완료되었음을 안내
    _showMessage('결제는 완료되었습니다. 주문 상태는 관리자에게 문의하세요.');
    
    // 결제 완료 화면으로 이동 (주문 확정 없이)
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(
        builder: (context) => PaymentSuccessPage(
          order: order,
          payment: paymentResponse,
          note: '주문 확정 처리 중 오류가 발생했습니다. 관리자에게 문의하세요.',
        ),
      ),
    );
  }
}
```

---

## 💡 선택적 구현

### 방법 1: 주문 확정 API 호출 (권장)
결제 승인 후 명시적으로 주문 확정 API를 호출합니다.

**장점:**
- 명확한 상태 관리
- 에러 처리 가능
- 주문 상태 추적 용이

**단점:**
- API 호출 1회 추가

### 방법 2: 주문 확정 생략
결제 승인만 하고 주문 확정은 생략합니다.

**장점:**
- 구현 간단
- API 호출 감소

**단점:**
- 주문 상태가 PENDING으로 유지됨
- 유통업체가 주문을 확인하기 어려움

**권장: 방법 1 (주문 확정 API 호출)**

---

## 🧪 테스트 시나리오

### 정상 플로우
```
1. 주문 생성
   → status: PENDING

2. 결제 승인
   → payment status: DONE

3. 주문 확정
   → order status: CONFIRMED

4. 결제 완료 화면
   → 주문번호, 결제 정보 표시
```

### 에러 플로우
```
1. 주문 생성
   → status: PENDING

2. 결제 승인
   → payment status: DONE

3. 주문 확정 실패
   → 에러 메시지 표시
   → 결제는 완료되었음을 안내
```

---

## 📋 체크리스트

### Flutter 클라이언트
- [ ] 결제 승인 API 호출 구현
- [ ] 주문 확정 API 호출 추가
- [ ] 에러 처리 구현
- [ ] 결제 완료 화면 구현
- [ ] 로그 출력으로 디버깅

### 백엔드
- [x] PaymentService 수정 (주문 확정 로직 제거)
- [x] 주문 확정 API 추가
- [x] 에러 로깅 개선

---

## 🎯 요약

### 변경 사항
1. **PaymentService**: 주문 확정 로직 제거 (카탈로그 주문과 충돌 방지)
2. **CatalogOrderService**: 주문 확정 메서드 추가
3. **CatalogOrderController**: 주문 확정 API 추가

### Flutter 구현
```dart
// 결제 승인 후
final paymentResponse = await confirmPayment(...);

// 주문 확정 (새로 추가)
final confirmedOrder = await confirmOrder(order.id);

// 결제 완료 화면
Navigator.pushReplacement(...);
```

### API 엔드포인트
```
POST /api/payments/confirm          // 결제 승인
POST /api/catalog-orders/{id}/confirm  // 주문 확정 (새로 추가)
```

---

## 📞 추가 지원

문제가 계속되면 다음 정보를 제공해주세요:

1. Flutter 로그 (결제 승인 ~ 주문 확정)
2. 백엔드 로그
3. 에러 메시지

이제 결제가 정상적으로 작동할 것입니다! 🎉
