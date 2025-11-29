# 🎯 공동구매 시스템 Flutter API 가이드

## 📋 개요

유통업자가 공동구매 방을 생성하고, 가게들이 참여하여 할인 혜택을 받는 시스템입니다.

**Base URL**: `http://localhost:8080/api/group-buying`

---

## 🔐 인증

모든 API는 JWT 토큰 인증이 필요할 수 있습니다. (현재는 테스트를 위해 비활성화 가능)

```dart
final headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Bearer $token', // 필요시
};
```

---

## 📡 API 목록

### 1️⃣ 공동구매 방 관리 (유통업자)

#### 1-1. 공동구매 방 생성

**POST** `/rooms`

유통업자가 새로운 공동구매 방을 생성합니다.

**Request Body:**
```json
{
  "roomTitle": "🔥 김치 대박 세일! 20% 할인",
  "distributorId": "DIST001",
  "distributorName": "신선식품 유통",
  "productId": 1,
  "discountRate": 20.00,
  "availableStock": 500,
  "targetQuantity": 300,
  "minOrderPerStore": 10,
  "maxOrderPerStore": 100,
  "minParticipants": 5,
  "maxParticipants": 20,
  "region": "서울 강남구,서초구",
  "deliveryFee": 50000,
  "deliveryFeeType": "SHARED",
  "expectedDeliveryDate": "2023-12-01T09:00:00",
  "durationHours": 24,
  "description": "신선한 김치를 특가로 제공합니다!",
  "specialNote": "당일 배송 보장",
  "featured": true
}
```

**필수 필드:**
- `roomTitle`: 방 제목
- `distributorId`: 유통업자 ID
- `distributorName`: 유통업자명
- `productId`: 상품 ID
- `discountRate`: 할인율 (%)
- `availableStock`: 준비한 재고
- `targetQuantity`: 목표 수량
- `minOrderPerStore`: 가게당 최소 주문 수량
- `minParticipants`: 최소 참여자 수
- `region`: 대상 지역
- `deliveryFee`: 배송비
- `deliveryFeeType`: 배송비 타입 (FREE/FIXED/SHARED)
- `durationHours`: 진행 시간 (시간)

**선택 필드:**
- `maxOrderPerStore`: 가게당 최대 주문 수량
- `maxParticipants`: 최대 참여자 수
- `expectedDeliveryDate`: 예상 배송일
- `description`: 방 설명
- `specialNote`: 특이사항
- `featured`: 추천 여부

**Response:**
```json
{
  "id": 1,
  "roomId": "GBR-20231129143022-1234",
  "roomTitle": "🔥 김치 대박 세일! 20% 할인",
  "status": "WAITING",
  "distributorId": "DIST001",
  "productName": "포기김치",
  "originalPrice": 10000,
  "discountedPrice": 8000,
  "savingsPerUnit": 2000,
  "targetQuantity": 300,
  "currentQuantity": 0,
  "achievementRate": 0.00,
  "currentParticipants": 0,
  "deliveryFeePerStore": null
}
```


#### 1-2. 방 오픈

**POST** `/rooms/{roomId}/open?distributorId={distributorId}`

생성된 방을 오픈하여 가게들이 참여할 수 있게 합니다.

**Path Parameters:**
- `roomId`: 방 ID (예: GBR-20231129143022-1234)

**Query Parameters:**
- `distributorId`: 유통업자 ID

**Response:**
```json
{
  "roomId": "GBR-20231129143022-1234",
  "status": "OPEN",
  "currentParticipants": 0,
  "currentQuantity": 0,
  "openedAt": "2023-11-29T14:30:22"
}
```

#### 1-3. 방 수동 마감

**POST** `/rooms/{roomId}/close?distributorId={distributorId}`

방을 수동으로 마감합니다.

**Response:**
```json
{
  "roomId": "GBR-20231129143022-1234",
  "status": "CLOSED_SUCCESS",
  "closedAt": "2023-11-29T20:30:22"
}
```

#### 1-4. 방 취소

**POST** `/rooms/{roomId}/cancel?distributorId={distributorId}&reason={reason}`

방을 취소합니다.

**Query Parameters:**
- `distributorId`: 유통업자 ID
- `reason`: 취소 사유

#### 1-5. 유통업자의 방 목록 조회

**GET** `/rooms/distributor/{distributorId}`

유통업자가 생성한 모든 방을 조회합니다.

**Response:**
```json
[
  {
    "roomId": "GBR-20231129143022-1234",
    "roomTitle": "🔥 김치 대박 세일! 20% 할인",
    "status": "OPEN",
    "achievementRate": 75.00,
    "currentParticipants": 8
  }
]
```

---

### 2️⃣ 공동구매 방 조회 (가게)

#### 2-1. 오픈 중인 방 목록 조회

**GET** `/rooms/open`

현재 오픈 중인 모든 방을 조회합니다.

**Query Parameters (선택):**
- `region`: 지역 필터 (예: 강남구)
- `category`: 카테고리 필터 (예: 채소)

**Response:**
```json
[
  {
    "roomId": "GBR-20231129143022-1234",
    "roomTitle": "🔥 김치 대박 세일! 20% 할인",
    "productName": "포기김치",
    "category": "채소",
    "originalPrice": 10000,
    "discountedPrice": 8000,
    "discountRate": 20.00,
    "savingsPerUnit": 2000,
    "targetQuantity": 300,
    "currentQuantity": 225,
    "achievementRate": 75.00,
    "currentParticipants": 8,
    "minParticipants": 5,
    "deliveryFeePerStore": 6250,
    "region": "서울 강남구,서초구",
    "deadline": "2023-11-30T14:30:22",
    "remainingMinutes": 1103,
    "status": "OPEN",
    "imageUrl": "https://example.com/kimchi.jpg"
  }
]
```

#### 2-2. 방 상세 조회

**GET** `/rooms/{roomId}`

특정 방의 상세 정보를 조회합니다.

**Response:**
```json
{
  "id": 1,
  "roomId": "GBR-20231129143022-1234",
  "roomTitle": "🔥 김치 대박 세일! 20% 할인",
  "distributorId": "DIST001",
  "distributorName": "신선식품 유통",
  "productId": 1,
  "productName": "포기김치",
  "category": "채소",
  "unit": "kg",
  "origin": "국내산",
  "productDescription": "신선한 김치입니다",
  "imageUrl": "https://example.com/kimchi.jpg",
  "originalPrice": 10000,
  "discountRate": 20.00,
  "discountedPrice": 8000,
  "savingsPerUnit": 2000,
  "availableStock": 500,
  "targetQuantity": 300,
  "currentQuantity": 225,
  "minOrderPerStore": 10,
  "maxOrderPerStore": 100,
  "minParticipants": 5,
  "maxParticipants": 20,
  "currentParticipants": 8,
  "achievementRate": 75.00,
  "stockRemainRate": 55.00,
  "region": "서울 강남구,서초구",
  "deliveryFee": 50000,
  "deliveryFeePerStore": 6250,
  "deliveryFeeType": "SHARED",
  "expectedDeliveryDate": "2023-12-01T09:00:00",
  "startTime": "2023-11-29T14:30:22",
  "deadline": "2023-11-30T14:30:22",
  "durationHours": 24,
  "remainingMinutes": 1103,
  "status": "OPEN",
  "openedAt": "2023-11-29T14:30:22",
  "description": "신선한 김치를 특가로 제공합니다!",
  "specialNote": "당일 배송 보장",
  "featured": true,
  "viewCount": 152,
  "createdAt": "2023-11-29T14:30:22",
  "updatedAt": "2023-11-29T18:45:10"
}
```


#### 2-3. 추천 방 목록 조회

**GET** `/rooms/featured`

추천 방 목록을 조회합니다.

#### 2-4. 마감 임박 방 조회

**GET** `/rooms/deadline-soon`

24시간 이내 마감되는 방을 조회합니다.

---

### 3️⃣ 공동구매 참여 (가게)

#### 3-1. 공동구매 참여

**POST** `/participants/join`

가게가 공동구매에 참여합니다.

**Request Body:**
```json
{
  "roomId": "GBR-20231129143022-1234",
  "storeId": "STORE001",
  "quantity": 30,
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요"
}
```

**필수 필드:**
- `roomId`: 방 ID
- `storeId`: 가게 ID
- `quantity`: 주문 수량

**선택 필드:**
- `deliveryAddress`: 배송 주소 (없으면 가게 주소 사용)
- `deliveryPhone`: 배송 연락처 (없으면 가게 연락처 사용)
- `deliveryRequest`: 배송 요청사항

**Response:**
```json
{
  "id": 1,
  "storeId": "STORE001",
  "storeName": "맛있는 식당",
  "quantity": 30,
  "unitPrice": 8000,
  "totalProductAmount": 240000,
  "deliveryFee": 6250,
  "totalAmount": 246250,
  "savingsAmount": 60000,
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요",
  "status": "JOINED",
  "joinedAt": "2023-11-29T15:30:22"
}
```

#### 3-2. 참여 취소

**POST** `/participants/{participantId}/cancel?storeId={storeId}&reason={reason}`

참여를 취소합니다. (방이 오픈 중일 때만 가능)

**Path Parameters:**
- `participantId`: 참여 ID

**Query Parameters:**
- `storeId`: 가게 ID
- `reason`: 취소 사유

#### 3-3. 가게의 참여 내역 조회

**GET** `/participants/store/{storeId}`

가게가 참여한 모든 공동구매 내역을 조회합니다.

**Response:**
```json
[
  {
    "id": 1,
    "storeId": "STORE001",
    "storeName": "맛있는 식당",
    "quantity": 30,
    "totalAmount": 246250,
    "savingsAmount": 60000,
    "status": "JOINED",
    "joinedAt": "2023-11-29T15:30:22"
  }
]
```

#### 3-4. 방의 참여자 목록 조회

**GET** `/participants/room/{roomId}`

특정 방의 참여자 목록을 조회합니다.

**Response:**
```json
[
  {
    "id": 1,
    "storeId": "STORE001",
    "storeName": "맛있는 식당",
    "storeRegion": "서울 강남구",
    "quantity": 30,
    "totalAmount": 246250,
    "status": "JOINED",
    "joinedAt": "2023-11-29T15:30:22"
  }
]
```

---

### 4️⃣ 통계 API

#### 4-1. 유통업자 통계

**GET** `/statistics/distributor/{distributorId}`

**Response:**
```json
{
  "distributorId": "DIST001",
  "totalRooms": 10,
  "openRooms": 2,
  "successRooms": 7,
  "failedRooms": 1,
  "successRate": 70.0,
  "totalRevenue": 5000000,
  "totalParticipants": 45
}
```

#### 4-2. 가게 통계

**GET** `/statistics/store/{storeId}`

**Response:**
```json
{
  "storeId": "STORE001",
  "totalParticipations": 15,
  "activeParticipations": 2,
  "completedOrders": 12,
  "totalSavings": 300000,
  "totalSpent": 1200000
}
```

#### 4-3. 전체 시스템 통계

**GET** `/statistics/system`

**Response:**
```json
{
  "totalRooms": 50,
  "openRooms": 5,
  "successRooms": 40,
  "successRate": 80.0,
  "totalParticipants": 200,
  "totalRevenue": 25000000,
  "totalSavings": 5000000
}
```

---

## 📊 데이터 모델

### RoomStatus (방 상태)
- `WAITING`: 대기 중 (생성됨, 아직 오픈 전)
- `OPEN`: 오픈 (참여 가능)
- `CLOSED_SUCCESS`: 마감 성공 (목표 달성)
- `CLOSED_FAILED`: 마감 실패 (목표 미달)
- `ORDER_CREATED`: 주문 생성 완료
- `COMPLETED`: 완료 (배송 완료)
- `CANCELLED`: 취소됨

### ParticipantStatus (참여자 상태)
- `JOINED`: 참여 완료 (방이 아직 진행 중)
- `CONFIRMED`: 확정됨 (방 마감 성공)
- `ORDER_CREATED`: 주문 생성됨
- `DELIVERED`: 배송 완료
- `CANCELLED`: 취소됨

### DeliveryFeeType (배송비 타입)
- `FREE`: 무료 배송
- `FIXED`: 고정 배송비 (가게당)
- `SHARED`: 분담 배송비 (총 배송비를 참여자 수로 나눔)
