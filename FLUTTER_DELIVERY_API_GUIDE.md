# 배송 관리 API 가이드 (Flutter 개발용)

## 📋 개요

카탈로그 주문 시스템의 배송 관리 기능입니다. 가게사장님과 유통업자 간의 상품 주문 후 배송 프로세스를 관리합니다.

**Base URL**: `http://localhost:8080` (개발 서버 URL로 변경 필요)

---

## 🔐 인증

모든 API는 JWT 토큰 기반 인증을 사용합니다.

### 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "store001",
  "password": "password"
}
```

**응답**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "store001"
}
```

**이후 모든 요청 헤더에 포함**:
```
Authorization: Bearer {accessToken}
```

---

## 👥 사용자 역할

- **STORE_OWNER** (가게사장님): 주문 생성, 배송 조회
- **DISTRIBUTOR** (유통업자): 배송 관리, 배송 시작/완료

**테스트 계정**:
- 가게사장님: `username: store001, password: password`
- 유통업자: `username: dist001, password: password`

---

## 📦 배송 상태 흐름

```
PENDING (주문대기)
   ↓ 결제 완료
CONFIRMED (주문확정)
   ↓ 유통업자가 배송 정보 생성
PREPARING (상품준비중)
   ↓ 유통업자가 배송 시작
SHIPPED (배송중)
   ↓ 유통업자가 배송 완료
DELIVERED (배송완료)
```

---

## 🛒 1. 주문 생성 (가게사장님)

### 1-1. 주문 생성
```http
POST /api/catalog-orders/create
Authorization: Bearer {token}
Content-Type: application/json

{
  "distributorId": "dist001",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요",
  "desiredDeliveryDate": "2025-11-30T10:00:00"
}
```

**응답**:
```json
{
  "id": 1,
  "storeId": "store001",
  "distributorId": "dist001",
  "orderNumber": "ORD-20251128-143022-456",
  "items": [],
  "totalAmount": 50000,
  "totalQuantity": 10,
  "status": "PENDING",
  "statusDescription": "주문대기",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요",
  "desiredDeliveryDate": "2025-11-30T10:00:00",
  "orderedAt": "2025-11-28T14:30:22"
}
```

### 1-2. 주문 확정 (결제 완료 후)
```http
POST /api/catalog-orders/{orderId}/confirm
Authorization: Bearer {token}
```

**응답**: 주문 정보 (status가 "CONFIRMED"로 변경됨)

---

## 🚚 2. 배송 관리 (유통업자)

### 2-1. 유통업자 주문 목록 조회
```http
GET /api/catalog-orders/distributor
Authorization: Bearer {token}
```

**응답**:
```json
[
  {
    "id": 1,
    "orderNumber": "ORD-20251128-143022-456",
    "storeId": "store001",
    "distributorId": "dist001",
    "totalAmount": 50000,
    "totalQuantity": 10,
    "status": "CONFIRMED",
    "statusDescription": "주문확정",
    "orderedAt": "2025-11-28T14:30:22",
    "confirmedAt": "2025-11-28T14:35:00"
  }
]
```

### 2-2. 배송 정보 생성
주문 확정 후 유통업자가 배송 정보를 생성합니다.

```http
POST /api/deliveries/order/{orderId}
Authorization: Bearer {token}
```

**응답**:
```json
{
  "id": 1,
  "orderId": 1,
  "orderNumber": "ORD-20251128-143022-456",
  "storeId": "store001",
  "distributorId": "dist001",
  "status": "PREPARING",
  "statusDescription": "상품준비중",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요",
  "totalAmount": 50000,
  "preparedAt": "2025-11-28T15:00:00",
  "createdAt": "2025-11-28T15:00:00"
}
```

### 2-3. 배송 시작
상품 준비 완료 후 배송을 시작합니다. **택배 배송** 또는 **직접 배송** 중 선택할 수 있습니다.

#### 옵션 1: 택배 배송
```http
POST /api/deliveries/order/{orderId}/ship
Authorization: Bearer {token}
Content-Type: application/json

{
  "deliveryType": "COURIER",
  "trackingNumber": "1234567890",
  "courierCompany": "CJ대한통운",
  "courierPhone": "1588-1255",
  "estimatedDeliveryDate": "2025-11-30T18:00:00",
  "deliveryNotes": "신선식품 주의"
}
```

**필수 필드**:
- `deliveryType`: "COURIER" (택배배송)
- `trackingNumber`: 송장번호
- `courierCompany`: 배송사 (CJ대한통운, 로젠택배, 한진택배, 우체국택배)

**선택 필드**:
- `courierPhone`: 배송사 연락처
- `estimatedDeliveryDate`: 예상 배송일
- `deliveryNotes`: 배송 메모

#### 옵션 2: 직접 배송
```http
POST /api/deliveries/order/{orderId}/ship
Authorization: Bearer {token}
Content-Type: application/json

{
  "deliveryType": "DIRECT",
  "driverName": "김배송",
  "driverPhone": "010-9876-5432",
  "vehicleNumber": "12가3456",
  "estimatedDeliveryDate": "2025-11-30T18:00:00",
  "deliveryNotes": "신선식품 주의"
}
```

**필수 필드**:
- `deliveryType`: "DIRECT" (직접배송)
- `driverName`: 배송 기사 이름
- `driverPhone`: 배송 기사 연락처

**선택 필드**:
- `vehicleNumber`: 차량 번호
- `estimatedDeliveryDate`: 예상 배송일
- `deliveryNotes`: 배송 메모

**응답 (택배 배송)**:
```json
{
  "id": 1,
  "orderId": 1,
  "orderNumber": "ORD-20251128-143022-456",
  "deliveryType": "COURIER",
  "deliveryTypeDescription": "택배배송",
  "trackingNumber": "1234567890",
  "courierCompany": "CJ대한통운",
  "courierPhone": "1588-1255",
  "status": "SHIPPED",
  "statusDescription": "배송중",
  "shippedAt": "2025-11-28T16:00:00",
  "estimatedDeliveryDate": "2025-11-30T18:00:00",
  "deliveryNotes": "신선식품 주의"
}
```

**응답 (직접 배송)**:
```json
{
  "id": 1,
  "orderId": 1,
  "orderNumber": "ORD-20251128-143022-456",
  "deliveryType": "DIRECT",
  "deliveryTypeDescription": "직접배송",
  "driverName": "김배송",
  "driverPhone": "010-9876-5432",
  "vehicleNumber": "12가3456",
  "status": "SHIPPED",
  "statusDescription": "배송중",
  "shippedAt": "2025-11-28-16:00:00",
  "estimatedDeliveryDate": "2025-11-30T18:00:00",
  "deliveryNotes": "신선식품 주의"
}
```

### 2-4. 배송 완료
```http
POST /api/deliveries/order/{orderId}/complete
Authorization: Bearer {token}
```

**응답**:
```json
{
  "id": 1,
  "orderId": 1,
  "orderNumber": "ORD-20251128-143022-456",
  "status": "DELIVERED",
  "statusDescription": "배송완료",
  "deliveredAt": "2025-11-30T17:30:00"
}
```

### 2-5. 유통업자 배송 목록 조회
```http
GET /api/deliveries/distributor
Authorization: Bearer {token}
```

**응답**: 배송 정보 배열

---

## 📱 3. 배송 조회 (가게사장님)

### 3-1. 내 주문 목록 조회
```http
GET /api/catalog-orders/my
Authorization: Bearer {token}
```

**응답**: 주문 정보 배열

### 3-2. 배송 정보 조회 (주문 ID로)
```http
GET /api/deliveries/order/{orderId}
Authorization: Bearer {token}
```

**응답**:
```json
{
  "id": 1,
  "orderId": 1,
  "orderNumber": "ORD-20251128-143022-456",
  "storeId": "store001",
  "distributorId": "dist001",
  "trackingNumber": "1234567890",
  "courierCompany": "CJ대한통운",
  "courierPhone": "1588-1255",
  "status": "SHIPPED",
  "statusDescription": "배송중",
  "preparedAt": "2025-11-28T15:00:00",
  "shippedAt": "2025-11-28T16:00:00",
  "estimatedDeliveryDate": "2025-11-30T18:00:00",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요",
  "totalAmount": 50000,
  "createdAt": "2025-11-28T15:00:00"
}
```

### 3-3. 매장 배송 목록 조회
```http
GET /api/deliveries/store
Authorization: Bearer {token}
```

**응답**: 배송 정보 배열

---

## 📊 4. 데이터 모델

### DeliveryType (배송 방식)
```dart
enum DeliveryType {
  DIRECT,   // 직접배송 (유통업자가 직접 배송)
  COURIER   // 택배배송 (택배사를 통한 배송)
}
```

### DeliveryStatus (배송 상태)
```dart
enum DeliveryStatus {
  PREPARING,  // 상품준비중
  SHIPPED,    // 배송중
  DELIVERED   // 배송완료
}
```

### OrderStatus (주문 상태)
```dart
enum OrderStatus {
  PENDING,    // 주문대기
  CONFIRMED,  // 주문확정
  PREPARING,  // 상품준비중
  SHIPPED,    // 배송중
  DELIVERED,  // 배송완료
  CANCELLED   // 주문취소
}
```

### DeliveryInfo (배송 정보)
```dart
class DeliveryInfo {
  final int id;
  final int orderId;
  final String orderNumber;
  final String storeId;
  final String distributorId;
  final DeliveryType? deliveryType;
  final String? deliveryTypeDescription;
  
  // 택배 배송 정보
  final String? trackingNumber;
  final String? courierCompany;
  final String? courierPhone;
  
  // 직접 배송 정보
  final String? driverName;
  final String? driverPhone;
  final String? vehicleNumber;
  
  final DeliveryStatus status;
  final String statusDescription;
  final DateTime? preparedAt;
  final DateTime? shippedAt;
  final DateTime? deliveredAt;
  final DateTime? estimatedDeliveryDate;
  final String? deliveryAddress;
  final String? deliveryPhone;
  final String? deliveryRequest;
  final String? deliveryNotes;
  final int totalAmount;
  final DateTime createdAt;
}
```

---

## 🎨 5. UI 구현 가이드

### 5-1. 가게사장님 화면

**배송 목록 화면**:
- 주문 목록 표시 (주문번호, 금액, 상태)
- 배송 상태별 색상 구분
  - PREPARING: 파란색
  - SHIPPED: 초록색
  - DELIVERED: 보라색
- 송장번호 클릭 시 배송사 홈페이지 연결 (선택)

**배송 상세 화면**:
- 주문 정보 (주문번호, 금액, 주문일시)
- 배송지 정보 (주소, 연락처, 요청사항)
- 배송 방식 표시 (택배배송 / 직접배송)
- **택배 배송인 경우**: 송장번호, 배송사, 예상 도착일
- **직접 배송인 경우**: 배송 기사 이름, 연락처, 차량번호, 예상 도착일
- 배송 상태 타임라인

### 5-2. 유통업자 화면

**대시보드**:
- 통계 카드
  - 주문확정 (배송 대기) 건수
  - 상품준비중 건수
  - 배송중 건수
  - 배송완료 건수

**주문 목록**:
- 상태별 필터 탭 (전체, 주문확정, 상품준비중, 배송중, 배송완료)
- 각 주문 카드에 액션 버튼
  - CONFIRMED: "배송 정보 생성" 버튼
  - PREPARING: "배송 시작" 버튼
  - SHIPPED: "배송 완료" 버튼

**배송 시작 모달/화면**:
- **배송 방식 선택** (필수)
  - 📦 택배 배송
  - 🚚 직접 배송

**택배 배송 선택 시**:
- 송장번호 입력 (필수)
- 배송사 선택 (필수)
  - CJ대한통운
  - 로젠택배
  - 한진택배
  - 우체국택배
- 배송사 연락처 입력 (선택)
- 예상 배송일 선택 (선택)
- 배송 메모 입력 (선택)

**직접 배송 선택 시**:
- 배송 기사 이름 입력 (필수)
- 배송 기사 연락처 입력 (필수)
- 차량 번호 입력 (선택)
- 예상 배송일 선택 (선택)
- 배송 메모 입력 (선택)

---

## 🔄 6. 권장 플로우

### 가게사장님 앱
```
1. 로그인
2. 상품 주문 생성
3. 결제 완료 후 주문 확정
4. 배송 목록에서 배송 상태 확인
5. 송장번호로 배송 추적
```

### 유통업자 앱
```
1. 로그인
2. 대시보드에서 주문확정 건수 확인
3. 주문 목록에서 "주문확정" 필터 선택
4. 주문 선택 → "배송 정보 생성" 클릭
5. 상품 준비 완료 후 "배송 시작" 클릭
6. 송장번호, 배송사 정보 입력
7. 배송 완료 후 "배송 완료" 클릭
```

---

## ⚠️ 에러 처리

### 일반적인 에러 응답
```json
{
  "timestamp": "2025-11-28T10:37:12.798+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "배송 정보를 찾을 수 없습니다.",
  "path": "/api/deliveries/order/999"
}
```

### 주요 에러 케이스
- `400 Bad Request`: 잘못된 요청 (필수 필드 누락, 잘못된 상태 전이)
- `401 Unauthorized`: 인증 실패 (토큰 없음 또는 만료)
- `403 Forbidden`: 권한 없음 (다른 사용자의 주문 접근)
- `404 Not Found`: 리소스 없음 (존재하지 않는 주문/배송)

---

## 🧪 7. 테스트 시나리오

### 시나리오 1: 정상 배송 프로세스
```
1. 가게사장님 로그인
2. 주문 생성 (POST /api/catalog-orders/create)
3. 주문 확정 (POST /api/catalog-orders/{orderId}/confirm)
4. 유통업자 로그인
5. 배송 정보 생성 (POST /api/deliveries/order/{orderId})
6. 배송 시작 (POST /api/deliveries/order/{orderId}/ship)
7. 가게사장님이 배송 정보 조회 (GET /api/deliveries/order/{orderId})
8. 유통업자가 배송 완료 (POST /api/deliveries/order/{orderId}/complete)
```

### 시나리오 2: 배송 조회
```
1. 가게사장님 로그인
2. 내 주문 목록 조회 (GET /api/catalog-orders/my)
3. 배송 목록 조회 (GET /api/deliveries/store)
4. 특정 배송 상세 조회 (GET /api/deliveries/order/{orderId})
```

---

## 📝 8. 개발 시 주의사항

1. **토큰 관리**: JWT 토큰을 안전하게 저장하고 만료 시 재로그인 처리
2. **상태 전이**: 올바른 순서로만 상태 변경 가능 (CONFIRMED → PREPARING → SHIPPED → DELIVERED)
3. **권한 체크**: 가게사장님은 자신의 주문만, 유통업자는 자신에게 온 주문만 접근 가능
4. **날짜 형식**: ISO 8601 형식 사용 (`2025-11-30T18:00:00`)
5. **에러 처리**: 모든 API 호출에 대해 적절한 에러 처리 구현

---

## 🔗 9. 추가 리소스

- **API 테스트 스크립트**: `test-delivery-api.sh`
- **상세 문서**: `README.md` 파일의 "카탈로그 주문 배송 관리 시스템" 섹션
- **백엔드 코드**: `src/main/java/com/example/payflow/catalog/`

---

## 💬 10. 문의사항

API 관련 문의사항이나 버그 발견 시 백엔드 팀에 연락 주세요.

**Happy Coding! 🚀**
