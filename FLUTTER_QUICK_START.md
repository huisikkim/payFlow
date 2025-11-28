# 배송 관리 API - 빠른 시작 가이드

## 🚀 5분 안에 시작하기
---

### 3️⃣ 배송 조회
```dart
GET /api/deliveries/store  // 전체 목록
GET /api/deliveries/order/{orderId}  // 상세 정보
```

---

## 🚚 유통업자 앱 (4개 API만 사용)

### 1️⃣ 주문 목록 조회
```dart
GET /api/catalog-orders/distributor
```

### 2️⃣ 배송 정보 생성
```dart
POST /api/deliveries/order/{orderId}
```

### 3️⃣ 배송 시작

**택배 배송**:
```dart
POST /api/deliveries/order/{orderId}/ship
{
  "deliveryType": "COURIER",
  "trackingNumber": "1234567890",
  "courierCompany": "CJ대한통운",
  "estimatedDeliveryDate": "2025-11-30T18:00:00"
}
```

**직접 배송**:
```dart
POST /api/deliveries/order/{orderId}/ship
{
  "deliveryType": "DIRECT",
  "driverName": "김배송",
  "driverPhone": "010-9876-5432",
  "vehicleNumber": "12가3456",
  "estimatedDeliveryDate": "2025-11-30T18:00:00"
}
```

### 4️⃣ 배송 완료
```dart
POST /api/deliveries/order/{orderId}/complete
```

---

## 🎨 UI 체크리스트

### 가게사장님 화면
- [ ] 배송 목록 화면 (상태별 색상 구분)
- [ ] 배송 상세 화면 (송장번호, 배송사, 예상 도착일)

### 유통업자 화면
- [ ] 로그인 화면
- [ ] 대시보드 (통계 카드 4개)
- [ ] 주문 목록 (상태별 필터 탭)
- [ ] 배송 시작 모달
  - [ ] 배송 방식 선택 (택배/직접)
  - [ ] 택배: 송장번호, 배송사 입력
  - [ ] 직접: 기사 이름, 연락처, 차량번호 입력

---

## 📊 배송 상태
```
PREPARING  → 상품준비중 (파란색)
SHIPPED    → 배송중 (초록색)
DELIVERED  → 배송완료 (보라색)
```

---

## 🔑 테스트 계정
- 가게사장님: `store001 / password`
- 유통업자: `dist001 / password`

---

## 📞 문의
상세 문서: `FLUTTER_DELIVERY_API_GUIDE.md` 참고
