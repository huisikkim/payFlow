# 유통업체 비교 API - Flutter 가이드

## 📊 개요

여러 유통업체를 가격, 배송, 서비스, 품질, 인증 등 다양한 기준으로 비교할 수 있는 API입니다.

**Base URL**: `http://10.0.2.2:8080` (Android 에뮬레이터)

---

## 🎯 주요 기능

1. **추천 유통업체 비교** - Top N 추천 업체를 한눈에 비교
2. **특정 유통업체 비교** - 선택한 업체들만 비교
3. **카테고리별 최고 업체** - 가격/배송/품질 등 카테고리별 1등 찾기

---

## 📋 API 목록

### 1. 추천 유통업체 비교 (Top N) ⭐

**엔드포인트:**
```
GET http://10.0.2.2:8080/api/matching/compare/top?topN=5
```

**헤더:**
```
Authorization: Bearer {로그인에서 받은 accessToken}
```

**파라미터:**
- `topN` (선택): 비교할 유통업체 수 (기본값: 5)

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/matching/compare/top?topN=5');
final response = await http.get(
  url,
  headers: {
    'Authorization': 'Bearer $token',
  },
);

if (response.statusCode == 200) {
  final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
  final comparisons = data.map((json) => DistributorComparison.fromJson(json)).toList();
}
```

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

---

### 2. 특정 유통업체 비교

**엔드포인트:**
```
POST http://10.0.2.2:8080/api/matching/compare
```

**헤더:**
```
Authorization: Bearer {로그인에서 받은 accessToken}
Content-Type: application/json
```

**요청 Body:**
```json
["distributor1", "distributor2", "distributor3"]
```

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/matching/compare');
final response = await http.post(
  url,
  headers: {
    'Authorization': 'Bearer $token',
    'Content-Type': 'application/json',
  },
  body: jsonEncode(['distributor1', 'distributor2', 'distributor3']),
);
```

**응답:** API 1과 동일

---

### 3. 카테고리별 최고 유통업체

**엔드포인트:**
```
POST http://10.0.2.2:8080/api/matching/compare/best-by-category
```

**헤더:**
```
Authorization: Bearer {로그인에서 받은 accessToken}
Content-Type: application/json
```

**요청 Body:**
```json
["distributor1", "distributor2", "distributor3"]
```

**요청 예시 (Dart):**
```dart
final url = Uri.parse('http://10.0.2.2:8080/api/matching/compare/best-by-category');
final response = await http.post(
  url,
  headers: {
    'Authorization': 'Bearer $token',
    'Content-Type': 'application/json',
  },
  body: jsonEncode(['distributor1', 'distributor2', 'distributor3']),
);

if (response.statusCode == 200) {
  final Map<String, dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
  // data['PRICE'] - 가격 최고
  // data['DELIVERY'] - 배송 최고
  // data['QUALITY'] - 품질 최고
  // data['CERTIFICATION'] - 인증 최고
  // data['OVERALL'] - 종합 최고
}
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

## 🎨 UI 구현 가이드

### 비교 화면 구성

```
┌─────────────────────────────────────┐
│  유통업체 비교 (Top 5)              │
├─────────────────────────────────────┤
│                                     │
│  [1위] 신선식자재 유통  ⭐ 98.5점  │
│  ├ 가격: 보통 (10만원~)            │
│  ├ 배송: 익일 배송 (무료)          │
│  ├ 품질: 최상 (신뢰도 86점)        │
│  ├ 인증: HACCP, ISO22000           │
│  └ 강점: 서비스 지역 완벽 일치...  │
│                                     │
│  [2위] 프리미엄 유통  ⭐ 87.3점    │
│  ├ 가격: 비쌈 (20만원~)            │
│  ├ 배송: 당일 배송 (5천원)         │
│  ├ 품질: 최상 (신뢰도 92점)        │
│  ├ 인증: HACCP, ISO22000, 유기농   │
│  └ 강점: 프리미엄 품질...          │
│                                     │
│  [비교표 보기] [카테고리별 최고]   │
└─────────────────────────────────────┘
```

### 비교표 화면

```
┌─────────────────────────────────────┐
│  항목      │ 신선식자재 │ 프리미엄  │
├─────────────────────────────────────┤
│  종합점수  │  98.5점   │  87.3점   │
│  가격대    │  보통     │  비쌈     │
│  최소주문  │  10만원   │  20만원   │
│  배송속도  │  익일     │  당일     │
│  배송비    │  무료     │  5천원    │
│  품질등급  │  최상     │  최상     │
│  신뢰도    │  86점     │  92점     │
│  인증개수  │  2개      │  3개      │
└─────────────────────────────────────┘
```

---

## 📊 비교 지표 설명

### 가격 레벨 (priceLevel)
- `LOW` - 저렴 (5만원 미만)
- `MEDIUM` - 보통 (5만원~15만원)
- `HIGH` - 비쌈 (15만원 이상)

### 배송 속도 (deliverySpeed)
- `SAME_DAY` - 당일 배송
- `NEXT_DAY` - 익일 배송
- `TWO_TO_THREE_DAYS` - 2-3일 배송
- `OVER_THREE_DAYS` - 3일 이상

### 품질 등급 (qualityRating)
- `EXCELLENT` - 최상
- `GOOD` - 상
- `AVERAGE` - 중
- `BELOW_AVERAGE` - 하

### 최고 카테고리 (bestCategory)
- `PRICE` - 가격
- `DELIVERY` - 배송
- `QUALITY` - 품질
- `SERVICE` - 서비스
- `CERTIFICATION` - 인증

---

## 🎯 사용 시나리오

### 1. 추천 업체 비교
```dart
// 1. 추천 유통업체 조회
final recommendations = await getRecommendations();

// 2. Top 5 비교
final comparisons = await compareTopDistributors(5);

// 3. 비교 화면 표시
showComparisonScreen(comparisons);
```

### 2. 선택 업체 비교
```dart
// 1. 사용자가 비교할 업체 선택
List<String> selectedIds = ['distributor1', 'distributor2'];

// 2. 비교 요청
final comparisons = await compareDistributors(selectedIds);

// 3. 비교표 표시
showComparisonTable(comparisons);
```

### 3. 카테고리별 최고 찾기
```dart
// 1. 비교할 업체 선택
List<String> selectedIds = ['distributor1', 'distributor2', 'distributor3'];

// 2. 카테고리별 최고 조회
final bestByCategory = await findBestByCategory(selectedIds);

// 3. 결과 표시
print('가격 최고: ${bestByCategory['PRICE'].distributorName}');
print('배송 최고: ${bestByCategory['DELIVERY'].distributorName}');
print('품질 최고: ${bestByCategory['QUALITY'].distributorName}');
```

---

## 💡 UI/UX 권장사항

### 1. 비교 카드
- 순위 배지 표시 (1위, 2위, 3위)
- 종합 점수를 별점으로 표시 (98.5점 → ⭐⭐⭐⭐⭐)
- 강점은 초록색, 약점은 회색으로 표시

### 2. 비교표
- 가로 스크롤 가능하게 구현
- 최고 값은 하이라이트 (파란색 배경)
- 터치하면 상세 정보 표시

### 3. 필터링
- 가격대별 필터 (저렴/보통/비쌈)
- 배송 속도별 필터 (당일/익일/2-3일)
- 품질 등급별 필터 (최상/상/중)

### 4. 정렬
- 종합 점수 순
- 가격 낮은 순
- 배송 빠른 순
- 신뢰도 높은 순