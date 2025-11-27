# Flutter 결제 API 가이드 - 토스페이먼츠 연동

## 📋 개요

가게 사장님이 장바구니에서 주문을 생성한 후, **토스페이먼츠**를 통해 결제를 진행하는 API 가이드입니다.

**Base URL**: `http://10.0.2.2:8080` (Android 에뮬레이터)

---

## 🎯 결제 흐름

```
1. 장바구니에서 주문 생성
   POST /api/orders/create
   ↓
2. 결제 준비 (결제 정보 생성)
   주문 정보를 기반으로 결제 데이터 준비
   ↓
3. 토스페이먼츠 결제 위젯 호출
   Flutter WebView 또는 외부 브라우저
   ↓
4. 결제 승인 요청
   POST /api/orders/{orderId}/payment/confirm
   ↓
5. 결제 완료
   주문 상태 업데이트 및 결제 완료 화면
```

---

## 📦 API 엔드포인트

### 1. 주문 생성 (결제 전)

**엔드포인트:**
```
POST /api/orders/create
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}
Content-Type: application/json
```

**요청 Body:**
```json
{
  "distributorId": "distributor1",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요"
}
```

**응답:**
```json
{
  "id": 1,
  "storeId": "test_store",
  "distributorId": "distributor1",
  "orderNumber": "ORD-20231126-143025-456",
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
  "orderedAt": "2023-11-26T14:30:25"
}
```

---

### 2. 결제 준비 (결제 정보 생성)

주문 생성 후, 결제를 위한 정보를 준비합니다. 별도의 API 호출 없이 **주문 정보를 기반으로 클라이언트에서 생성**합니다.

**결제 정보 구조:**
```dart
class PaymentInfo {
  final String orderId;           // "ORDER-{주문ID}" 형식
  final String orderName;         // 주문명 (예: "경기미 20kg 외 2건")
  final int amount;               // 결제 금액
  final String customerEmail;     // 고객 이메일
  final String customerName;      // 고객 이름
  
  // 생성 예시
  static PaymentInfo fromOrder(Order order, String email, String name) {
    return PaymentInfo(
      orderId: "ORDER-${order.id}",
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

---

### 3. 토스페이먼츠 결제 호출

Flutter에서 토스페이먼츠 결제를 호출하는 방법은 **2가지**가 있습니다:

#### 방법 1: WebView 사용 (권장)

```dart
import 'package:webview_flutter/webview_flutter.dart';

class PaymentWebView extends StatefulWidget {
  final PaymentInfo paymentInfo;
  
  @override
  _PaymentWebViewState createState() => _PaymentWebViewState();
}

class _PaymentWebViewState extends State<PaymentWebView> {
  late WebViewController _controller;
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('결제하기')),
      body: WebView(
        initialUrl: 'about:blank',
        javascriptMode: JavascriptMode.unrestricted,
        onWebViewCreated: (controller) {
          _controller = controller;
          _loadPaymentPage();
        },
        navigationDelegate: (navigation) {
          final uri = Uri.parse(navigation.url);
          
          // 결제 성공 콜백
          if (uri.path.contains('/payment/success')) {
            _handlePaymentSuccess(uri.queryParameters);
            return NavigationDecision.prevent;
          }
          
          // 결제 실패 콜백
          if (uri.path.contains('/payment/fail')) {
            _handlePaymentFail(uri.queryParameters);
            return NavigationDecision.prevent;
          }
          
          return NavigationDecision.navigate;
        },
      ),
    );
  }
  
  void _loadPaymentPage() {
    final html = '''
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://js.tosspayments.com/v1/payment"></script>
</head>
<body>
    <script>
        // 백엔드와 동일한 클라이언트 키 사용
        const clientKey = 'test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq';
        const tossPayments = TossPayments(clientKey);
        
        const paymentInfo = {
            orderId: '${widget.paymentInfo.orderId}',
            orderName: '${widget.paymentInfo.orderName}',
            amount: ${widget.paymentInfo.amount},
            customerEmail: '${widget.paymentInfo.customerEmail}',
            customerName: '${widget.paymentInfo.customerName}'
        };
        
        // 자동으로 결제 시작
        tossPayments.requestPayment('카드', {
            amount: paymentInfo.amount,
            orderId: paymentInfo.orderId,
            orderName: paymentInfo.orderName,
            customerName: paymentInfo.customerName,
            customerEmail: paymentInfo.customerEmail,
            successUrl: window.location.origin + '/payment/success',
            failUrl: window.location.origin + '/payment/fail',
        }).catch(function(error) {
            alert('결제 요청 실패: ' + error.message);
        });
    </script>
</body>
</html>
    ''';
    
    _controller.loadUrl(Uri.dataFromString(
      html,
      mimeType: 'text/html',
      encoding: Encoding.getByName('utf-8'),
    ).toString());
  }
  
  void _handlePaymentSuccess(Map<String, String> params) {
    final paymentKey = params['paymentKey'];
    final orderId = params['orderId'];
    final amount = params['amount'];
    
    // 결제 승인 API 호출
    Navigator.pop(context, {
      'success': true,
      'paymentKey': paymentKey,
      'orderId': orderId,
      'amount': amount,
    });
  }
  
  void _handlePaymentFail(Map<String, String> params) {
    final code = params['code'];
    final message = params['message'];
    
    Navigator.pop(context, {
      'success': false,
      'code': code,
      'message': message,
    });
  }
}
```

#### 방법 2: 외부 브라우저 사용

```dart
import 'package:url_launcher/url_launcher.dart';

Future<void> openPaymentInBrowser(PaymentInfo paymentInfo) async {
  // 백엔드에 결제 페이지 요청
  final paymentUrl = 'http://10.0.2.2:8080/payment/${paymentInfo.orderId}';
  
  if (await canLaunch(paymentUrl)) {
    await launch(paymentUrl);
  } else {
    throw '결제 페이지를 열 수 없습니다';
  }
}
```

---

### 4. 결제 승인 API ⭐

토스페이먼츠에서 결제가 성공하면, **반드시 서버에서 결제 승인**을 해야 합니다.

**엔드포인트:**
```
POST /api/payments/confirm
```

**헤더:**
```
Content-Type: application/json
```

**중요:** 이 API는 **인증 토큰이 필요 없습니다**. `paymentKey`가 인증 역할을 합니다.

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

**결제 상태 (status):**
- `READY`: 결제 준비
- `DONE`: 결제 완료
- `FAILED`: 결제 실패
- `CANCELED`: 결제 취소

---

## 💾 Dart 모델

### PaymentInfo (결제 정보)
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
  
  static PaymentInfo fromOrder(Order order, String email, String name) {
    return PaymentInfo(
      orderId: "ORDER-${order.id}",
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

### PaymentResponse (결제 승인 응답)
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

## 🔄 전체 결제 플로우 예시

```dart
class CheckoutPage extends StatefulWidget {
  final Order order;
  
  @override
  _CheckoutPageState createState() => _CheckoutPageState();
}

class _CheckoutPageState extends State<CheckoutPage> {
  bool _isProcessing = false;
  
  Future<void> _processPayment() async {
    setState(() => _isProcessing = true);
    
    try {
      // 1. 결제 정보 생성
      final paymentInfo = PaymentInfo.fromOrder(
        widget.order,
        'customer@example.com',
        '홍길동',
      );
      
      // 2. 토스페이먼츠 결제 호출
      final result = await Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => PaymentWebView(paymentInfo: paymentInfo),
        ),
      );
      
      if (result == null) {
        // 사용자가 취소
        _showMessage('결제가 취소되었습니다');
        return;
      }
      
      if (result['success'] == true) {
        // 3. 결제 승인 API 호출
        final paymentResponse = await _confirmPayment(
          paymentKey: result['paymentKey'],
          orderId: result['orderId'],
          amount: int.parse(result['amount']),
        );
        
        // 4. 결제 완료 확인
        if (paymentResponse.status == 'DONE') {
          // 5. 결제 완료 화면으로 이동
          Navigator.pushReplacement(
            context,
            MaterialPageRoute(
              builder: (context) => PaymentSuccessPage(
                order: widget.order,
                payment: paymentResponse,
              ),
            ),
          );
        } else {
          _showMessage('결제 처리 중 오류가 발생했습니다');
        }
      } else {
        // 결제 실패
        _showMessage('결제 실패: ${result['message']}');
      }
    } catch (e) {
      _showMessage('결제 처리 중 오류: $e');
    } finally {
      setState(() => _isProcessing = false);
    }
  }
  
  Future<PaymentResponse> _confirmPayment({
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
      throw Exception('결제 승인 실패');
    }
  }
  
  void _showMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message)),
    );
  }
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('결제하기')),
      body: Column(
        children: [
          // 주문 정보 표시
          Expanded(
            child: ListView(
              padding: EdgeInsets.all(16),
              children: [
                Text('주문 번호: ${widget.order.orderNumber}'),
                SizedBox(height: 16),
                Text('총 금액: ${widget.order.totalAmount}원',
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
                ),
                // ... 주문 상품 목록
              ],
            ),
          ),
          
          // 결제 버튼
          Padding(
            padding: EdgeInsets.all(16),
            child: ElevatedButton(
              onPressed: _isProcessing ? null : _processPayment,
              child: _isProcessing
                  ? CircularProgressIndicator()
                  : Text('${widget.order.totalAmount}원 결제하기'),
              style: ElevatedButton.styleFrom(
                minimumSize: Size(double.infinity, 56),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
```

---

## 🎨 UI 구현 가이드

### 1. 주문 확인 화면
```
┌─────────────────────────────────────┐
│  주문 확인                          │
├─────────────────────────────────────┤
│  📦 주문 상품                       │
│  경기미 20kg x 10포 = 480,000원    │
│  국산 양파 x 10kg = 30,000원       │
│                                     │
│  💰 결제 금액                       │
│  상품 금액: 510,000원               │
│  배송비: 무료                       │
│  총 결제 금액: 510,000원            │
│                                     │
│  🚚 배송 정보                       │
│  주소: 서울시 강남구 테헤란로 123   │
│  전화번호: 010-1234-5678            │
│  요청사항: 문 앞에 놓아주세요       │
│                                     │
│  [510,000원 결제하기]               │
└─────────────────────────────────────┘
```

### 2. 결제 진행 화면 (WebView)
```
┌─────────────────────────────────────┐
│  [← 뒤로]    결제하기               │
├─────────────────────────────────────┤
│                                     │
│  [토스페이먼츠 결제 위젯]           │
│                                     │
│  - 카드 결제                        │
│  - 계좌이체                         │
│  - 가상계좌                         │
│  - 간편결제                         │
│                                     │
└─────────────────────────────────────┘
```

### 3. 결제 완료 화면
```
┌─────────────────────────────────────┐
│  ✅ 결제가 완료되었습니다!          │
├─────────────────────────────────────┤
│  주문번호: ORD-20231126-143025-456  │
│  결제일시: 2023-11-26 14:35         │
│  결제방법: 신용카드                 │
│                                     │
│  📦 주문 상품                       │
│  경기미 20kg x 10포                │
│  국산 양파 x 10kg                  │
│                                     │
│  💰 결제 금액: 510,000원            │
│  🚚 배송 상태: 주문확정             │
│                                     │
│  [주문 상세 보기]                   │
│  [주문 목록으로]                    │
└─────────────────────────────────────┘
```

---

## ⚠️ 주의사항

### 1. 토스페이먼츠 클라이언트 키

**현재 백엔드에 설정된 키:**
```dart
// 테스트 환경 (현재 사용 중)
const clientKey = 'test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq';

// 프로덕션 환경 (실제 서비스 시 교체 필요)
const prodClientKey = 'YOUR_PRODUCTION_CLIENT_KEY';
```

**백엔드 설정 위치:**
- 파일: `src/main/resources/application.properties`
- 설정: `toss.payments.client-key=test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq`
- Secret Key: `test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R` (서버 전용)

### 2. 결제 금액 검증
- 클라이언트에서 보낸 금액과 서버의 주문 금액이 일치해야 함
- 서버에서 최종 검증 수행

### 3. 결제 승인 필수
- 토스페이먼츠 결제 성공 후 **반드시 서버에서 승인** 필요
- 승인하지 않으면 자동 취소됨

### 4. 에러 처리
```dart
try {
  // 결제 처리
} catch (e) {
  if (e.toString().contains('USER_CANCEL')) {
    // 사용자 취소
  } else if (e.toString().contains('INVALID_CARD')) {
    // 카드 오류
  } else {
    // 기타 오류
  }
}
```

### 5. 콜백 URL
- `successUrl`: 결제 성공 시 리다이렉트
- `failUrl`: 결제 실패 시 리다이렉트
- WebView에서 URL 변경 감지하여 처리

---

## 📱 필요한 패키지

```yaml
dependencies:
  http: ^1.1.0
  webview_flutter: ^4.4.2  # WebView 사용 시
  url_launcher: ^6.2.1     # 외부 브라우저 사용 시
```

---

## 🧪 테스트

### 테스트 카드 정보
토스페이먼츠 테스트 환경에서 사용 가능한 카드:

```
카드번호: 아무 16자리 숫자
유효기간: 미래 날짜
CVC: 아무 3자리 숫자
비밀번호: 아무 2자리 숫자
```

---

## 📞 참고 문서

- **토스페이먼츠 공식 문서**: https://docs.tosspayments.com/
- **Flutter WebView**: https://pub.dev/packages/webview_flutter
- **백엔드 API 문서**: `BACKEND_API_DOCUMENTATION.md`

---

## 💡 개발 팁

1. **WebView 권장**: 외부 브라우저보다 WebView 사용이 UX가 좋음
2. **로딩 상태**: 결제 진행 중 로딩 인디케이터 표시
3. **뒤로가기 처리**: 결제 중 뒤로가기 시 확인 다이얼로그
4. **결제 내역 저장**: 로컬에 결제 내역 캐싱
5. **재시도 로직**: 네트워크 오류 시 재시도 기능

---

## ✅ 구현 체크리스트

- [ ] 주문 생성 API 연동
- [ ] 결제 정보 생성 로직
- [ ] 토스페이먼츠 WebView 구현
- [ ] 결제 성공/실패 콜백 처리
- [ ] 결제 승인 API 연동
- [ ] 결제 완료 화면
- [ ] 에러 처리 및 사용자 안내
- [ ] 로딩 상태 표시
- [ ] 결제 내역 조회

---

## 🎯 요약

**가게 사장님 결제 플로우:**

1. **결제 준비 API**: 별도 API 없음 (주문 정보 기반으로 클라이언트에서 생성)
2. **필요한 파라미터**: 
   - `orderId`: "ORDER-{주문ID}"
   - `orderName`: "경기미 20kg 외 2건"
   - `amount`: 주문 금액
   - `customerEmail`: 고객 이메일
   - `customerName`: 고객 이름

3. **응답 형식**: 토스페이먼츠에서 `paymentKey`, `orderId`, `amount` 반환

4. **결제 승인 API**: `POST /api/orders/{orderId}/payment/confirm`
   - Body: `{ paymentKey, orderId, amount }`

5. **콜백 URL**: WebView에서 URL 변경 감지
   - 성공: `/payment/success?paymentKey=xxx&orderId=xxx&amount=xxx`
   - 실패: `/payment/fail?code=xxx&message=xxx`
