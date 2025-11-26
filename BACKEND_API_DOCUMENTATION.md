# 백엔드 API 문서

## 🚀 서버 정보
- **Base URL**: `http://localhost:8080`
- **프로덕션 URL**: `http://YOUR_SERVER_IP:8080`

---

## 📋 API 목록

### 1. 인증 API

#### 1.1 회원가입
```
POST /api/auth/signup
Content-Type: application/json
```

**요청 Body:**
```json
{
  "username": "store_owner1",
  "password": "password123",
  "email": "store1@example.com",
  "userType": "STORE_OWNER",  // STORE_OWNER 또는 DISTRIBUTOR
  "businessNumber": "123-45-67890",
  "businessName": "맛있는 식당",
  "ownerName": "김사장",
  "phoneNumber": "010-1234-5678",
  "address": "서울시 강남구"
}
```

**응답:**
```
200 OK
"User registered successfully"
```

**에러:**
- `400`: Username already exists
- `400`: Email already exists
- `400`: Business number already exists

---

#### 1.2 로그인
```
POST /api/auth/login
Content-Type: application/json
```

**요청 Body:**
```json
{
  "username": "store_owner1",
  "password": "password123"
}
```

**응답:**
```json
{
  "accessToken": "eyJhbGci...",
  "tokenType": "Bearer",
  "username": "store_owner1",
  "userId": 1,
  "userType": "STORE_OWNER",
  "businessName": "맛있는 식당"
}
```

---

### 2. 사용자 프로필 API

#### 2.1 내 프로필 조회
```
GET /api/user/profile
Authorization: Bearer {token}
```

**응답:**
```json
{
  "userId": 1,
  "username": "store_owner1",
  "email": "store1@example.com",
  "userType": "STORE_OWNER",
  "businessNumber": "123-45-67890",
  "businessName": "맛있는 식당",
  "ownerName": "김사장",
  "phoneNumber": "010-1234-5678",
  "address": "서울시 강남구",
  "roles": ["ROLE_STORE_OWNER"],
  "enabled": true,
  "createdAt": "2025-11-25T20:48:13"
}
```

---

### 3. 매장 정보 API

#### 3.1 매장 정보 등록/수정
```
POST /api/store/info
Authorization: Bearer {token}
Content-Type: application/json
```

**권한**: ROLE_STORE_OWNER

**요청 Body:**
```json
{
  "storeName": "맛있는 한식당",
  "businessType": "한식",
  "region": "서울 강남구",
  "mainProducts": "쌀/곡물,채소,육류",
  "description": "정성을 다하는 한식당입니다",
  "employeeCount": 5,
  "operatingHours": "09:00-22:00",
  "phoneNumber": "010-1234-5678",
  "address": "서울시 강남구 테헤란로 123"
}
```

**응답:**
```json
{
  "id": 10,
  "storeId": "store_owner1",
  "storeName": "맛있는 한식당",
  "ownerName": "store_owner1",
  "phoneNumber": "010-1234-5678",
  "address": "서울시 강남구 테헤란로 123",
  "businessType": "한식",
  "region": "서울 강남구",
  "mainProducts": "쌀/곡물,채소,육류",
  "description": "정성을 다하는 한식당입니다",
  "employeeCount": 5,
  "operatingHours": "09:00-22:00",
  "isActive": true,
  "createdAt": "2025-11-25T21:01:34",
  "updatedAt": "2025-11-25T21:01:34"
}
```

---

#### 3.2 내 매장 정보 조회
```
GET /api/store/info
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

**응답**: 3.1과 동일

**에러:**
- `404`: 매장 정보 없음

---

#### 3.3 특정 매장 정보 조회
```
GET /api/store/info/{storeId}
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER, ROLE_DISTRIBUTOR

**응답**: 3.1과 동일

---

#### 3.4 매장 활성화/비활성화
```
PUT /api/store/status?activate=true
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

**파라미터:**
- `activate`: true (활성화) / false (비활성화)

**응답:**
```
200 OK
"매장이 활성화되었습니다."
```

---

### 4. 유통업체 정보 API

#### 4.1 유통업체 정보 등록/수정
```
POST /api/distributor/info
Authorization: Bearer {token}
Content-Type: application/json
```

**권한**: ROLE_DISTRIBUTOR

**요청 Body:**
```json
{
  "distributorName": "신선식자재 유통",
  "supplyProducts": "쌀/곡물,채소,과일,육류,수산물",
  "serviceRegions": "서울,경기,인천",
  "deliveryAvailable": true,
  "deliveryInfo": "배송비 무료 (10만원 이상), 익일 배송",
  "description": "신선한 식자재를 공급하는 전문 유통업체입니다",
  "certifications": "HACCP,ISO22000",
  "minOrderAmount": 100000,
  "operatingHours": "09:00-18:00",
  "phoneNumber": "010-9876-5432",
  "email": "distributor1@example.com",
  "address": "서울시 송파구 올림픽로 456"
}
```

**응답:**
```json
{
  "id": 15,
  "distributorId": "distributor1",
  "distributorName": "신선식자재 유통",
  "phoneNumber": "010-9876-5432",
  "email": "distributor1@example.com",
  "supplyProducts": "쌀/곡물,채소,과일,육류,수산물",
  "serviceRegions": "서울,경기,인천",
  "deliveryAvailable": true,
  "deliveryInfo": "배송비 무료 (10만원 이상), 익일 배송",
  "description": "신선한 식자재를 공급하는 전문 유통업체입니다",
  "certifications": "HACCP,ISO22000",
  "minOrderAmount": 100000,
  "operatingHours": "09:00-18:00",
  "address": "서울시 송파구 올림픽로 456",
  "isActive": true,
  "createdAt": "2025-11-25T21:35:58",
  "updatedAt": "2025-11-25T21:35:58"
}
```

---

#### 4.2 내 유통업체 정보 조회
```
GET /api/distributor/info
Authorization: Bearer {token}
```

**권한**: ROLE_DISTRIBUTOR

**응답**: 4.1과 동일

**에러:**
- `404`: 유통업체 정보 없음

---

#### 4.3 특정 유통업체 정보 조회
```
GET /api/distributor/info/{distributorId}
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER, ROLE_DISTRIBUTOR

**응답**: 4.1과 동일

---

#### 4.4 유통업체 활성화/비활성화
```
PUT /api/distributor/status?activate=true
Authorization: Bearer {token}
```

**권한**: ROLE_DISTRIBUTOR

**파라미터:**
- `activate`: true (활성화) / false (비활성화)

**응답:**
```
200 OK
"유통업체가 활성화되었습니다."
```

---

### 5. 매칭 및 추천 API ⭐

#### 5.1 맞춤 유통업체 추천
```
GET /api/matching/recommend?limit=10
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

**파라미터:**
- `limit` (선택): 추천 개수 (기본값: 10)

**응답:**
```json
[
  {
    "distributorId": "distributor1",
    "distributorName": "신선식자재 유통",
    "totalScore": 87.50,
    "regionScore": 100.00,
    "productScore": 85.71,
    "deliveryScore": 100.00,
    "certificationScore": 85.00,
    "matchReason": "서비스 지역 완벽 일치, 필요 품목 대부분 공급 가능, 배송 서비스 우수, 인증 보유",
    "supplyProducts": "쌀/곡물,채소,과일,육류,수산물",
    "serviceRegions": "서울,경기,인천",
    "deliveryAvailable": true,
    "deliveryInfo": "배송비 무료 (10만원 이상), 익일 배송",
    "certifications": "HACCP,ISO22000",
    "minOrderAmount": 100000,
    "phoneNumber": "010-9876-5432",
    "email": "distributor1@example.com"
  }
]
```

**매칭 점수 계산:**
- 지역 매칭: 40%
- 품목 매칭: 35%
- 배송 서비스: 15%
- 인증 정보: 10%

**에러:**
- `404`: 매장 정보 없음 (매장 정보를 먼저 등록해야 함)
- `200` + `[]`: 활성화된 유통업체 없음

---

#### 5.2 품목별 유통업체 검색
```
GET /api/matching/search/product?keyword=쌀
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

**파라미터:**
- `keyword` (필수): 검색할 품목 (예: "쌀", "채소", "육류")

**응답**: 5.1과 동일 (매칭 점수 포함)

---

#### 5.3 지역별 유통업체 검색
```
GET /api/matching/search/region?keyword=서울
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

**파라미터:**
- `keyword` (필수): 검색할 지역 (예: "서울", "경기", "부산")

**응답**: 5.1과 동일 (매칭 점수 포함)

---

### 6. 유통업체 비교 API ⭐ NEW

**비교 항목:**
- 가격 (최소 주문 금액, 가격대)
- 배송 (배송 가능 여부, 배송 속도, 배송비)
- 서비스 (서비스 지역, 공급 품목)
- 품질 (품질 등급, 신뢰도 점수)
- 인증 (인증 개수, 인증 종류)
- 종합 (매칭 점수, 순위, 강점/약점)

#### 6.1 추천 유통업체 비교 (Top N)
```
GET /api/matching/compare/top?topN=5
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

**파라미터:**
- `topN` (선택): 비교할 유통업체 수 (기본값: 5)

**응답:**
```json
[
  {
    "distributorId": "distributor1",
    "distributorName": "신선식자재 유통",
    "phoneNumber": "010-9876-5432",
    "email": "distributor1@example.com",
    "totalScore": 98.5,
    "regionScore": 100,
    "productScore": 100.0,
    "deliveryScore": 100,
    "certificationScore": 85,
    "minOrderAmount": 100000,
    "priceLevel": "MEDIUM",
    "priceNote": "최소 주문 금액: 100,000원",
    "deliveryAvailable": true,
    "deliveryInfo": "배송비 무료 (10만원 이상), 익일 배송",
    "deliverySpeed": "NEXT_DAY",
    "deliveryFee": 0,
    "deliveryRegions": "서울,경기,인천",
    "serviceRegions": "서울,경기,인천",
    "supplyProducts": "쌀/곡물,채소,과일,육류,수산물",
    "certifications": "HACCP,ISO22000",
    "certificationCount": 2,
    "operatingHours": "09:00-18:00",
    "qualityRating": "EXCELLENT",
    "reliabilityScore": 86.0,
    "description": "신선한 식자재를 공급하는 전문 유통업체",
    "strengths": [
      "서비스 지역 완벽 일치",
      "필요 품목 대부분 공급 가능",
      "배송 서비스 제공",
      "다수 인증 보유"
    ],
    "weaknesses": [],
    "rank": 1,
    "bestCategory": "SERVICE"
  }
]
```

**비교 지표 설명:**
- `priceLevel`: LOW(저렴), MEDIUM(보통), HIGH(비쌈)
- `deliverySpeed`: SAME_DAY(당일), NEXT_DAY(익일), TWO_TO_THREE_DAYS(2-3일), OVER_THREE_DAYS(3일 이상)
- `qualityRating`: EXCELLENT(최상), GOOD(상), AVERAGE(중), BELOW_AVERAGE(하)
- `reliabilityScore`: 신뢰도 점수 (0-100)
- `rank`: 종합 순위
- `bestCategory`: PRICE(가격), DELIVERY(배송), QUALITY(품질), SERVICE(서비스), CERTIFICATION(인증)

---

#### 6.2 특정 유통업체 비교
```
POST /api/matching/compare
Authorization: Bearer {token}
Content-Type: application/json
```

**권한**: ROLE_STORE_OWNER

**요청 Body:**
```json
["distributor1", "distributor2", "distributor3"]
```

**응답:** 6.1과 동일 (선택한 유통업체들의 비교 정보)

---

#### 6.3 카테고리별 최고 유통업체
```
POST /api/matching/compare/best-by-category
Authorization: Bearer {token}
Content-Type: application/json
```

**권한**: ROLE_STORE_OWNER

**요청 Body:**
```json
["distributor1", "distributor2", "distributor3"]
```

**응답:**
```json
{
  "PRICE": {
    "distributorId": "distributor2",
    "distributorName": "저렴한 유통",
    "minOrderAmount": 50000,
    "priceLevel": "LOW",
    ...
  },
  "DELIVERY": {
    "distributorId": "distributor1",
    "distributorName": "빠른 배송",
    "deliverySpeed": "SAME_DAY",
    ...
  },
  "QUALITY": {
    "distributorId": "distributor3",
    "distributorName": "프리미엄 유통",
    "qualityRating": "EXCELLENT",
    ...
  },
  "CERTIFICATION": {
    "distributorId": "distributor1",
    "certificationCount": 3,
    ...
  },
  "OVERALL": {
    "distributorId": "distributor1",
    "totalScore": 98.5,
    ...
  }
}
```

---

### 7. 식자재 카탈로그 API ⭐ NEW

유통업체가 자신의 식자재 목록을 등록하고, 매장이 이를 조회하여 주문할 수 있는 기능입니다.

#### 7.1 상품 등록 (유통업체)
```
POST /api/catalog/products
Authorization: Bearer {token}
Content-Type: application/json
```

**권한**: ROLE_DISTRIBUTOR

**요청 Body:**
```json
{
  "productName": "경기미 20kg",
  "category": "쌀/곡물",
  "description": "경기도에서 생산된 고품질 쌀입니다",
  "unitPrice": 45000,
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
```

**응답:**
```json
{
  "id": 1,
  "distributorId": "distributor1",
  "productName": "경기미 20kg",
  "category": "쌀/곡물",
  "description": "경기도에서 생산된 고품질 쌀입니다",
  "unitPrice": 45000,
  "unit": "포",
  "stockQuantity": 100,
  "origin": "경기도",
  "brand": "농협",
  "imageUrl": "https://example.com/rice.jpg",
  "isAvailable": true,
  "minOrderQuantity": 1,
  "maxOrderQuantity": 50,
  "certifications": "친환경인증",
  "createdAt": "2025-11-26T10:43:24",
  "updatedAt": "2025-11-26T10:43:24"
}
```

---

#### 7.2 상품 수정 (유통업체)
```
PUT /api/catalog/products/{productId}
Authorization: Bearer {token}
Content-Type: application/json
```

**권한**: ROLE_DISTRIBUTOR

**요청 Body:** (수정할 필드만 포함)
```json
{
  "unitPrice": 48000,
  "description": "경기도에서 생산된 고품질 쌀입니다 (가격 인상)"
}
```

---

#### 7.3 상품 삭제 (유통업체)
```
DELETE /api/catalog/products/{productId}
Authorization: Bearer {token}
```

**권한**: ROLE_DISTRIBUTOR

---

#### 7.4 내 카탈로그 조회 (유통업체)
```
GET /api/catalog/my-products
Authorization: Bearer {token}
```

**권한**: ROLE_DISTRIBUTOR

**응답:** 상품 목록 배열

---

#### 7.5 유통업체 카탈로그 조회 (매장)
```
GET /api/catalog/distributor/{distributorId}
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

**응답:** 판매 가능한 상품 목록 (isAvailable=true)

---

#### 7.6 카테고리별 상품 조회 (매장)
```
GET /api/catalog/distributor/{distributorId}/category/{category}
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

**카테고리 예시:** 쌀/곡물, 채소, 과일, 육류, 수산물, 유제품

---

#### 7.7 상품 검색 (매장)
```
GET /api/catalog/distributor/{distributorId}/search?keyword=쌀
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

---

#### 7.8 가격 범위 검색 (매장)
```
GET /api/catalog/distributor/{distributorId}/price-range?minPrice=1000&maxPrice=50000
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

---

#### 7.9 재고 있는 상품만 조회 (매장)
```
GET /api/catalog/distributor/{distributorId}/in-stock
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

---

#### 7.10 상품 상세 조회
```
GET /api/catalog/products/{productId}
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER, ROLE_DISTRIBUTOR

---

#### 7.11 재고 업데이트 (유통업체)
```
PUT /api/catalog/products/{productId}/stock?quantity=450
Authorization: Bearer {token}
```

**권한**: ROLE_DISTRIBUTOR

---

#### 7.12 판매 상태 변경 (유통업체)
```
PUT /api/catalog/products/{productId}/toggle-availability
Authorization: Bearer {token}
```

**권한**: ROLE_DISTRIBUTOR

**설명:** 판매 가능 ↔ 품절 상태 토글

---

#### 7.13 배송 정보 등록/수정 (유통업체)
```
POST /api/catalog/products/{productId}/delivery-info
Authorization: Bearer {token}
Content-Type: application/json
```

**권한**: ROLE_DISTRIBUTOR

**요청 Body:**
```json
{
  "deliveryType": "익일배송",
  "deliveryFee": 3000,
  "freeDeliveryThreshold": 50000,
  "deliveryRegions": "서울,경기,인천",
  "deliveryDays": "월,화,수,목,금",
  "deliveryTimeSlots": "오전,오후",
  "estimatedDeliveryDays": 1,
  "packagingType": "박스",
  "isFragile": false,
  "requiresRefrigeration": false,
  "specialInstructions": "직사광선을 피해 보관해주세요"
}
```

---

#### 7.14 상품 상세 정보 조회 (가격, 재고, 배송 정보 포함) ⭐
```
GET /api/catalog/products/{productId}/detail
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER, ROLE_DISTRIBUTOR

**응답:**
```json
{
  "id": 1,
  "distributorId": "distributor1",
  "distributorName": "신선식자재 유통",
  "productName": "경기미 20kg",
  "category": "쌀/곡물",
  "description": "경기도에서 생산된 고품질 쌀입니다",
  "unitPrice": 48000,
  "unit": "포",
  "priceInfo": "1포당 48,000원",
  "hasDiscount": false,
  "stockQuantity": 100,
  "stockStatus": "재고 충분",
  "isAvailable": true,
  "minOrderQuantity": 1,
  "maxOrderQuantity": 50,
  "orderLimitInfo": "최소 1포 ~ 최대 50포",
  "origin": "경기도",
  "brand": "농협",
  "imageUrl": "https://example.com/rice.jpg",
  "certifications": "친환경인증",
  "deliveryInfo": {
    "deliveryType": "익일배송",
    "deliveryFee": 3000,
    "freeDeliveryThreshold": 50000,
    "deliveryFeeInfo": "배송비 3,000원 (50,000원 이상 무료)",
    "deliveryRegions": "서울,경기,인천",
    "deliveryDays": "월,화,수,목,금",
    "deliveryTimeSlots": "오전,오후",
    "estimatedDeliveryDays": 1,
    "estimatedDeliveryInfo": "익일 배송",
    "packagingType": "박스",
    "isFragile": false,
    "requiresRefrigeration": false,
    "specialInstructions": "직사광선을 피해 보관해주세요"
  },
  "createdAt": "2025-11-26T10:43:24",
  "updatedAt": "2025-11-26T10:43:24"
}
```

**상세 정보 설명:**
- `priceInfo`: 사용자 친화적 가격 표시 ("1포당 48,000원")
- `stockStatus`: "재고 충분", "재고 부족", "품절"
- `orderLimitInfo`: 주문 수량 제한 정보
- `deliveryFeeInfo`: 배송비 정보 ("배송비 3,000원 (50,000원 이상 무료)")
- `estimatedDeliveryInfo`: 예상 배송 일정 ("익일 배송", "주문 후 2일 내 배송")

---

### 8. 견적 요청 API ⭐

**견적 요청 워크플로우:**
```
[매장] 견적 요청 생성 (PENDING)
   ↓
[유통업체] 견적 요청 확인
   ↓
[유통업체] 견적 응답
   ├─ ACCEPTED (수락) → [매장] 완료 처리 → COMPLETED
   └─ REJECTED (거절) → 종료
   
[매장] 대기중 요청 취소 가능 (PENDING 상태만)
```

#### 8.1 견적 요청 생성 (매장 → 유통업체)
```
POST /api/matching/quote-request
Authorization: Bearer {token}
Content-Type: application/json
```

**권한**: ROLE_STORE_OWNER

**요청 Body:**
```json
{
  "distributorId": "distributor1",
  "requestedProducts": "쌀/곡물,채소,육류",
  "message": "매주 월요일 오전 배송 가능한지 확인 부탁드립니다."
}
```

**응답:**
```json
{
  "id": 1,
  "storeId": "store_owner1",
  "storeName": "맛있는 한식당",
  "distributorId": "distributor1",
  "distributorName": "신선식자재 유통",
  "requestedProducts": "쌀/곡물,채소,육류",
  "message": "매주 월요일 오전 배송 가능한지 확인 부탁드립니다.",
  "status": "PENDING",
  "estimatedAmount": null,
  "distributorResponse": null,
  "requestedAt": "2025-11-26T10:30:00",
  "respondedAt": null
}
```

**견적 상태 (status):**
- `PENDING`: 대기중
- `ACCEPTED`: 수락됨
- `REJECTED`: 거절됨
- `COMPLETED`: 완료됨

---

#### 8.2 매장의 견적 요청 목록 조회
```
GET /api/matching/quote-requests/store
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

**응답:**
```json
[
  {
    "id": 1,
    "storeId": "store_owner1",
    "storeName": "맛있는 한식당",
    "distributorId": "distributor1",
    "distributorName": "신선식자재 유통",
    "requestedProducts": "쌀/곡물,채소,육류",
    "message": "매주 월요일 오전 배송 가능한지 확인 부탁드립니다.",
    "status": "ACCEPTED",
    "estimatedAmount": 500000,
    "distributorResponse": "매주 월요일 오전 8시 배송 가능합니다. 최소 주문금액은 10만원입니다.",
    "requestedAt": "2025-11-26T10:30:00",
    "respondedAt": "2025-11-26T11:00:00"
  }
]
```

---

#### 8.3 유통업체의 견적 요청 목록 조회
```
GET /api/matching/quote-requests/distributor
Authorization: Bearer {token}
```

**권한**: ROLE_DISTRIBUTOR

**응답**: 6.2와 동일

---

#### 8.4 견적 요청 상세 조회
```
GET /api/matching/quote-request/{id}
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER, ROLE_DISTRIBUTOR

**응답**: 6.1과 동일

---

#### 8.5 견적 요청 응답 (유통업체)
```
PUT /api/matching/quote-request/{id}/respond
Authorization: Bearer {token}
Content-Type: application/json
```

**권한**: ROLE_DISTRIBUTOR

**요청 Body:**
```json
{
  "status": "ACCEPTED",
  "estimatedAmount": 500000,
  "response": "매주 월요일 오전 8시 배송 가능합니다. 최소 주문금액은 10만원입니다."
}
```

**status 값:**
- `ACCEPTED`: 견적 수락
- `REJECTED`: 견적 거절

**응답:**
```json
{
  "id": 1,
  "storeId": "store_owner1",
  "storeName": "맛있는 한식당",
  "distributorId": "distributor1",
  "distributorName": "신선식자재 유통",
  "requestedProducts": "쌀/곡물,채소,육류",
  "message": "매주 월요일 오전 배송 가능한지 확인 부탁드립니다.",
  "status": "ACCEPTED",
  "estimatedAmount": 500000,
  "distributorResponse": "매주 월요일 오전 8시 배송 가능합니다. 최소 주문금액은 10만원입니다.",
  "requestedAt": "2025-11-26T10:30:00",
  "respondedAt": "2025-11-26T11:00:00"
}
```

**에러:**
- `403`: 권한 없음 (다른 유통업체의 견적 요청)
- `400`: 이미 응답한 견적 요청

---

#### 8.6 견적 요청 취소 (매장)
```
DELETE /api/matching/quote-request/{id}
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

**응답:**
```
200 OK
"견적 요청이 취소되었습니다."
```

**에러:**
- `403`: 권한 없음 (다른 매장의 견적 요청)
- `400`: 대기중인 요청만 취소 가능

---

#### 8.7 견적 완료 처리 (매장)
```
PUT /api/matching/quote-request/{id}/complete
Authorization: Bearer {token}
```

**권한**: ROLE_STORE_OWNER

**응답:**
```json
{
  "id": 1,
  "storeId": "store_owner1",
  "storeName": "맛있는 한식당",
  "distributorId": "distributor1",
  "distributorName": "신선식자재 유통",
  "requestedProducts": "쌀/곡물,채소,육류",
  "message": "매주 월요일 오전 배송 가능한지 확인 부탁드립니다.",
  "status": "COMPLETED",
  "estimatedAmount": 500000,
  "distributorResponse": "매주 월요일 오전 8시 배송 가능합니다. 최소 주문금액은 10만원입니다.",
  "requestedAt": "2025-11-26T10:30:00",
  "respondedAt": "2025-11-26T11:00:00"
}
```

**에러:**
- `403`: 권한 없음 (다른 매장의 견적 요청)
- `400`: 수락된 요청만 완료 가능

---

## 🔐 인증 및 권한

### JWT 토큰 사용
모든 인증이 필요한 API는 헤더에 JWT 토큰을 포함해야 합니다:
```
Authorization: Bearer {accessToken}
```

### 역할(Role)
- `ROLE_USER`: 일반 사용자
- `ROLE_ADMIN`: 관리자
- `ROLE_STORE_OWNER`: 매장 사장님
- `ROLE_DISTRIBUTOR`: 유통업체

### 권한 체크
- 매장 정보 API: `ROLE_STORE_OWNER` 필요
- 유통업체 정보 API: `ROLE_DISTRIBUTOR` 필요
- 매칭 API: `ROLE_STORE_OWNER` 필요

---

## 📊 데이터 형식

### UserType
- `STORE_OWNER`: 매장 사장님
- `DISTRIBUTOR`: 유통업체

### 콤마로 구분되는 필드
- `mainProducts`: "쌀/곡물,채소,육류"
- `supplyProducts`: "쌀/곡물,채소,과일,육류,수산물"
- `serviceRegions`: "서울,경기,인천"
- `certifications`: "HACCP,ISO22000,유기농인증"

---

## 🧪 테스트 스크립트

### 전체 테스트
```bash
# 매장 정보 API 테스트
./test-store-api.sh

# 유통업체 정보 API 테스트
./test-distributor-api.sh

# 매칭 API 테스트
./test-matching-api.sh

# 견적 요청 API 테스트
./test-quote-request-api.sh
```

### 개별 테스트
```bash
# 회원가입
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_store",
    "password": "password123",
    "email": "test@example.com",
    "userType": "STORE_OWNER",
    "businessNumber": "123-45-67890",
    "businessName": "테스트 매장",
    "ownerName": "김사장",
    "phoneNumber": "010-1234-5678",
    "address": "서울시 강남구"
  }'

# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_store",
    "password": "password123"
  }'

# 매장 정보 등록 (토큰 필요)
curl -X POST http://localhost:8080/api/store/info \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "storeName": "테스트 매장",
    "businessType": "한식",
    "region": "서울 강남구",
    "mainProducts": "쌀/곡물,채소,육류",
    "phoneNumber": "010-1234-5678",
    "address": "서울시 강남구"
  }'

# 추천 유통업체 조회 (토큰 필요)
curl -X GET "http://localhost:8080/api/matching/recommend?limit=10" \
  -H "Authorization: Bearer {TOKEN}"

# 견적 요청 생성 (토큰 필요)
curl -X POST http://localhost:8080/api/matching/quote-request \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "distributorId": "distributor1",
    "requestedProducts": "쌀/곡물,채소,육류",
    "message": "매주 월요일 오전 배송 가능한지 확인 부탁드립니다."
  }'

# 매장의 견적 요청 목록 조회 (토큰 필요)
curl -X GET http://localhost:8080/api/matching/quote-requests/store \
  -H "Authorization: Bearer {TOKEN}"

# 견적 요청 응답 (유통업체, 토큰 필요)
curl -X PUT http://localhost:8080/api/matching/quote-request/1/respond \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {DISTRIBUTOR_TOKEN}" \
  -d '{
    "status": "ACCEPTED",
    "estimatedAmount": 500000,
    "response": "매주 월요일 오전 8시 배송 가능합니다."
  }'
```

---

## 🚨 에러 코드

| 상태 코드 | 설명 |
|----------|------|
| 200 | 성공 |
| 400 | 잘못된 요청 (중복 데이터, 유효성 검증 실패) |
| 401 | 인증 실패 (토큰 없음 또는 만료) |
| 403 | 권한 없음 (역할 불일치) |
| 404 | 리소스 없음 (매장/유통업체 정보 없음) |
| 500 | 서버 오류 |

---

## 📝 프론트엔드 개발자를 위한 참고사항

### 1. 토큰 관리
- 로그인 후 받은 `accessToken`을 로컬 스토리지에 저장
- 모든 API 요청 시 헤더에 포함
- 토큰 만료 시 재로그인 필요 (만료 시간: 24시간)

### 2. 회원가입 후 흐름

**매장 사장님:**
```
회원가입 → 로그인 → 매장 정보 등록 → 추천 유통업체 조회 → 견적 요청 → 응답 확인 → 완료 처리
```

**유통업체:**
```
회원가입 → 로그인 → 유통업체 정보 등록 → 견적 요청 수신 → 견적 응답 (수락/거절)
```

### 3. 견적 요청 흐름
1. 매장이 추천 유통업체 목록에서 원하는 유통업체 선택
2. 견적 요청 생성 (요청 품목, 메시지 포함)
3. 유통업체가 견적 요청 목록에서 확인
4. 유통업체가 견적 응답 (수락/거절, 예상 금액, 응답 메시지)
5. 매장이 수락된 견적 확인 후 완료 처리

### 4. 빈 응답 처리
- 추천 API가 `[]` 반환 시: "아직 등록된 유통업체가 없습니다" 메시지 표시
- 매장 정보 조회 시 404: "매장 정보를 먼저 등록해주세요" 메시지 표시
- 견적 요청 목록이 `[]`: "아직 견적 요청이 없습니다" 메시지 표시

### 5. 에러 처리
```javascript
try {
  const response = await fetch('/api/matching/recommend', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (response.status === 404) {
    // 매장 정보 없음 → 매장 정보 등록 화면으로 이동
  } else if (response.status === 401) {
    // 토큰 만료 → 로그인 화면으로 이동
  } else if (response.ok) {
    const data = await response.json();
    if (data.length === 0) {
      // 유통업체 없음 → 안내 메시지 표시
    }
  }
} catch (error) {
  // 네트워크 오류 처리
}
```

---

## 🔄 데이터베이스 초기화

개발 중 데이터베이스를 초기화하려면:
```bash
./reset-database.sh
```

---

## 📞 문의

백엔드 API 관련 문의사항이 있으면 백엔드 팀에 연락주세요.
