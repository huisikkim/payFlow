# 토스페이먼츠 400 Bad Request 에러 해결 가이드

## 🔍 발생한 에러

```
2025-11-27 06:06:49.971 ERROR 10 --- [nio-8080-exec-2] c.e.p.p.i.TossPaymentsClient : 토스페이먼츠 결제 승인 실패
org.springframework.web.reactive.function.client.WebClientResponseException$BadRequest: 400 Bad Request from POST https://api.tosspayments.com/v1/payments/confirm
```

---

## 📋 로그 분석

### 1. 주문 생성 성공 ✅
```
주문 생성 완료: ORD-20251127-060610-833 (매장: 김가게, 유통업체: 김유통)
결제 생성: orderId=ORD-20251127-060610-833, processingTime=99ms
```

### 2. 결제 승인 요청 ✅
```
토스페이먼츠 결제 승인 요청: orderId=ORD-20251127-060610-833, amount=1000
```

### 3. 토스페이먼츠 API 오류 ❌
```
400 Bad Request from POST https://api.tosspayments.com/v1/payments/confirm
```

---

## 🎯 가능한 원인

### 1. paymentKey 불일치 (가장 가능성 높음)
토스페이먼츠에서 발급한 `paymentKey`와 서버로 전달된 `paymentKey`가 다를 수 있습니다.

**확인 방법:**
```dart
// Flutter에서 토스 결제 성공 후
print('토스 paymentKey: ${result['paymentKey']}');
print('토스 orderId: ${result['orderId']}');
print('토스 amount: ${result['amount']}');

// 서버로 전달하는 값
print('서버 paymentKey: $paymentKey');
print('서버 orderId: $orderId');
print('서버 amount: $amount');
```

### 2. orderId 불일치
토스페이먼츠에 전달한 `orderId`와 승인 요청의 `orderId`가 다를 수 있습니다.

**문제 예시:**
```dart
// 토스 결제 시
orderId: "ORD-20251127-060610-833"

// 승인 요청 시
orderId: "ORDER-1732689537123"  // ❌ 다른 값!
```

### 3. 금액 불일치
토스페이먼츠에 전달한 금액과 승인 요청의 금액이 다를 수 있습니다.

**문제 예시:**
```dart
// 토스 결제 시
amount: 1000

// 승인 요청 시
amount: 10000  // ❌ 다른 값!
```

### 4. 이미 승인된 결제
동일한 `paymentKey`로 중복 승인 요청을 했을 수 있습니다.

### 5. 만료된 paymentKey
토스페이먼츠 결제 후 10분 이내에 승인하지 않으면 자동 취소됩니다.

### 6. 테스트 환경 설정 오류
- 테스트 시크릿 키를 사용하지 않음
- 테스트 클라이언트 키와 시크릿 키가 매칭되지 않음

---

## 🔧 해결 방법

### 1. 에러 응답 상세 로그 확인 (백엔드 수정 완료)

**수정 내용:**
`TossPaymentsClient.java`에 에러 응답 본문 로깅 추가

```java
.onStatus(
    status -> status.is4xxClientError() || status.is5xxServerError(),
    clientResponse -> clientResponse.bodyToMono(String.class)
        .map(errorBody -> {
            log.error("토스페이먼츠 에러 응답: status={}, body={}", 
                clientResponse.statusCode(), errorBody);
            return new RuntimeException("토스페이먼츠 API 오류: " + errorBody);
        })
)
```

**다음 테스트 시 확인할 로그:**
```
토스페이먼츠 에러 응답: status=400, body={"code":"INVALID_REQUEST","message":"..."}
```

### 2. Flutter 클라이언트 디버깅

**WebView에서 콜백 파라미터 확인:**
```dart
void _handlePaymentSuccess(Map<String, String> params) {
  final paymentKey = params['paymentKey'];
  final orderId = params['orderId'];
  final amount = params['amount'];
  
  // 로그 출력
  print('=== 토스 결제 성공 콜백 ===');
  print('paymentKey: $paymentKey');
  print('orderId: $orderId');
  print('amount: $amount');
  print('========================');
  
  // 서버로 전달
  Navigator.pop(context, {
    'success': true,
    'paymentKey': paymentKey,
    'orderId': orderId,
    'amount': amount,
  });
}
```

**결제 승인 API 호출 전 확인:**
```dart
Future<PaymentResponse> confirmPayment({
  required String paymentKey,
  required String orderId,
  required int amount,
}) async {
  // 로그 출력
  print('=== 결제 승인 요청 ===');
  print('paymentKey: $paymentKey');
  print('orderId: $orderId');
  print('amount: $amount');
  print('====================');
  
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
  
  print('응답 상태: ${response.statusCode}');
  print('응답 본문: ${response.body}');
  
  if (response.statusCode == 200) {
    return PaymentResponse.fromJson(
      jsonDecode(utf8.decode(response.bodyBytes))
    );
  } else {
    throw Exception('결제 승인 실패: ${response.body}');
  }
}
```

### 3. orderId 일치 확인

**전체 플로우에서 동일한 orderId 사용:**
```dart
// 1. 주문 생성
final order = await orderService.createOrder(...);
print('주문번호: ${order.orderNumber}');  // "ORD-20251127-060610-833"

// 2. 결제 정보 생성
final paymentInfo = PaymentInfo(
  orderId: order.orderNumber,  // ✅ 주문번호 사용
  orderName: '경기미 20kg',
  amount: order.totalAmount,
  customerEmail: 'customer@example.com',
  customerName: '홍길동',
);
print('결제 orderId: ${paymentInfo.orderId}');  // "ORD-20251127-060610-833"

// 3. 토스페이먼츠 호출
await tossPayments.requestPayment('카드', {
  orderId: paymentInfo.orderId,  // ✅ 동일한 값
  // ...
});

// 4. 결제 승인
await confirmPayment(
  paymentKey: result['paymentKey'],
  orderId: result['orderId'],  // ✅ 토스에서 받은 값 (동일해야 함)
  amount: order.totalAmount,
);
```

### 4. 금액 타입 확인

**토스페이먼츠는 정수(Long) 타입 요구:**
```dart
// ✅ 올바른 방법
amount: 1000  // int

// ❌ 잘못된 방법
amount: "1000"  // String
amount: 1000.0  // double
```

**백엔드 DTO 확인:**
```java
public class TossPaymentConfirmRequest {
    private String paymentKey;
    private String orderId;
    private Long amount;  // ✅ Long 타입
}
```

### 5. 테스트 환경 설정 확인

**application.properties 확인:**
```properties
toss.payments.secret-key=test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R
toss.payments.client-key=test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq
toss.payments.api-url=https://api.tosspayments.com/v1
```

**Flutter에서 동일한 클라이언트 키 사용:**
```dart
const clientKey = 'test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq';
```

---

## 🧪 테스트 시나리오

### 1. 정상 플로우 테스트

```dart
void testPaymentFlow() async {
  // 1. 주문 생성
  final order = await createOrder();
  print('✅ 주문 생성: ${order.orderNumber}');
  
  // 2. 결제 정보 생성
  final paymentInfo = PaymentInfo.fromOrder(order, 'test@example.com', '테스터');
  print('✅ 결제 정보: orderId=${paymentInfo.orderId}, amount=${paymentInfo.amount}');
  
  // 3. orderId 일치 확인
  assert(paymentInfo.orderId == order.orderNumber, 'orderId 불일치!');
  print('✅ orderId 일치 확인');
  
  // 4. 토스페이먼츠 결제
  final result = await openPaymentWebView(paymentInfo);
  print('✅ 토스 결제 성공: paymentKey=${result['paymentKey']}');
  
  // 5. orderId 일치 확인
  assert(result['orderId'] == order.orderNumber, 'orderId 불일치!');
  print('✅ 토스 orderId 일치 확인');
  
  // 6. 금액 일치 확인
  assert(int.parse(result['amount']) == order.totalAmount, '금액 불일치!');
  print('✅ 금액 일치 확인');
  
  // 7. 결제 승인
  final payment = await confirmPayment(
    paymentKey: result['paymentKey'],
    orderId: result['orderId'],
    amount: int.parse(result['amount']),
  );
  print('✅ 결제 승인 성공: status=${payment.status}');
}
```

### 2. 에러 케이스 테스트

```dart
// 케이스 1: 잘못된 paymentKey
try {
  await confirmPayment(
    paymentKey: 'invalid_key',
    orderId: order.orderNumber,
    amount: order.totalAmount,
  );
} catch (e) {
  print('❌ 예상된 에러: $e');
}

// 케이스 2: 다른 orderId
try {
  await confirmPayment(
    paymentKey: validPaymentKey,
    orderId: 'WRONG-ORDER-ID',
    amount: order.totalAmount,
  );
} catch (e) {
  print('❌ 예상된 에러: $e');
}

// 케이스 3: 다른 금액
try {
  await confirmPayment(
    paymentKey: validPaymentKey,
    orderId: order.orderNumber,
    amount: 99999,  // 잘못된 금액
  );
} catch (e) {
  print('❌ 예상된 에러: $e');
}
```

---

## 📊 토스페이먼츠 에러 코드

| 코드 | 설명 | 해결 방법 |
|------|------|-----------|
| `INVALID_REQUEST` | 잘못된 요청 | 파라미터 확인 |
| `NOT_FOUND_PAYMENT` | 결제 정보 없음 | paymentKey 확인 |
| `ALREADY_PROCESSED_PAYMENT` | 이미 처리된 결제 | 중복 요청 확인 |
| `PROVIDER_ERROR` | PG사 오류 | 잠시 후 재시도 |
| `EXCEED_MAX_AUTH_COUNT` | 최대 승인 횟수 초과 | 새로운 결제 시작 |
| `INVALID_API_KEY` | 잘못된 API 키 | 시크릿 키 확인 |

---

## 🔍 디버깅 체크리스트

### Flutter 클라이언트
- [ ] 주문 생성 후 `order.orderNumber` 값 확인
- [ ] `PaymentInfo.orderId`에 `order.orderNumber` 사용
- [ ] 토스페이먼츠 호출 시 동일한 `orderId` 사용
- [ ] 토스 성공 콜백에서 받은 파라미터 로그 출력
- [ ] 결제 승인 API 호출 전 파라미터 로그 출력
- [ ] 금액이 정수(int) 타입인지 확인

### 백엔드
- [ ] 토스페이먼츠 에러 응답 본문 로그 확인
- [ ] `paymentKey`, `orderId`, `amount` 값 로그 확인
- [ ] 테스트 시크릿 키 설정 확인
- [ ] API URL 확인 (`https://api.tosspayments.com/v1`)

---

## 💡 다음 단계

### 1. 백엔드 재시작
수정된 `TossPaymentsClient.java`를 적용하기 위해 서버 재시작:
```bash
./gradlew bootRun
```

### 2. 다시 테스트
Flutter 앱에서 결제 플로우 재실행

### 3. 에러 로그 확인
이번에는 상세한 에러 메시지가 출력됩니다:
```
토스페이먼츠 에러 응답: status=400, body={"code":"INVALID_REQUEST","message":"orderId가 일치하지 않습니다"}
```

### 4. 에러 메시지 공유
에러 메시지를 공유해주시면 정확한 해결 방법을 제시하겠습니다.

---

## 📞 추가 지원

다음 정보를 제공해주시면 더 정확한 진단이 가능합니다:

1. **Flutter 로그:**
   ```
   토스 결제 성공 콜백:
   paymentKey: xxx
   orderId: xxx
   amount: xxx
   
   결제 승인 요청:
   paymentKey: xxx
   orderId: xxx
   amount: xxx
   ```

2. **백엔드 로그:**
   ```
   토스페이먼츠 에러 응답: status=400, body={...}
   ```

3. **주문 생성 응답:**
   ```json
   {
     "orderNumber": "ORD-20251127-060610-833",
     "totalAmount": 1000
   }
   ```

---

## 🎯 가장 가능성 높은 원인

로그를 보면:
```
orderId=ORD-20251127-060610-833, amount=1000
```

orderId 형식은 올바르고, 금액도 정상입니다.

**가장 가능성 높은 원인:**
1. **paymentKey 불일치** - 토스에서 받은 paymentKey와 서버로 전달한 paymentKey가 다름
2. **orderId 불일치** - 토스 결제 시 사용한 orderId와 승인 요청의 orderId가 다름

**확인 방법:**
Flutter에서 토스 결제 성공 후 받은 파라미터를 로그로 출력하고, 서버로 전달하는 값과 비교해보세요.
