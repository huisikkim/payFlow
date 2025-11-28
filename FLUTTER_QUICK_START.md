# 배송 관리 API - 빠른 시작 가이드

## 🚀 5분 안에 시작하기
---

### 3️⃣ 배송 조회
```dart
GET /api/deliveries/store  // 전체 목록
GET /api/deliveries/order/{orderId}  // 상세 정보

// 응답에 리뷰 작성 여부 포함
{
  "id": 1,
  "status": "DELIVERED",
  "hasStoreReview": false,        // 리뷰 작성 여부
  "hasDistributorReview": false
}
```

---

## 🚚 유통업자 앱 (4개 API만 사용)

### 1️⃣ 주문 목록 조회
```dart
GET /api/catalog-orders/distributor

// 응답에 리뷰 작성 여부 포함
{
  "id": 1,
  "status": "DELIVERED",
  "hasStoreReview": false,
  "hasDistributorReview": false  // 리뷰 작성 여부
}
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


---

## ⭐ 리뷰 및 평점 시스템

### 가게사장님 앱 (2개 API 추가)

#### 1️⃣ 리뷰 작성 (유통업자 평가)
```dart
POST /api/reviews/store
{
  "orderId": 1,
  "rating": 5,
  "comment": "배송이 빠르고 상품 품질이 좋습니다!",
  "deliveryQuality": 5,
  "productQuality": 5,
  "serviceQuality": 4
}
```

#### 2️⃣ 내 리뷰 통계 조회
```dart
GET /api/reviews/statistics
```

### 유통업자 앱 (2개 API 추가)

#### 1️⃣ 리뷰 작성 (가게사장님 평가)
```dart
POST /api/reviews/distributor
{
  "orderId": 1,
  "rating": 5,
  "comment": "결제가 빠르고 소통이 원활합니다!",
  "paymentReliability": 5,
  "communicationQuality": 5,
  "orderAccuracy": 4
}
```

#### 2️⃣ 내 리뷰 통계 조회
```dart
GET /api/reviews/statistics
```

---

## 🎨 리뷰 UI 체크리스트

### 가게사장님 화면
- [ ] 주문 목록에서 리뷰 버튼 표시
  - [ ] `status == DELIVERED` AND `hasStoreReview == false` → "리뷰 작성" 버튼
  - [ ] `hasStoreReview == true` → "리뷰 등록 완료" 표시
- [ ] 리뷰 작성 화면 (배송 완료 후)
  - [ ] 별점 입력 (1-5)
  - [ ] 리뷰 내용 입력
  - [ ] 세부 평점 (배송/상품/서비스)
- [ ] 받은 리뷰 화면
  - [ ] 평균 평점 표시
  - [ ] 리뷰 목록
- [ ] 작성한 리뷰 화면

### 유통업자 화면
- [ ] 주문 목록에서 리뷰 버튼 표시
  - [ ] `status == DELIVERED` AND `hasDistributorReview == false` → "리뷰 작성" 버튼
  - [ ] `hasDistributorReview == true` → "리뷰 등록 완료" 표시
- [ ] 리뷰 작성 화면 (배송 완료 후)
  - [ ] 별점 입력 (1-5)
  - [ ] 리뷰 내용 입력
  - [ ] 세부 평점 (결제/소통/주문정확도)
- [ ] 받은 리뷰 화면
  - [ ] 평균 평점 표시
  - [ ] 리뷰 목록
- [ ] 프로필에 평점 배지 표시

---

## 📊 리뷰 제약사항
- ✅ 배송 완료 후에만 작성 가능
- ✅ 한 주문당 한 번만 작성 가능
- ✅ 평점은 1-5 사이
- ✅ `hasStoreReview` 또는 `hasDistributorReview`로 중복 작성 방지
