# orderId 형식 가이드

## ✅ 주문번호 형식

### 생성 규칙
```java
// CatalogOrderService.generateOrderNumber()
private String generateOrderNumber() {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    String random = String.format("%03d", (int)(Math.random() * 1000));
    return "ORD-" + timestamp + "-" + random;
}
```

### 형식
```
ORD-YYYYMMDD-HHMMSS-XXX
```

- **ORD**: 고정 접두사 (Order)
- **YYYYMMDD**: 연월일 (예: 20251127)
- **HHMMSS**: 시분초 (예: 035844)
- **XXX**: 랜덤 3자리 숫자 (000-999)

### 실제 예시
```
ORD-20251127-035844-153
ORD-20251127-124326-789
ORD-20251128-093015-042
```

---

## 🔍 토스페이먼츠 규칙 검증

### 토스페이먼츠 orderId 요구사항
1. ✅ **영문 대소문자, 숫자, 특수문자(-, _) 만 허용**
2. ✅ **6자 이상 64자 이하**

### 우리 형식 검증

| 항목 | 규칙 | 우리 형식 | 결과 |
|------|------|-----------|------|
| 문자 종류 | 영문, 숫자, -, _ | `ORD-20251127-035844-153` | ✅ 통과 |
| 한글 포함 | 불가 | 한글 없음 | ✅ 통과 |
| 특수문자 | -, _ 만 허용 | `-` 만 사용 | ✅ 통과 |
| 길이 | 6-64자 | 24자 | ✅ 통과 |

**결론: 완벽하게 토스페이먼츠 규칙을 준수합니다!** ✅

---

## 📋 주문 생성 API 응답

### API 엔드포인트
```
POST /api/catalog-orders/create
```

### 요청 Body
```json
{
  "distributorId": "distributor1",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요"
}
```

### 응답 JSON (실제 예시)
```json
{
  "id": 1,
  "storeId": "test_store",
  "distributorId": "distributor1",
  "orderNumber": "ORD-20251127-035844-153",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "경기미 20kg",
      "unitPrice": 48000,
      "unit": "포",
      "quantity": 10,
      "subtotal": 480000,
      "imageUrl": "https://example.com/rice.jpg"
    }
  ],
  "totalAmount": 480000,
  "totalQuantity": 10,
  "status": "PENDING",
  "statusDescription": "주문대기",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요",
  "orderedAt": "2023-11-27T03:58:44"
}
```

### 중요 필드
- **`orderNumber`**: `"ORD-20251127-035844-153"` ← 이 값을 `orderId`로 사용
- **`totalAmount`**: `480000` ← 결제 금액

---

## 💡 Flutter에서 사용 방법

### 1. 주문 생성 후 orderNumber 추출
```dart
// 주문 생성
final response = await http.post(
  Uri.parse('http://10.0.2.2:8080/api/catalog-orders/create'),
  headers: {
    'Authorization': 'Bearer $token',
    'Content-Type': 'application/json',
  },
  body: jsonEncode({
    'distributorId': 'distributor1',
    'deliveryAddress': '서울시 강남구 테헤란로 123',
    'deliveryPhone': '010-1234-5678',
    'deliveryRequest': '문 앞에 놓아주세요',
  }),
);

final order = Order.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));

// orderNumber 확인
print('주문번호: ${order.orderNumber}');  // "ORD-20251127-035844-153"
print('주문번호 길이: ${order.orderNumber.length}');  // 24
```

### 2. 토스페이먼츠에 전달
```dart
// 결제 정보 생성
final paymentInfo = PaymentInfo(
  orderId: order.orderNumber,  // "ORD-20251127-035844-153"
  orderName: _generateOrderName(order.items),
  amount: order.totalAmount,
  customerEmail: 'customer@example.com',
  customerName: '홍길동',
);

// 토스페이먼츠 호출
await tossPayments.requestPayment('카드', {
  orderId: paymentInfo.orderId,  // "ORD-20251127-035844-153"
  orderName: paymentInfo.orderName,
  amount: paymentInfo.amount,
  customerName: paymentInfo.customerName,
  customerEmail: paymentInfo.customerEmail,
  successUrl: window.location.origin + '/payment/success',
  failUrl: window.location.origin + '/payment/fail',
});
```

### 3. 결제 승인 API 호출
```dart
// 토스 결제 성공 후
final paymentResponse = await confirmPayment(
  paymentKey: result['paymentKey'],
  orderId: result['orderId'],  // "ORD-20251127-035844-153"
  amount: order.totalAmount,
);
```

---

## 🔍 orderId 검증 함수

### Dart 검증 함수
```dart
bool isValidTossOrderId(String orderId) {
  // 1. 길이 체크 (6-64자)
  if (orderId.length < 6 || orderId.length > 64) {
    print('❌ orderId 길이 오류: ${orderId.length}자 (6-64자 필요)');
    return false;
  }
  
  // 2. 허용된 문자만 사용 (영문, 숫자, -, _)
  final validPattern = RegExp(r'^[a-zA-Z0-9\-_]+$');
  if (!validPattern.hasMatch(orderId)) {
    print('❌ orderId 형식 오류: 영문, 숫자, -, _ 만 허용');
    return false;
  }
  
  print('✅ orderId 검증 통과: $orderId');
  return true;
}

// 사용 예시
void main() {
  print(isValidTossOrderId('ORD-20251127-035844-153'));  // ✅ true
  print(isValidTossOrderId('주문-20251127-035844-153'));  // ❌ false (한글)
  print(isValidTossOrderId('ORD_20251127_035844_153'));  // ✅ true
  print(isValidTossOrderId('ORD'));                      // ❌ false (너무 짧음)
}
```

---

## 📊 형식 비교

| 형식 | 예시 | 토스 규칙 | 사용 가능 |
|------|------|-----------|-----------|
| 현재 형식 | `ORD-20251127-035844-153` | ✅ 통과 | ✅ 사용 |
| 한글 포함 | `주문-20251127-035844-153` | ❌ 실패 | ❌ 불가 |
| 언더스코어 | `ORD_20251127_035844_153` | ✅ 통과 | ✅ 가능 |
| 숫자만 | `20251127035844153` | ✅ 통과 | ✅ 가능 |
| 특수문자 | `ORD@20251127#035844` | ❌ 실패 | ❌ 불가 |
| 너무 짧음 | `ORD` | ❌ 실패 | ❌ 불가 |

---

## 🧪 테스트 예시

### 주문 생성 테스트
```bash
curl -X POST http://localhost:8080/api/catalog-orders/create \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "distributorId": "distributor1",
    "deliveryAddress": "서울시 강남구 테헤란로 123",
    "deliveryPhone": "010-1234-5678",
    "deliveryRequest": "문 앞에 놓아주세요"
  }'
```

### 응답 확인
```json
{
  "orderNumber": "ORD-20251127-035844-153",
  "totalAmount": 480000,
  ...
}
```

### orderId 검증
```bash
# 길이 확인
echo "ORD-20251127-035844-153" | wc -c
# 출력: 24 (✅ 6-64자 범위)

# 형식 확인
echo "ORD-20251127-035844-153" | grep -E '^[a-zA-Z0-9_-]+$'
# 출력: ORD-20251127-035844-153 (✅ 매칭됨)
```

---

## ⚠️ 주의사항

### 1. orderNumber vs orderId
```dart
// ✅ 올바른 방법
orderId: order.orderNumber  // "ORD-20251127-035844-153"

// ❌ 잘못된 방법
orderId: order.id.toString()  // "1" (너무 짧음)
orderId: "ORDER-${order.id}"  // "ORDER-1" (너무 짧음)
```

### 2. 한글 사용 금지
```dart
// ❌ 한글 포함 시 토스페이먼츠 오류 발생
orderId: "주문-20251127-035844-153"  // 한글 불가
orderName: "경기미 20kg 외 2건"      // orderName은 한글 가능
```

### 3. 길이 제한
```dart
// orderId는 6-64자
assert(orderId.length >= 6 && orderId.length <= 64);

// 우리 형식은 24자로 안전
"ORD-20251127-035844-153".length  // 24
```

---

## 🎯 요약

### 질문에 대한 답변

**Q: 주문 생성 API가 반환하는 orderId 형식이 정확히 무엇인가요?**

**A:** `ORD-YYYYMMDD-HHMMSS-XXX` 형식입니다.

**실제 예시:**
```json
{
  "orderNumber": "ORD-20251127-035844-153"
}
```

**형식 분석:**
- `ORD`: 고정 접두사
- `20251127`: 날짜 (2025년 11월 27일)
- `035844`: 시간 (03시 58분 44초)
- `153`: 랜덤 3자리 숫자

**토스페이먼츠 규칙 준수:**
- ✅ 영문 대소문자, 숫자, 특수문자(-, _) 만 사용
- ✅ 24자 (6-64자 범위 내)
- ✅ 한글 없음
- ✅ 허용된 특수문자만 사용 (-)

**결론: 현재 형식은 토스페이먼츠 규칙을 완벽하게 준수합니다!** ✅

---

## 📞 추가 지원

만약 토스페이먼츠에서 orderId 오류가 발생한다면:

1. **orderId 값 확인**
   ```dart
   print('orderId: ${paymentInfo.orderId}');
   print('orderId 길이: ${paymentInfo.orderId.length}');
   print('orderId 검증: ${isValidTossOrderId(paymentInfo.orderId)}');
   ```

2. **토스 에러 메시지 확인**
   - 에러 코드와 메시지를 공유해주세요

3. **실제 전송 데이터 확인**
   - 토스페이먼츠에 전달되는 전체 파라미터를 확인해주세요
