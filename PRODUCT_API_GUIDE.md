# Pick Swap Product API 가이드

Flutter 앱에서 사용할 수 있는 상품 관련 REST API 문서입니다.

## Base URL
```
http://localhost:8080/api/products
```

---

## 📱 Flutter에서 주로 사용할 API

### 1. Home Feed - 최신 상품 리스트 조회
**가장 중요한 API입니다. Flutter 앱의 메인 화면에서 사용하세요.**

```http
GET /api/products/feed?page=0&size=20
```

**Query Parameters:**
- `page` (optional): 페이지 번호 (기본값: 0)
- `size` (optional): 페이지 크기 (기본값: 20)

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "아이폰 14 Pro 256GB",
      "description": "거의 새것 같은 상태입니다.",
      "price": 950000,
      "category": "ELECTRONICS",
      "productCondition": "LIKE_NEW",
      "status": "AVAILABLE",
      "sellerId": 1,
      "sellerName": "김철수",
      "location": "서울 강남구",
      "imageUrls": ["https://picsum.photos/400/400?random=1"],
      "viewCount": 45,
      "likeCount": 12,
      "chatCount": 3,
      "createdAt": "2025-11-19T10:30:00",
      "updatedAt": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 15,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

**Flutter 사용 예시:**
```dart
Future<List<Product>> getHomeFeed(int page) async {
  final response = await http.get(
    Uri.parse('$baseUrl/feed?page=$page&size=20'),
  );
  
  if (response.statusCode == 200) {
    final data = json.decode(response.body);
    return (data['content'] as List)
        .map((item) => Product.fromJson(item))
        .toList();
  }
  throw Exception('Failed to load products');
}
```

---

### 2. 인기 상품 조회 (좋아요 많은 순)

```http
GET /api/products/popular?page=0&size=20
```

**Query Parameters:**
- `page` (optional): 페이지 번호
- `size` (optional): 페이지 크기

**Response:** Home Feed와 동일한 형식

---

### 3. 카테고리별 상품 조회

```http
GET /api/products/category/{category}?page=0&size=20
```

**Path Parameters:**
- `category`: 카테고리 (아래 목록 참조)

**카테고리 목록:**
- `ELECTRONICS` - 전자기기
- `FASHION` - 패션/의류
- `HOME_APPLIANCES` - 가전제품
- `FURNITURE` - 가구/인테리어
- `BOOKS` - 도서
- `SPORTS` - 스포츠/레저
- `TOYS` - 완구/취미
- `BEAUTY` - 뷰티/미용
- `FOOD` - 식품
- `PET_SUPPLIES` - 반려동물용품
- `OTHERS` - 기타

**Flutter 사용 예시:**
```dart
Future<List<Product>> getProductsByCategory(String category, int page) async {
  final response = await http.get(
    Uri.parse('$baseUrl/category/$category?page=$page&size=20'),
  );
  
  if (response.statusCode == 200) {
    final data = json.decode(response.body);
    return (data['content'] as List)
        .map((item) => Product.fromJson(item))
        .toList();
  }
  throw Exception('Failed to load products');
}
```

---

### 4. 키워드로 상품 검색

```http
GET /api/products/search?keyword={keyword}&page=0&size=20
```

**Query Parameters:**
- `keyword` (required): 검색 키워드
- `page` (optional): 페이지 번호
- `size` (optional): 페이지 크기

**Flutter 사용 예시:**
```dart
Future<List<Product>> searchProducts(String keyword, int page) async {
  final response = await http.get(
    Uri.parse('$baseUrl/search?keyword=$keyword&page=$page&size=20'),
  );
  
  if (response.statusCode == 200) {
    final data = json.decode(response.body);
    return (data['content'] as List)
        .map((item) => Product.fromJson(item))
        .toList();
  }
  throw Exception('Failed to search products');
}
```

---

### 5. 상품 상세 조회

```http
GET /api/products/{productId}
```

**Path Parameters:**
- `productId`: 상품 ID

**Response:**
```json
{
  "id": 1,
  "title": "아이폰 14 Pro 256GB",
  "description": "거의 새것 같은 상태입니다. 케이스와 함께 드립니다.",
  "price": 950000,
  "category": "ELECTRONICS",
  "productCondition": "LIKE_NEW",
  "status": "AVAILABLE",
  "sellerId": 1,
  "sellerName": "김철수",
  "location": "서울 강남구",
  "imageUrls": [
    "https://picsum.photos/400/400?random=1",
    "https://picsum.photos/400/400?random=2"
  ],
  "viewCount": 46,
  "likeCount": 12,
  "chatCount": 3,
  "createdAt": "2025-11-19T10:30:00",
  "updatedAt": null,
  "soldAt": null
}
```

**Flutter 사용 예시:**
```dart
Future<Product> getProductDetail(int productId) async {
  final response = await http.get(
    Uri.parse('$baseUrl/$productId'),
  );
  
  if (response.statusCode == 200) {
    return Product.fromJson(json.decode(response.body));
  }
  throw Exception('Failed to load product detail');
}
```

---

### 6. 상품 등록

```http
POST /api/products
```

**Request Body:**
```json
{
  "title": "아이폰 14 Pro 256GB",
  "description": "거의 새것 같은 상태입니다.",
  "price": 950000,
  "category": "ELECTRONICS",
  "productCondition": "LIKE_NEW",
  "sellerId": 1,
  "sellerName": "김철수",
  "location": "서울 강남구",
  "imageUrls": [
    "https://picsum.photos/400/400?random=1"
  ]
}
```

**상품 상태 (productCondition):**
- `NEW` - 새 상품
- `LIKE_NEW` - 거의 새것
- `GOOD` - 좋음
- `FAIR` - 보통
- `POOR` - 나쁨

**Flutter 사용 예시:**
```dart
Future<Product> createProduct(Product product) async {
  final response = await http.post(
    Uri.parse(baseUrl),
    headers: {'Content-Type': 'application/json'},
    body: json.encode({
      'title': product.title,
      'description': product.description,
      'price': product.price,
      'category': product.category,
      'productCondition': product.productCondition,
      'sellerId': product.sellerId,
      'sellerName': product.sellerName,
      'location': product.location,
      'imageUrls': product.imageUrls,
    }),
  );
  
  if (response.statusCode == 201) {
    return Product.fromJson(json.decode(response.body));
  }
  throw Exception('Failed to create product');
}
```

---

### 7. 상품 수정

```http
PUT /api/products/{productId}
```

**Request Body:**
```json
{
  "title": "아이폰 14 Pro 256GB (가격 인하)",
  "description": "가격을 낮췄습니다!",
  "price": 900000,
  "category": "ELECTRONICS",
  "productCondition": "LIKE_NEW",
  "location": "서울 강남구",
  "imageUrls": [
    "https://picsum.photos/400/400?random=1"
  ]
}
```

---

### 8. 상품 삭제

```http
DELETE /api/products/{productId}
```

**Response:** 204 No Content

---

### 9. 상품 좋아요

```http
POST /api/products/{productId}/like
```

**Flutter 사용 예시:**
```dart
Future<void> likeProduct(int productId) async {
  final response = await http.post(
    Uri.parse('$baseUrl/$productId/like'),
  );
  
  if (response.statusCode != 200) {
    throw Exception('Failed to like product');
  }
}
```

---

### 10. 상품 좋아요 취소

```http
DELETE /api/products/{productId}/like
```

---

### 11. 판매자의 상품 목록 조회

```http
GET /api/products/seller/{sellerId}?page=0&size=20
```

---

### 12. 가격 범위로 상품 검색

```http
GET /api/products/price-range?minPrice=100000&maxPrice=500000&page=0&size=20
```

**Query Parameters:**
- `minPrice` (required): 최소 가격
- `maxPrice` (required): 최대 가격
- `page` (optional): 페이지 번호
- `size` (optional): 페이지 크기

---

### 13. 지역별 상품 조회

```http
GET /api/products/location?location=서울&page=0&size=20
```

**Query Parameters:**
- `location` (required): 지역명
- `page` (optional): 페이지 번호
- `size` (optional): 페이지 크기

---

### 14. 상품 판매 완료 처리

```http
POST /api/products/{productId}/sold
```

---

## 📦 Flutter Model 클래스 예시

```dart
class Product {
  final int id;
  final String title;
  final String description;
  final double price;
  final String category;
  final String productCondition;
  final String status;
  final int sellerId;
  final String sellerName;
  final String location;
  final List<String> imageUrls;
  final int viewCount;
  final int likeCount;
  final int chatCount;
  final DateTime createdAt;
  final DateTime? updatedAt;

  Product({
    required this.id,
    required this.title,
    required this.description,
    required this.price,
    required this.category,
    required this.productCondition,
    required this.status,
    required this.sellerId,
    required this.sellerName,
    required this.location,
    required this.imageUrls,
    required this.viewCount,
    required this.likeCount,
    required this.chatCount,
    required this.createdAt,
    this.updatedAt,
  });

  factory Product.fromJson(Map<String, dynamic> json) {
    return Product(
      id: json['id'],
      title: json['title'],
      description: json['description'],
      price: json['price'].toDouble(),
      category: json['category'],
      productCondition: json['productCondition'],
      status: json['status'],
      sellerId: json['sellerId'],
      sellerName: json['sellerName'],
      location: json['location'],
      imageUrls: List<String>.from(json['imageUrls']),
      viewCount: json['viewCount'],
      likeCount: json['likeCount'],
      chatCount: json['chatCount'],
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: json['updatedAt'] != null 
          ? DateTime.parse(json['updatedAt']) 
          : null,
    );
  }
}
```

---

## 🚀 테스트 방법

1. Spring Boot 애플리케이션 실행:
```bash
./gradlew bootRun
```

2. PowerShell 테스트 스크립트 실행:
```powershell
.\test-product-api.ps1
```

3. 또는 브라우저에서 직접 테스트:
```
http://localhost:8080/api/products/feed
```

---

## 📝 주의사항

1. **페이징**: 모든 리스트 API는 페이징을 지원합니다. Flutter에서 무한 스크롤 구현 시 `page` 파라미터를 증가시키면서 호출하세요.

2. **이미지 URL**: 현재는 샘플 이미지 URL을 사용하고 있습니다. 실제 서비스에서는 이미지 업로드 API를 별도로 구현해야 합니다.

3. **인증**: 현재는 인증이 없지만, 실제 서비스에서는 JWT 토큰을 헤더에 포함해야 합니다.

4. **에러 처리**: API 호출 실패 시 적절한 에러 처리를 구현하세요.

---

## 🎯 Flutter 앱 구현 권장사항

1. **상태 관리**: Provider, Riverpod, Bloc 등을 사용하여 상품 데이터를 관리하세요.

2. **캐싱**: 네트워크 요청을 줄이기 위해 로컬 캐싱을 구현하세요.

3. **무한 스크롤**: ListView.builder와 ScrollController를 사용하여 무한 스크롤을 구현하세요.

4. **이미지 로딩**: cached_network_image 패키지를 사용하여 이미지를 효율적으로 로드하세요.

5. **검색 디바운싱**: 검색 입력 시 디바운싱을 적용하여 불필요한 API 호출을 줄이세요.
