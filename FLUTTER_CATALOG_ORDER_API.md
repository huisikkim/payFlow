# 식자재 카탈로그 & 주문 API - Flutter 가이드

## 📋 개요

유통업체의 식자재 카탈로그를 조회하고, 장바구니에 담아 주문 리스트를 작성하는 기능입니다.

**Base URL**: `http://10.0.2.2:8080` (Android 에뮬레이터)

---

## 🎯 전체 흐름

```
1. 유통업체 카탈로그 조회
   ↓
2. 상품 상세 정보 확인 (가격, 재고, 배송 정보)
   ↓
3. 장바구니에 상품 추가
   ↓
4. 장바구니 조회 및 수량 조정
   ↓
5. 주문 확정 (다음 단계)
```

---

## 📦 API 목록

### 카탈로그 & 장바구니 (7개)
1. 유통업체 카탈로그 조회
2. 상품 상세 정보 조회
3. 장바구니에 상품 추가
4. 장바구니 조회
5. 장바구니 아이템 수량 변경
6. 장바구니 아이템 삭제
7. 장바구니 비우기

### 주문 관리 (5개)
8. 주문 생성 (장바구니 → 주문)
9. 내 주문 목록 조회
10. 주문 상세 조회
11. 유통업체별 주문 목록 조회
12. 주문 취소

---

### 1. 유통업체 카탈로그 조회

**엔드포인트:**
```
GET http://10.0.2.2:8080/api/catalog/distributor/{distributorId}
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}
```

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/catalog/distributor/distributor1');
final response = await http.get(
  url,
  headers: {'Authorization': 'Bearer $storeToken'},
);

if (response.statusCode == 200) {
  final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
  final products = data.map((json) => Product.fromJson(json)).toList();
}
```

**응답:**
```json
[
  {
    "id": 1,
    "distributorId": "distributor1",
    "productName": "경기미 20kg",
    "category": "쌀/곡물",
    "description": "경기도에서 생산된 고품질 쌀입니다",
    "unitPrice": 48000,
    "unit": "포",
    "stockQuantity": 100,
    "origin": "경기도",
    "brand": "농협",
    "imageUrl": "https://example.com/rice.jpg",
    "isAvailable": true,
    "minOrderQuantity": 1,
    "maxOrderQuantity": 50,
    "certifications": "친환경인증"
  }
]
```

---

### 2. 상품 상세 정보 조회 (가격, 재고, 배송 정보 포함)

**엔드포인트:**
```
GET http://10.0.2.2:8080/api/catalog/products/{productId}/detail
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}
```

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/catalog/products/1/detail');
final response = await http.get(
  url,
  headers: {'Authorization': 'Bearer $storeToken'},
);

if (response.statusCode == 200) {
  final detail = ProductDetail.fromJson(
    jsonDecode(utf8.decode(response.bodyBytes))
  );
}
```

**응답:**
```json
{
  "id": 1,
  "distributorId": "distributor1",
  "distributorName": "신선식자재 유통",
  "productName": "경기미 20kg",
  "category": "쌀/곡물",
  "unitPrice": 48000,
  "unit": "포",
  "priceInfo": "1포당 48,000원",
  "stockQuantity": 100,
  "stockStatus": "재고 충분",
  "isAvailable": true,
  "orderLimitInfo": "최소 1포 ~ 최대 50포",
  "deliveryInfo": {
    "deliveryType": "익일배송",
    "deliveryFee": 3000,
    "deliveryFeeInfo": "배송비 3,000원 (50,000원 이상 무료)",
    "estimatedDeliveryInfo": "익일 배송"
  }
}
```

---

### 3. 장바구니에 상품 추가

**엔드포인트:**
```
POST http://10.0.2.2:8080/api/cart/add
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}
Content-Type: application/json
```

**요청 Body:**
```json
{
  "productId": 1,
  "quantity": 5
}
```

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/cart/add');
final response = await http.post(
  url,
  headers: {
    'Authorization': 'Bearer $storeToken',
    'Content-Type': 'application/json',
  },
  body: jsonEncode({
    'productId': 1,
    'quantity': 5,
  }),
);

if (response.statusCode == 200) {
  final cart = OrderCart.fromJson(
    jsonDecode(utf8.decode(response.bodyBytes))
  );
  // 장바구니 업데이트
}
```

**응답:**
```json
{
  "id": 1,
  "storeId": "test_store",
  "distributorId": "distributor1",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "경기미 20kg",
      "unitPrice": 48000,
      "unit": "포",
      "quantity": 5,
      "subtotal": 240000,
      "imageUrl": "https://example.com/rice.jpg"
    }
  ],
  "totalAmount": 240000,
  "totalQuantity": 5
}
```

---

### 4. 장바구니 조회

**엔드포인트:**
```
GET http://10.0.2.2:8080/api/cart/{distributorId}
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}
```

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/cart/distributor1');
final response = await http.get(
  url,
  headers: {'Authorization': 'Bearer $storeToken'},
);

if (response.statusCode == 200) {
  final cart = OrderCart.fromJson(
    jsonDecode(utf8.decode(response.bodyBytes))
  );
}
```

**응답:** API 3과 동일

---

### 5. 장바구니 아이템 수량 변경

**엔드포인트:**
```
PUT http://10.0.2.2:8080/api/cart/{distributorId}/items/{itemId}?quantity=10
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}
```

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/cart/distributor1/items/1?quantity=10');
final response = await http.put(
  url,
  headers: {'Authorization': 'Bearer $storeToken'},
);

if (response.statusCode == 200) {
  final cart = OrderCart.fromJson(
    jsonDecode(utf8.decode(response.bodyBytes))
  );
}
```

**응답:** 업데이트된 장바구니 (API 3과 동일 형식)

---

### 6. 장바구니 아이템 삭제

**엔드포인트:**
```
DELETE http://10.0.2.2:8080/api/cart/{distributorId}/items/{itemId}
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}
```

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/cart/distributor1/items/1');
final response = await http.delete(
  url,
  headers: {'Authorization': 'Bearer $storeToken'},
);
```

**응답:** 업데이트된 장바구니

---

### 7. 장바구니 비우기

**엔드포인트:**
```
DELETE http://10.0.2.2:8080/api/cart/{distributorId}
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}
```

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/cart/distributor1');
final response = await http.delete(
  url,
  headers: {'Authorization': 'Bearer $storeToken'},
);
```

**응답:**
```
장바구니가 비워졌습니다.
```

---

### 8. 주문 생성 (장바구니 → 주문)

**엔드포인트:**
```
POST http://10.0.2.2:8080/api/orders/create
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

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/orders/create');
final response = await http.post(
  url,
  headers: {
    'Authorization': 'Bearer $storeToken',
    'Content-Type': 'application/json',
  },
  body: jsonEncode({
    'distributorId': 'distributor1',
    'deliveryAddress': '서울시 강남구 테헤란로 123',
    'deliveryPhone': '010-1234-5678',
    'deliveryRequest': '문 앞에 놓아주세요',
  }),
);

if (response.statusCode == 200) {
  final order = Order.fromJson(
    jsonDecode(utf8.decode(response.bodyBytes))
  );
  // 주문 완료 화면으로 이동
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

**주문 상태:**
- `PENDING`: 주문대기
- `CONFIRMED`: 주문확정
- `PREPARING`: 상품준비중
- `SHIPPED`: 배송중
- `DELIVERED`: 배송완료
- `CANCELLED`: 주문취소

---

### 9. 내 주문 목록 조회

**엔드포인트:**
```
GET http://10.0.2.2:8080/api/orders/my
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}
```

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/orders/my');
final response = await http.get(
  url,
  headers: {'Authorization': 'Bearer $storeToken'},
);

if (response.statusCode == 200) {
  final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
  final orders = data.map((json) => Order.fromJson(json)).toList();
}
```

**응답:** 주문 목록 (최신순)

---

### 10. 주문 상세 조회

**엔드포인트:**
```
GET http://10.0.2.2:8080/api/orders/{orderId}
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}
```

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/orders/1');
final response = await http.get(
  url,
  headers: {'Authorization': 'Bearer $storeToken'},
);

if (response.statusCode == 200) {
  final order = Order.fromJson(
    jsonDecode(utf8.decode(response.bodyBytes))
  );
}
```

**응답:** API 8과 동일

---

### 11. 유통업체별 주문 목록 조회

**엔드포인트:**
```
GET http://10.0.2.2:8080/api/orders/my/distributor/{distributorId}
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}
```

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/orders/my/distributor/distributor1');
final response = await http.get(
  url,
  headers: {'Authorization': 'Bearer $storeToken'},
);

if (response.statusCode == 200) {
  final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
  final orders = data.map((json) => Order.fromJson(json)).toList();
}
```

**응답:** 해당 유통업체 주문 목록

---

### 12. 주문 취소

**엔드포인트:**
```
POST http://10.0.2.2:8080/api/orders/{orderId}/cancel
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}
Content-Type: application/json
```

**요청 Body (선택):**
```json
{
  "reason": "상품이 필요 없어졌습니다"
}
```

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/orders/1/cancel');
final response = await http.post(
  url,
  headers: {
    'Authorization': 'Bearer $storeToken',
    'Content-Type': 'application/json',
  },
  body: jsonEncode({
    'reason': '상품이 필요 없어졌습니다',
  }),
);

if (response.statusCode == 200) {
  final order = Order.fromJson(
    jsonDecode(utf8.decode(response.bodyBytes))
  );
  // 주문 취소 완료
}
```

**응답:** 취소된 주문 정보 (status: CANCELLED)

**주의사항:**
- 배송 중(`SHIPPED`) 또는 배송 완료(`DELIVERED`) 상태에서는 취소 불가
- 취소 시 재고가 자동으로 복구됨

---

## 💾 Dart 모델

### Product (상품)
```dart
class Product {
  final int id;
  final String distributorId;
  final String productName;
  final String category;
  final String? description;
  final int unitPrice;
  final String unit;
  final int? stockQuantity;
  final String? origin;
  final String? brand;
  final String? imageUrl;
  final bool isAvailable;
  final int? minOrderQuantity;
  final int? maxOrderQuantity;
  final String? certifications;

  Product({
    required this.id,
    required this.distributorId,
    required this.productName,
    required this.category,
    this.description,
    required this.unitPrice,
    required this.unit,
    this.stockQuantity,
    this.origin,
    this.brand,
    this.imageUrl,
    required this.isAvailable,
    this.minOrderQuantity,
    this.maxOrderQuantity,
    this.certifications,
  });

  factory Product.fromJson(Map<String, dynamic> json) {
    return Product(
      id: json['id'],
      distributorId: json['distributorId'],
      productName: json['productName'],
      category: json['category'],
      description: json['description'],
      unitPrice: json['unitPrice'],
      unit: json['unit'],
      stockQuantity: json['stockQuantity'],
      origin: json['origin'],
      brand: json['brand'],
      imageUrl: json['imageUrl'],
      isAvailable: json['isAvailable'],
      minOrderQuantity: json['minOrderQuantity'],
      maxOrderQuantity: json['maxOrderQuantity'],
      certifications: json['certifications'],
    );
  }
}
```

### OrderCart (장바구니)
```dart
class OrderCart {
  final int id;
  final String storeId;
  final String distributorId;
  final List<OrderCartItem> items;
  final int totalAmount;
  final int totalQuantity;

  OrderCart({
    required this.id,
    required this.storeId,
    required this.distributorId,
    required this.items,
    required this.totalAmount,
    required this.totalQuantity,
  });

  factory OrderCart.fromJson(Map<String, dynamic> json) {
    return OrderCart(
      id: json['id'],
      storeId: json['storeId'],
      distributorId: json['distributorId'],
      items: (json['items'] as List)
          .map((item) => OrderCartItem.fromJson(item))
          .toList(),
      totalAmount: json['totalAmount'],
      totalQuantity: json['totalQuantity'],
    );
  }
}
```

### OrderCartItem (장바구니 아이템)
```dart
class OrderCartItem {
  final int id;
  final int productId;
  final String productName;
  final int unitPrice;
  final String unit;
  final int quantity;
  final int subtotal;
  final String? imageUrl;

  OrderCartItem({
    required this.id,
    required this.productId,
    required this.productName,
    required this.unitPrice,
    required this.unit,
    required this.quantity,
    required this.subtotal,
    this.imageUrl,
  });

  factory OrderCartItem.fromJson(Map<String, dynamic> json) {
    return OrderCartItem(
      id: json['id'],
      productId: json['productId'],
      productName: json['productName'],
      unitPrice: json['unitPrice'],
      unit: json['unit'],
      quantity: json['quantity'],
      subtotal: json['subtotal'],
      imageUrl: json['imageUrl'],
    );
  }
}
```

### Order (주문)
```dart
class Order {
  final int id;
  final String storeId;
  final String distributorId;
  final String orderNumber;
  final List<OrderItem> items;
  final int totalAmount;
  final int totalQuantity;
  final String status;
  final String statusDescription;
  final String deliveryAddress;
  final String deliveryPhone;
  final String? deliveryRequest;
  final DateTime orderedAt;
  final DateTime? confirmedAt;
  final DateTime? shippedAt;
  final DateTime? deliveredAt;

  Order({
    required this.id,
    required this.storeId,
    required this.distributorId,
    required this.orderNumber,
    required this.items,
    required this.totalAmount,
    required this.totalQuantity,
    required this.status,
    required this.statusDescription,
    required this.deliveryAddress,
    required this.deliveryPhone,
    this.deliveryRequest,
    required this.orderedAt,
    this.confirmedAt,
    this.shippedAt,
    this.deliveredAt,
  });

  factory Order.fromJson(Map<String, dynamic> json) {
    return Order(
      id: json['id'],
      storeId: json['storeId'],
      distributorId: json['distributorId'],
      orderNumber: json['orderNumber'],
      items: (json['items'] as List)
          .map((item) => OrderItem.fromJson(item))
          .toList(),
      totalAmount: json['totalAmount'],
      totalQuantity: json['totalQuantity'],
      status: json['status'],
      statusDescription: json['statusDescription'],
      deliveryAddress: json['deliveryAddress'],
      deliveryPhone: json['deliveryPhone'],
      deliveryRequest: json['deliveryRequest'],
      orderedAt: DateTime.parse(json['orderedAt']),
      confirmedAt: json['confirmedAt'] != null 
          ? DateTime.parse(json['confirmedAt']) 
          : null,
      shippedAt: json['shippedAt'] != null 
          ? DateTime.parse(json['shippedAt']) 
          : null,
      deliveredAt: json['deliveredAt'] != null 
          ? DateTime.parse(json['deliveredAt']) 
          : null,
    );
  }
}
```

### OrderItem (주문 아이템)
```dart
class OrderItem {
  final int id;
  final int productId;
  final String productName;
  final int unitPrice;
  final String unit;
  final int quantity;
  final int subtotal;
  final String? imageUrl;

  OrderItem({
    required this.id,
    required this.productId,
    required this.productName,
    required this.unitPrice,
    required this.unit,
    required this.quantity,
    required this.subtotal,
    this.imageUrl,
  });

  factory OrderItem.fromJson(Map<String, dynamic> json) {
    return OrderItem(
      id: json['id'],
      productId: json['productId'],
      productName: json['productName'],
      unitPrice: json['unitPrice'],
      unit: json['unit'],
      quantity: json['quantity'],
      subtotal: json['subtotal'],
      imageUrl: json['imageUrl'],
    );
  }
}
```

---

## 🎨 UI 구현 가이드

### 1. 카탈로그 화면 (상품 목록)
```
┌─────────────────────────────────────┐
│  신선식자재 유통 카탈로그            │
├─────────────────────────────────────┤
│  [전체] [쌀/곡물] [채소] [육류]     │
├─────────────────────────────────────┤
│  ┌───────────────────────────────┐  │
│  │ [이미지]  경기미 20kg         │  │
│  │           48,000원/포         │  │
│  │           재고: 100포         │  │
│  │           [장바구니 담기]     │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │ [이미지]  국산 양파           │  │
│  │           3,000원/kg          │  │
│  │           재고: 450kg         │  │
│  │           [장바구니 담기]     │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

### 2. 상품 상세 화면
```
┌─────────────────────────────────────┐
│  [← 뒤로]        경기미 20kg        │
├─────────────────────────────────────┤
│  [상품 이미지]                      │
│                                     │
│  💰 가격 정보                       │
│  1포당 48,000원                     │
│  재고: 100포 (재고 충분)            │
│                                     │
│  📦 배송 정보                       │
│  익일 배송                          │
│  배송비 3,000원 (50,000원 이상 무료)│
│                                     │
│  📝 상품 정보                       │
│  원산지: 경기도                     │
│  브랜드: 농협                       │
│  인증: 친환경인증                   │
│                                     │
│  수량: [−] 5 [+]                   │
│  [장바구니 담기 (240,000원)]       │
└─────────────────────────────────────┘
```

### 3. 장바구니 화면
```
┌─────────────────────────────────────┐
│  장바구니 (신선식자재 유통)         │
├─────────────────────────────────────┤
│  ┌───────────────────────────────┐  │
│  │ [이미지] 경기미 20kg          │  │
│  │          48,000원 x 10포      │  │
│  │          = 480,000원          │  │
│  │          [−] 10 [+]  [삭제]  │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │ [이미지] 국산 양파            │  │
│  │          3,000원 x 10kg       │  │
│  │          = 30,000원           │  │
│  │          [−] 10 [+]  [삭제]  │  │
│  └───────────────────────────────┘  │
├─────────────────────────────────────┤
│  총 수량: 20개                      │
│  총 금액: 510,000원                 │
│  [주문하기]                         │
└─────────────────────────────────────┘
```

### 4. 주문 확인 화면
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
│  주소: [입력]                       │
│  전화번호: [입력]                   │
│  요청사항: [입력]                   │
│                                     │
│  [주문하기]                         │
└─────────────────────────────────────┘
```

### 5. 주문 완료 화면
```
┌─────────────────────────────────────┐
│  ✅ 주문이 완료되었습니다!          │
├─────────────────────────────────────┤
│  주문번호: ORD-20231126-143025-456  │
│  주문일시: 2023-11-26 14:30         │
│                                     │
│  📦 주문 상품                       │
│  경기미 20kg x 10포                │
│  국산 양파 x 10kg                  │
│                                     │
│  💰 결제 금액: 510,000원            │
│  🚚 배송 상태: 주문대기             │
│                                     │
│  [주문 상세 보기]                   │
│  [주문 목록으로]                    │
└─────────────────────────────────────┘
```

### 6. 주문 목록 화면
```
┌─────────────────────────────────────┐
│  내 주문 내역                       │
├─────────────────────────────────────┤
│  ┌───────────────────────────────┐  │
│  │ 2023-11-26 14:30              │  │
│  │ ORD-20231126-143025-456       │  │
│  │ 신선식자재 유통                │  │
│  │ 경기미 20kg 외 1건            │  │
│  │ 510,000원                     │  │
│  │ [주문대기]                    │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │ 2023-11-25 10:15              │  │
│  │ ORD-20231125-101520-789       │  │
│  │ 신선식자재 유통                │  │
│  │ 국산 양파 외 2건              │  │
│  │ 150,000원                     │  │
│  │ [배송중]                      │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

### 7. 주문 상세 화면
```
┌─────────────────────────────────────┐
│  [← 뒤로]    주문 상세              │
├─────────────────────────────────────┤
│  주문번호: ORD-20231126-143025-456  │
│  주문일시: 2023-11-26 14:30         │
│  주문상태: [주문대기]               │
│                                     │
│  📦 주문 상품                       │
│  ┌───────────────────────────────┐  │
│  │ [이미지] 경기미 20kg          │  │
│  │          48,000원 x 10포      │  │
│  │          = 480,000원          │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │ [이미지] 국산 양파            │  │
│  │          3,000원 x 10kg       │  │
│  │          = 30,000원           │  │
│  └───────────────────────────────┘  │
│                                     │
│  💰 결제 정보                       │
│  상품 금액: 510,000원               │
│  배송비: 무료                       │
│  총 결제 금액: 510,000원            │
│                                     │
│  🚚 배송 정보                       │
│  주소: 서울시 강남구 테헤란로 123   │
│  전화번호: 010-1234-5678            │
│  요청사항: 문 앞에 놓아주세요       │
│                                     │
│  [주문 취소]                        │
└─────────────────────────────────────┘
```

---

## 🔄 사용 시나리오

### 시나리오 1: 상품 주문 전체 흐름
```dart
// 1. 카탈로그 조회
final products = await catalogService.getDistributorCatalog('distributor1');

// 2. 상품 선택 및 상세 정보 확인
final detail = await catalogService.getProductDetail(products[0].id);

// 3. 장바구니에 추가
await cartService.addToCart(
  productId: detail.id,
  quantity: 10,
);

// 4. 장바구니 조회
final cart = await cartService.getCart('distributor1');

// 5. 주문 생성
final order = await orderService.createOrder(
  distributorId: 'distributor1',
  deliveryAddress: '서울시 강남구 테헤란로 123',
  deliveryPhone: '010-1234-5678',
  deliveryRequest: '문 앞에 놓아주세요',
);

// 6. 주문 완료 화면으로 이동
Navigator.push(
  context,
  MaterialPageRoute(
    builder: (context) => OrderCompletePage(order: order),
  ),
);
```

### 시나리오 2: 주문 내역 조회 및 취소
```dart
// 1. 내 주문 목록 조회
final orders = await orderService.getMyOrders();

// 2. 주문 상세 조회
final orderDetail = await orderService.getOrderDetail(orders[0].id);

// 3. 주문 취소 (필요시)
if (orderDetail.status == 'PENDING' || orderDetail.status == 'CONFIRMED') {
  await orderService.cancelOrder(
    orderId: orderDetail.id,
    reason: '상품이 필요 없어졌습니다',
  );
}
```

---

## 📱 완성된 서비스 클래스

### CatalogService (카탈로그)
```dart
class CatalogService {
  final String baseUrl = 'http://10.0.2.2:8080';
  final String token;

  CatalogService({required this.token});

  // 카탈로그 조회
  Future<List<Product>> getDistributorCatalog(String distributorId) async {
    final url = Uri.parse('$baseUrl/api/catalog/distributor/$distributorId');
    final response = await http.get(
      url,
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((json) => Product.fromJson(json)).toList();
    } else {
      throw Exception('카탈로그 조회 실패');
    }
  }

  // 상품 상세 조회
  Future<ProductDetail> getProductDetail(int productId) async {
    final url = Uri.parse('$baseUrl/api/catalog/products/$productId/detail');
    final response = await http.get(
      url,
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      return ProductDetail.fromJson(
        jsonDecode(utf8.decode(response.bodyBytes))
      );
    } else {
      throw Exception('상품 상세 조회 실패');
    }
  }
}
```

### CartService (장바구니)
```dart
class CartService {
  final String baseUrl = 'http://10.0.2.2:8080';
  final String token;

  CartService({required this.token});

  // 장바구니에 추가
  Future<OrderCart> addToCart({
    required int productId,
    required int quantity,
  }) async {
    final url = Uri.parse('$baseUrl/api/cart/add');
    final response = await http.post(
      url,
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'productId': productId,
        'quantity': quantity,
      }),
    );

    if (response.statusCode == 200) {
      return OrderCart.fromJson(
        jsonDecode(utf8.decode(response.bodyBytes))
      );
    } else {
      throw Exception('장바구니 추가 실패');
    }
  }

  // 장바구니 조회
  Future<OrderCart> getCart(String distributorId) async {
    final url = Uri.parse('$baseUrl/api/cart/$distributorId');
    final response = await http.get(
      url,
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      return OrderCart.fromJson(
        jsonDecode(utf8.decode(response.bodyBytes))
      );
    } else {
      throw Exception('장바구니 조회 실패');
    }
  }

  // 수량 변경
  Future<OrderCart> updateItemQuantity({
    required String distributorId,
    required int itemId,
    required int quantity,
  }) async {
    final url = Uri.parse('$baseUrl/api/cart/$distributorId/items/$itemId?quantity=$quantity');
    final response = await http.put(
      url,
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      return OrderCart.fromJson(
        jsonDecode(utf8.decode(response.bodyBytes))
      );
    } else {
      throw Exception('수량 변경 실패');
    }
  }

  // 아이템 삭제
  Future<OrderCart> removeItem({
    required String distributorId,
    required int itemId,
  }) async {
    final url = Uri.parse('$baseUrl/api/cart/$distributorId/items/$itemId');
    final response = await http.delete(
      url,
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      return OrderCart.fromJson(
        jsonDecode(utf8.decode(response.bodyBytes))
      );
    } else {
      throw Exception('아이템 삭제 실패');
    }
  }

  // 장바구니 비우기
  Future<void> clearCart(String distributorId) async {
    final url = Uri.parse('$baseUrl/api/cart/$distributorId');
    final response = await http.delete(
      url,
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode != 200) {
      throw Exception('장바구니 비우기 실패');
    }
  }
}
```

### OrderService (주문)
```dart
class OrderService {
  final String baseUrl = 'http://10.0.2.2:8080';
  final String token;

  OrderService({required this.token});

  // 주문 생성
  Future<Order> createOrder({
    required String distributorId,
    required String deliveryAddress,
    required String deliveryPhone,
    String? deliveryRequest,
  }) async {
    final url = Uri.parse('$baseUrl/api/orders/create');
    final response = await http.post(
      url,
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'distributorId': distributorId,
        'deliveryAddress': deliveryAddress,
        'deliveryPhone': deliveryPhone,
        'deliveryRequest': deliveryRequest,
      }),
    );

    if (response.statusCode == 200) {
      return Order.fromJson(
        jsonDecode(utf8.decode(response.bodyBytes))
      );
    } else {
      throw Exception('주문 생성 실패');
    }
  }

  // 내 주문 목록 조회
  Future<List<Order>> getMyOrders() async {
    final url = Uri.parse('$baseUrl/api/orders/my');
    final response = await http.get(
      url,
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((json) => Order.fromJson(json)).toList();
    } else {
      throw Exception('주문 목록 조회 실패');
    }
  }

  // 주문 상세 조회
  Future<Order> getOrderDetail(int orderId) async {
    final url = Uri.parse('$baseUrl/api/orders/$orderId');
    final response = await http.get(
      url,
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      return Order.fromJson(
        jsonDecode(utf8.decode(response.bodyBytes))
      );
    } else {
      throw Exception('주문 상세 조회 실패');
    }
  }

  // 유통업체별 주문 목록 조회
  Future<List<Order>> getOrdersByDistributor(String distributorId) async {
    final url = Uri.parse('$baseUrl/api/orders/my/distributor/$distributorId');
    final response = await http.get(
      url,
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((json) => Order.fromJson(json)).toList();
    } else {
      throw Exception('주문 목록 조회 실패');
    }
  }

  // 주문 취소
  Future<Order> cancelOrder({
    required int orderId,
    String? reason,
  }) async {
    final url = Uri.parse('$baseUrl/api/orders/$orderId/cancel');
    final response = await http.post(
      url,
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'reason': reason ?? '고객 요청',
      }),
    );

    if (response.statusCode == 200) {
      return Order.fromJson(
        jsonDecode(utf8.decode(response.bodyBytes))
      );
    } else {
      throw Exception('주문 취소 실패');
    }
  }
}
```

---

## ⚠️ 주의사항

### 1. UTF-8 인코딩 필수
```dart
jsonDecode(utf8.decode(response.bodyBytes))
```

### 2. 재고 확인
- 장바구니 추가 전 재고 확인
- `stockQuantity`와 `isAvailable` 체크
- 주문 생성 시 자동으로 재고 차감

### 3. 주문 수량 제한
- `minOrderQuantity` ~ `maxOrderQuantity` 범위 확인
- UI에서 수량 입력 제한

### 4. 주문 상태 관리
- `PENDING`: 주문대기 (취소 가능)
- `CONFIRMED`: 주문확정 (취소 가능)
- `PREPARING`: 상품준비중 (취소 가능)
- `SHIPPED`: 배송중 (취소 불가)
- `DELIVERED`: 배송완료 (취소 불가)
- `CANCELLED`: 주문취소

### 5. 에러 처리
- 400: 재고 부족, 주문 수량 초과, 장바구니 비어있음
- 404: 상품 없음, 주문 없음
- 403: 접근 권한 없음

### 6. 주문 취소 시 재고 복구
- 주문 취소 시 자동으로 재고가 복구됨
- 배송 중/완료 상태에서는 취소 불가

---

## 🎯 구현 체크리스트

### 1. 카탈로그 화면
- [ ] 유통업체 카탈로그 조회
- [ ] 카테고리별 필터링
- [ ] 상품 검색
- [ ] 상품 카드 UI (이미지, 이름, 가격, 재고)
- [ ] 장바구니 담기 버튼

### 2. 상품 상세 화면
- [ ] 상품 이미지 표시
- [ ] 가격 정보 표시
- [ ] 재고 상태 표시
- [ ] 배송 정보 표시
- [ ] 수량 선택 (+ / - 버튼)
- [ ] 장바구니 담기

### 3. 장바구니 화면
- [ ] 장바구니 아이템 목록
- [ ] 아이템별 수량 조정
- [ ] 아이템 삭제
- [ ] 총 수량 / 총 금액 표시
- [ ] 주문하기 버튼

### 4. 주문 확인 화면
- [ ] 주문 상품 목록 표시
- [ ] 결제 금액 표시
- [ ] 배송 정보 입력 (주소, 전화번호, 요청사항)
- [ ] 주문하기 버튼

### 5. 주문 완료 화면
- [ ] 주문번호 표시
- [ ] 주문 상품 요약
- [ ] 결제 금액 표시
- [ ] 주문 상세 보기 버튼
- [ ] 주문 목록으로 이동 버튼

### 6. 주문 목록 화면
- [ ] 주문 목록 조회 (최신순)
- [ ] 주문 카드 UI (주문번호, 날짜, 상품, 금액, 상태)
- [ ] 주문 상태별 필터링
- [ ] 주문 상세 보기

### 7. 주문 상세 화면
- [ ] 주문 정보 표시 (주문번호, 날짜, 상태)
- [ ] 주문 상품 목록
- [ ] 결제 정보
- [ ] 배송 정보
- [ ] 주문 취소 버튼 (상태에 따라)

---

## 📝 테스트 가이드

### 백엔드 서버 실행
```bash
./gradlew bootRun
```

### 전체 흐름 테스트
```bash
./test-order-flow.sh
```

이 스크립트는 다음을 자동으로 테스트합니다:
1. 카탈로그 조회
2. 상품 상세 조회
3. 장바구니에 상품 추가
4. 장바구니 조회
5. 주문 생성
6. 주문 상세 조회
7. 주문 목록 조회
