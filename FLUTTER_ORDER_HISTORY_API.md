# 주문 내역 저장 및 이력 관리 API - Flutter 가이드

## 📋 개요

식자재 카탈로그에서 생성된 주문의 내역을 저장하고, 주문 이력을 조회 및 관리하는 기능입니다.

**Base URL**: `http://10.0.2.2:8080` (Android 에뮬레이터)  
**Base URL**: `http://localhost:8080` (iOS 시뮬레이터)

## ⚠️ 중요: API 경로 주의사항

**모든 API 경로는 `/api`로 시작합니다!**

❌ **잘못된 예시:**
```
http://localhost:8080/catalog-orders/create
```

✅ **올바른 예시:**
```
http://localhost:8080/api/catalog-orders/create
```

---

## 🎯 주문 생명주기

```
주문 생성 (PENDING)
    ↓
주문 확정 (CONFIRMED)
    ↓
상품 준비 (PREPARING)
    ↓
배송 시작 (SHIPPED)
    ↓
배송 완료 (DELIVERED)

※ 주문 취소 (CANCELLED) - SHIPPED 이전까지만 가능
```

---

## 📦 주문 상태 (OrderStatus)

| 상태 | 코드 | 설명 | 취소 가능 여부 |
|------|------|------|----------------|
| 주문대기 | `PENDING` | 주문이 생성되었으나 확정되지 않음 | ✅ 가능 |
| 주문확정 | `CONFIRMED` | 유통업체가 주문을 확정함 | ✅ 가능 |
| 상품준비중 | `PREPARING` | 상품을 준비 중 | ✅ 가능 |
| 배송중 | `SHIPPED` | 배송이 시작됨 | ❌ 불가 |
| 배송완료 | `DELIVERED` | 배송이 완료됨 | ❌ 불가 |
| 주문취소 | `CANCELLED` | 주문이 취소됨 | - |

---

## 📦 API 목록


### 1. 주문 생성 (장바구니 → 주문)
### 2. 내 주문 목록 조회 (전체)
### 3. 유통업체별 주문 목록 조회
### 4. 주문 상세 조회
### 5. 주문 취소

---

## 🔌 API 상세

### 1. 주문 생성 (장바구니 → 주문)

장바구니에 담긴 상품들을 주문으로 전환합니다.

**엔드포인트:**
```
POST http://10.0.2.2:8080/api/catalog-orders/create
```

**중요: 헤더 필수 사항**
```
Authorization: Bearer {매장 로그인 토큰}  ← 필수!
Content-Type: application/json
```

**참고:**
- Authorization 헤더는 필수입니다 (매장 로그인 후 받은 토큰 사용)
- 토큰에서 자동으로 매장 ID를 추출하므로 X-Store-Id 헤더는 불필요합니다

**요청 Body:**
```json
{
  "distributorId": "distributor1",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요",
  "desiredDeliveryDate": "2025-11-27T19:55:20.606927"
}
```

**참고:**
- `desiredDeliveryDate`는 선택사항입니다
- ISO 8601 형식으로 전송: `YYYY-MM-DDTHH:mm:ss` 또는 `YYYY-MM-DDTHH:mm:ss.SSSSSS`

**요청 파라미터:**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| distributorId | String | ✅ | 유통업체 ID |
| deliveryAddress | String | ✅ | 배송 주소 |
| deliveryPhone | String | ✅ | 배송 연락처 |
| deliveryRequest | String | ❌ | 배송 요청사항 |
| desiredDeliveryDate | DateTime | ❌ | 희망 배송일 (ISO 8601 형식) |

**응답 (200 OK):**
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
  "desiredDeliveryDate": "2025-11-27T19:55:20",
  "orderedAt": "2023-11-26T14:30:25",
  "confirmedAt": null,
  "shippedAt": null,
  "deliveredAt": null
}
```

**에러 응답:**
- `400 Bad Request`: 
  - 장바구니가 비어있음 (해당 매장ID + 유통업체ID 조합의 장바구니가 없음)
  - 장바구니에 상품이 없음
  - 재고 부족
- `401 Unauthorized`: 인증 실패
- `404 Not Found`: 상품을 찾을 수 없음

**중요 주의사항:**
- 주문 생성 전에 반드시 해당 유통업체의 장바구니에 상품이 있어야 합니다
- 로그인한 매장의 장바구니만 주문으로 전환할 수 있습니다
- `distributorId`는 장바구니에 담긴 상품의 유통업체ID와 일치해야 합니다

**주요 동작:**
1. 장바구니 조회 및 검증
2. 재고 확인 및 차감
3. 주문 생성 (상태: PENDING)
4. 주문번호 자동 생성 (ORD-YYYYMMDD-HHMMSS-XXX)
5. 장바구니 비우기

---

### 2. 내 주문 목록 조회 (전체)

매장의 모든 주문 내역을 최신순으로 조회합니다.

**엔드포인트:**
```
GET http://10.0.2.2:8080/api/catalog-orders/my
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}  ← 필수!
```

**요청 파라미터:** 없음

**응답 (200 OK):**
```json
[
  {
    "id": 2,
    "storeId": "test_store",
    "distributorId": "distributor1",
    "orderNumber": "ORD-20231126-143025-456",
    "items": [
      {
        "id": 3,
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
    "status": "SHIPPED",
    "statusDescription": "배송중",
    "deliveryAddress": "서울시 강남구 테헤란로 123",
    "deliveryPhone": "010-1234-5678",
    "deliveryRequest": "문 앞에 놓아주세요",
    "orderedAt": "2023-11-26T14:30:25",
    "confirmedAt": "2023-11-26T15:00:00",
    "shippedAt": "2023-11-27T09:00:00",
    "deliveredAt": null
  },
  {
    "id": 1,
    "storeId": "test_store",
    "distributorId": "distributor2",
    "orderNumber": "ORD-20231125-101520-789",
    "items": [
      {
        "id": 1,
        "productId": 5,
        "productName": "국산 양파",
        "unitPrice": 3000,
        "unit": "kg",
        "quantity": 20,
        "subtotal": 60000,
        "imageUrl": "https://example.com/onion.jpg"
      }
    ],
    "totalAmount": 60000,
    "totalQuantity": 20,
    "status": "DELIVERED",
    "statusDescription": "배송완료",
    "deliveryAddress": "서울시 강남구 테헤란로 123",
    "deliveryPhone": "010-1234-5678",
    "deliveryRequest": null,
    "orderedAt": "2023-11-25T10:15:20",
    "confirmedAt": "2023-11-25T11:00:00",
    "shippedAt": "2023-11-25T14:00:00",
    "deliveredAt": "2023-11-25T18:30:00"
  }
]
```

**정렬:** 주문일시 기준 최신순 (orderedAt DESC)

---

### 3. 유통업체별 주문 목록 조회

특정 유통업체에 대한 주문 내역만 조회합니다.

**엔드포인트:**
```
GET http://10.0.2.2:8080/api/catalog-orders/my/distributor/{distributorId}
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}  ← 필수!
```

**경로 파라미터:**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| distributorId | String | ✅ | 유통업체 ID (예: distributor1) |

**요청 예시:**
```
GET http://10.0.2.2:8080/api/catalog-orders/my/distributor/distributor1
```

**응답 (200 OK):**
```json
[
  {
    "id": 2,
    "storeId": "test_store",
    "distributorId": "distributor1",
    "orderNumber": "ORD-20231126-143025-456",
    "items": [...],
    "totalAmount": 480000,
    "totalQuantity": 10,
    "status": "SHIPPED",
    "statusDescription": "배송중",
    "orderedAt": "2023-11-26T14:30:25",
    ...
  }
]
```

**정렬:** 주문일시 기준 최신순

---

### 4. 주문 상세 조회

특정 주문의 상세 정보를 조회합니다.

**엔드포인트:**
```
GET http://10.0.2.2:8080/api/catalog-orders/{orderId}
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}  ← 필수!
```

**경로 파라미터:**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| orderId | Long | ✅ | 주문 ID |

**요청 예시:**
```
GET http://10.0.2.2:8080/api/catalog-orders/1
```

**응답 (200 OK):**
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
    },
    {
      "id": 2,
      "productId": 5,
      "productName": "국산 양파",
      "unitPrice": 3000,
      "unit": "kg",
      "quantity": 10,
      "subtotal": 30000,
      "imageUrl": "https://example.com/onion.jpg"
    }
  ],
  "totalAmount": 510000,
  "totalQuantity": 20,
  "status": "CONFIRMED",
  "statusDescription": "주문확정",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요",
  "orderedAt": "2023-11-26T14:30:25",
  "confirmedAt": "2023-11-26T15:00:00",
  "shippedAt": null,
  "deliveredAt": null
}
```

**에러 응답:**
- `404 Not Found`: 주문을 찾을 수 없음
- `403 Forbidden`: 접근 권한 없음 (다른 매장의 주문)

---

### 5. 주문 취소

주문을 취소하고 재고를 복구합니다.

**엔드포인트:**
```
POST http://10.0.2.2:8080/api/catalog-orders/{orderId}/cancel
```

**헤더:**
```
Authorization: Bearer {매장 로그인 토큰}  ← 필수!
Content-Type: application/json
```

**경로 파라미터:**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| orderId | Long | ✅ | 주문 ID |

**요청 Body:**
```json
{
  "reason": "상품이 필요 없어졌습니다"
}
```

**요청 파라미터:**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| reason | String | ❌ | 취소 사유 (기본값: "고객 요청") |

**요청 예시:**
```
POST http://10.0.2.2:8080/api/catalog-orders/1/cancel
```

**응답 (200 OK):**
```json
{
  "id": 1,
  "storeId": "test_store",
  "distributorId": "distributor1",
  "orderNumber": "ORD-20231126-143025-456",
  "items": [...],
  "totalAmount": 480000,
  "totalQuantity": 10,
  "status": "CANCELLED",
  "statusDescription": "주문취소",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요",
  "orderedAt": "2023-11-26T14:30:25",
  "confirmedAt": "2023-11-26T15:00:00",
  "shippedAt": null,
  "deliveredAt": null
}
```

**에러 응답:**
- `400 Bad Request`: 
  - 배송 중이거나 완료된 주문은 취소 불가
  - 이미 취소된 주문
- `404 Not Found`: 주문을 찾을 수 없음
- `403 Forbidden`: 접근 권한 없음

**주요 동작:**
1. 주문 상태 확인 (SHIPPED, DELIVERED는 취소 불가)
2. 재고 복구 (주문 수량만큼 재고 증가)
3. 주문 상태를 CANCELLED로 변경
4. 취소 시간(cancelledAt) 및 취소 사유 기록

---

## 📊 응답 데이터 구조

### Order (주문)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 주문 ID |
| storeId | String | 매장 ID |
| distributorId | String | 유통업체 ID |
| orderNumber | String | 주문번호 (ORD-YYYYMMDD-HHMMSS-XXX) |
| items | Array | 주문 상품 목록 |
| totalAmount | Long | 총 주문 금액 (원) |
| totalQuantity | Integer | 총 주문 수량 |
| status | String | 주문 상태 코드 |
| statusDescription | String | 주문 상태 설명 (한글) |
| deliveryAddress | String | 배송 주소 |
| deliveryPhone | String | 배송 연락처 |
| deliveryRequest | String | 배송 요청사항 (nullable) |
| desiredDeliveryDate | DateTime | 희망 배송일 (nullable) |
| orderedAt | DateTime | 주문 생성 시간 |
| confirmedAt | DateTime | 주문 확정 시간 (nullable) |
| shippedAt | DateTime | 배송 시작 시간 (nullable) |
| deliveredAt | DateTime | 배송 완료 시간 (nullable) |

### OrderItem (주문 상품)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 주문 상품 ID |
| productId | Long | 상품 ID |
| productName | String | 상품명 |
| unitPrice | Long | 단가 (원) |
| unit | String | 단위 (포, kg, 개 등) |
| quantity | Integer | 주문 수량 |
| subtotal | Long | 소계 (단가 × 수량) |
| imageUrl | String | 상품 이미지 URL (nullable) |

---

## 🔄 사용 시나리오

### 시나리오 1: 주문 생성 및 조회

```
1. 장바구니에 상품 추가
   POST /api/cart/add

2. 주문 생성
   POST /api/catalog-orders/create
   → 주문번호: ORD-20231126-143025-456
   → 상태: PENDING (주문대기)

3. 내 주문 목록 조회
   GET /api/catalog-orders/my
   → 최신 주문이 목록 상단에 표시됨

4. 주문 상세 조회
   GET /api/catalog-orders/1
   → 주문 상품, 배송 정보, 주문 상태 확인
```

### 시나리오 2: 주문 상태 추적

```
1. 주문 생성
   상태: PENDING (주문대기)
   orderedAt: 2023-11-26T14:30:25

2. 유통업체가 주문 확정
   상태: CONFIRMED (주문확정)
   confirmedAt: 2023-11-26T15:00:00

3. 상품 준비 시작
   상태: PREPARING (상품준비중)

4. 배송 시작
   상태: SHIPPED (배송중)
   shippedAt: 2023-11-27T09:00:00

5. 배송 완료
   상태: DELIVERED (배송완료)
   deliveredAt: 2023-11-27T18:30:00
```

### 시나리오 3: 주문 취소

```
1. 주문 목록에서 취소할 주문 선택
   GET /api/catalog-orders/my

2. 주문 상태 확인
   - PENDING, CONFIRMED, PREPARING → 취소 가능
   - SHIPPED, DELIVERED → 취소 불가

3. 주문 취소 요청
   POST /api/catalog-orders/1/cancel
   {
     "reason": "상품이 필요 없어졌습니다"
   }

4. 취소 완료
   상태: CANCELLED (주문취소)
   재고 자동 복구
```

### 시나리오 4: 유통업체별 주문 이력 조회

```
1. 특정 유통업체의 주문 내역만 조회
   GET /api/catalog-orders/my/distributor/distributor1

2. 해당 유통업체와의 거래 이력 확인
   - 총 주문 횟수
   - 총 주문 금액
   - 최근 주문 상태
```

---

## ⚠️ 주의사항

### 1. API 경로 - 반드시 /api 포함!
**모든 API 경로는 `/api`로 시작해야 합니다!**

❌ **잘못된 경로 (404 에러 발생):**
```
http://localhost:8080/catalog-orders/create
http://localhost:8080/catalog-orders/my
```

✅ **올바른 경로:**
```
http://localhost:8080/api/catalog-orders/create
http://localhost:8080/api/catalog-orders/my
```

### 2. 필수 헤더
모든 API 호출 시 다음 헤더가 **반드시** 필요합니다:
```
X-Store-Id: {매장ID}  ← 필수!
```

**중요:**
- `X-Store-Id` 헤더가 없으면 400 또는 500 에러 발생
- Authorization 헤더는 현재 개발 환경에서 선택사항 (프로덕션에서는 필수)

### 3. 주문 취소 제한
- `SHIPPED` (배송중) 상태부터는 취소 불가
- `DELIVERED` (배송완료) 상태는 취소 불가
- 취소 가능 상태: `PENDING`, `CONFIRMED`, `PREPARING`

### 4. 재고 관리
- 주문 생성 시: 재고 자동 차감
- 주문 취소 시: 재고 자동 복구
- 재고 부족 시 주문 생성 실패 (400 에러)

### 5. 주문번호 형식
- 형식: `ORD-YYYYMMDD-HHMMSS-XXX`
- 예시: `ORD-20231126-143025-456`
- 자동 생성되며 중복되지 않음

### 6. 날짜/시간 형식
- ISO 8601 형식: `YYYY-MM-DDTHH:mm:ss`
- 예시: `2023-11-26T14:30:25`
- UTC 기준 또는 서버 로컬 시간

### 7. 접근 권한
- 매장은 자신의 주문만 조회/취소 가능
- 다른 매장의 주문 접근 시 403 에러

### 7. 에러 처리
```
400 Bad Request: 잘못된 요청 (재고 부족, 취소 불가 등)
401 Unauthorized: 인증 실패
403 Forbidden: 접근 권한 없음
404 Not Found: 리소스를 찾을 수 없음
```

---

## 🧪 테스트 시나리오

### 1. 정상 주문 생성 테스트
```bash
# 1. 매장 로그인
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "test_store", "password": "password123"}')
TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')

# 2. 장바구니에 상품 추가
curl -X POST http://localhost:8080/api/cart/add \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 10}'

# 3. 주문 생성
curl -X POST http://localhost:8080/api/catalog-orders/create \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "distributorId": "distributor1",
    "deliveryAddress": "서울시 강남구 테헤란로 123",
    "deliveryPhone": "010-1234-5678",
    "deliveryRequest": "문 앞에 놓아주세요",
    "desiredDeliveryDate": "2025-11-27T19:55:20"
  }'
```

### 2. 주문 목록 조회 테스트
```bash
# 전체 주문 목록
curl -X GET http://localhost:8080/api/catalog-orders/my \
  -H "Authorization: Bearer $TOKEN"

# 유통업체별 주문 목록
curl -X GET http://localhost:8080/api/catalog-orders/my/distributor/distributor1 \
  -H "Authorization: Bearer $TOKEN"
```

### 3. 주문 상세 조회 테스트
```bash
curl -X GET http://localhost:8080/api/catalog-orders/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 4. 주문 취소 테스트
```bash
curl -X POST http://localhost:8080/api/catalog-orders/1/cancel \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "상품이 필요 없어졌습니다"}'
```

---

## 📈 데이터베이스 스키마

### distributor_orders 테이블
```sql
CREATE TABLE distributor_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id VARCHAR(255) NOT NULL,
    distributor_id VARCHAR(255) NOT NULL,
    order_number VARCHAR(255) NOT NULL UNIQUE,
    total_amount BIGINT NOT NULL,
    total_quantity INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    delivery_address VARCHAR(500),
    delivery_phone VARCHAR(50),
    delivery_request VARCHAR(500),
    ordered_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(500),
    INDEX idx_store_id (store_id),
    INDEX idx_order_number (order_number),
    INDEX idx_ordered_at (ordered_at)
);
```

### distributor_order_items 테이블
```sql
CREATE TABLE distributor_order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price BIGINT NOT NULL,
    unit VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    subtotal BIGINT NOT NULL,
    image_url VARCHAR(500),
    FOREIGN KEY (order_id) REFERENCES distributor_orders(id) ON DELETE CASCADE
);
```

---

## 🎯 구현 체크리스트

### Flutter 화면 구현
- [ ] 주문 목록 화면 (전체)
- [ ] 주문 목록 화면 (유통업체별)
- [ ] 주문 상세 화면
- [ ] 주문 상태 표시 (색상, 아이콘)
- [ ] 주문 취소 기능
- [ ] 주문 이력 타임라인 표시

### API 연동
- [ ] 주문 생성 API 호출
- [ ] 주문 목록 조회 API 호출
- [ ] 주문 상세 조회 API 호출
- [ ] 주문 취소 API 호출
- [ ] 에러 처리 및 사용자 피드백

### 데이터 관리
- [ ] 주문 상태별 필터링
- [ ] 주문 검색 기능
- [ ] 주문 정렬 (최신순, 금액순)
- [ ] 로컬 캐싱 (선택사항)

---

## 📞 문의 및 지원

API 관련 문의사항이나 오류 발생 시:
- 백엔드 개발팀에 문의
- 로그 확인: `boot-run.log`
- 테스트 스크립트: `test-catalog-api-fixed.sh`

---

**작성일**: 2023-11-26  
**버전**: 1.0  
**담당**: Backend Team
