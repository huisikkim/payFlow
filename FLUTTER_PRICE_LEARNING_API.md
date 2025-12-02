# 📊 가격 학습/비교 API 가이드 (Flutter 개발자용)

> 가게 사장님이 식자재 단가를 비교하고, 적정 가격인지 확인할 수 있는 기능입니다.

## 📌 Base URL

```
http://localhost:8080/api/price-learning
```

---

## 🔑 인증

모든 API는 JWT 토큰 인증이 필요합니다.

```dart
headers: {
  'Authorization': 'Bearer $accessToken',
  'Content-Type': 'application/json',
}
```

---

## 📋 API 목록

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/items` | 전체 품목 목록 조회 |
| GET | `/items/{itemName}/statistics` | 품목별 단가 통계 |
| GET | `/items/{itemName}/history` | 품목별 단가 이력 |
| GET | `/items/{itemName}/recommended-price` | 추천 단가 조회 |
| GET | `/alerts/active` | 활성 가격 경고 목록 |
| GET | `/alerts/recent` | 최근 가격 경고 목록 |
| GET | `/alerts/{alertId}` | 경고 상세 조회 |
| POST | `/alerts/{alertId}/acknowledge` | 경고 확인 처리 |
| POST | `/alerts/{alertId}/resolve` | 경고 해결 처리 |

---

## 📖 API 상세

### 1. 전체 품목 목록 조회

품목 선택 드롭다운이나 검색에 사용합니다.

**Request**
```
GET /api/price-learning/items
```

**Response**
```json
[
  "양파",
  "감자",
  "당근",
  "대파",
  "마늘",
  "돼지고기 삼겹살",
  "닭가슴살"
]
```

**Flutter 예시**
```dart
Future<List<String>> getItemList() async {
  final response = await http.get(
    Uri.parse('$baseUrl/api/price-learning/items'),
    headers: _authHeaders(),
  );
  
  if (response.statusCode == 200) {
    return List<String>.from(jsonDecode(response.body));
  }
  throw Exception('품목 목록 조회 실패');
}
```

---

### 2. 품목별 단가 통계 조회 ⭐ (핵심 API)

특정 품목의 평균가, 최저가, 최고가, 추천가 등을 조회합니다.

**Request**
```
GET /api/price-learning/items/{itemName}/statistics?days=30
```

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| itemName | String | ✅ | - | 품목명 (URL 인코딩 필요) |
| days | Integer | ❌ | 30 | 조회 기간 (일) |

**Response**
```json
{
  "itemName": "양파",
  "averagePrice": 2500,
  "minPrice": 2000,
  "maxPrice": 3200,
  "recentPrice": 2800,
  "recommendedPrice": 2600,
  "dataPoints": 45,
  "volatility": 15.5,
  "volatilityLevel": "MEDIUM"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| itemName | String | 품목명 |
| averagePrice | Long | 평균 단가 (원) |
| minPrice | Long | 최저 단가 (원) |
| maxPrice | Long | 최고 단가 (원) |
| recentPrice | Long | 가장 최근 단가 (원) |
| recommendedPrice | Long | 추천 단가 (원) - 가중 평균 기반 |
| dataPoints | Integer | 데이터 수 (거래 건수) |
| volatility | Double | 변동성 (%) - 표준편차/평균 |
| volatilityLevel | String | 변동성 수준 (LOW/MEDIUM/HIGH) |

**Flutter 예시**
```dart
Future<PriceStatistics> getStatistics(String itemName, {int days = 30}) async {
  final encodedName = Uri.encodeComponent(itemName);
  final response = await http.get(
    Uri.parse('$baseUrl/api/price-learning/items/$encodedName/statistics?days=$days'),
    headers: _authHeaders(),
  );
  
  if (response.statusCode == 200) {
    return PriceStatistics.fromJson(jsonDecode(response.body));
  }
  throw Exception('통계 조회 실패');
}
```

---

### 3. 품목별 단가 이력 조회

차트나 그래프를 그릴 때 사용합니다.

**Request**
```
GET /api/price-learning/items/{itemName}/history?days=30
```

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| itemName | String | ✅ | - | 품목명 (URL 인코딩 필요) |
| days | Integer | ❌ | 30 | 조회 기간 (일) |

**Response**
```json
[
  {
    "id": 1,
    "itemName": "양파",
    "unitPrice": 2800,
    "unit": "kg",
    "orderId": "INGR_ORDER_abc123",
    "distributorId": "DIST_001",
    "storeId": "STORE_001",
    "recordedAt": "2025-12-02T10:30:00"
  },
  {
    "id": 2,
    "itemName": "양파",
    "unitPrice": 2500,
    "unit": "kg",
    "orderId": "INGR_ORDER_def456",
    "distributorId": "DIST_002",
    "storeId": "STORE_001",
    "recordedAt": "2025-12-01T14:20:00"
  }
]
```

**Flutter 예시**
```dart
Future<List<PriceHistory>> getPriceHistory(String itemName, {int days = 30}) async {
  final encodedName = Uri.encodeComponent(itemName);
  final response = await http.get(
    Uri.parse('$baseUrl/api/price-learning/items/$encodedName/history?days=$days'),
    headers: _authHeaders(),
  );
  
  if (response.statusCode == 200) {
    final List<dynamic> data = jsonDecode(response.body);
    return data.map((json) => PriceHistory.fromJson(json)).toList();
  }
  throw Exception('이력 조회 실패');
}
```

---

### 4. 추천 단가 조회

특정 품목의 추천 단가만 빠르게 조회합니다.

**Request**
```
GET /api/price-learning/items/{itemName}/recommended-price
```

**Response**
```json
2600
```

**Flutter 예시**
```dart
Future<int> getRecommendedPrice(String itemName) async {
  final encodedName = Uri.encodeComponent(itemName);
  final response = await http.get(
    Uri.parse('$baseUrl/api/price-learning/items/$encodedName/recommended-price'),
    headers: _authHeaders(),
  );
  
  if (response.statusCode == 200) {
    return int.parse(response.body);
  }
  throw Exception('추천 단가 조회 실패');
}
```

---

### 5. 활성 가격 경고 목록 조회

아직 확인하지 않은 가격 급등 경고를 조회합니다.

**Request**
```
GET /api/price-learning/alerts/active
```

**Response**
```json
[
  {
    "alertId": "ALERT_abc123",
    "itemName": "양파",
    "currentPrice": 4500,
    "averagePrice": 2500,
    "surgePercentage": 80.0,
    "alertType": "HIGH_SURGE",
    "status": "ACTIVE",
    "orderId": "INGR_ORDER_xyz789",
    "distributorId": "DIST_003",
    "message": "양파의 단가가 평균 대비 80.0% 급등했습니다. (평균: 2,500원 → 현재: 4,500원)",
    "createdAt": "2025-12-02T09:00:00",
    "acknowledgedAt": null,
    "resolvedAt": null
  }
]
```

| 필드 | 타입 | 설명 |
|------|------|------|
| alertId | String | 경고 ID |
| itemName | String | 품목명 |
| currentPrice | Long | 현재 단가 |
| averagePrice | Long | 평균 단가 |
| surgePercentage | Double | 급등률 (%) |
| alertType | String | 경고 유형 (MODERATE_SURGE/HIGH_SURGE/EXTREME_SURGE) |
| status | String | 상태 (ACTIVE/ACKNOWLEDGED/RESOLVED) |
| message | String | 경고 메시지 |
| createdAt | DateTime | 생성 시간 |

**alertType 설명**
| 값 | 설명 | 급등률 |
|----|------|--------|
| MODERATE_SURGE | 중간 급등 | 20~50% |
| HIGH_SURGE | 높은 급등 | 50~100% |
| EXTREME_SURGE | 극심한 급등 | 100% 이상 |

---

### 6. 경고 확인 처리

경고를 확인했음을 표시합니다.

**Request**
```
POST /api/price-learning/alerts/{alertId}/acknowledge
```

**Response**
```
200 OK (빈 응답)
```

**Flutter 예시**
```dart
Future<void> acknowledgeAlert(String alertId) async {
  final response = await http.post(
    Uri.parse('$baseUrl/api/price-learning/alerts/$alertId/acknowledge'),
    headers: _authHeaders(),
  );
  
  if (response.statusCode != 200) {
    throw Exception('경고 확인 처리 실패');
  }
}
```

---

### 7. 경고 해결 처리

경고를 해결 완료로 표시합니다.

**Request**
```
POST /api/price-learning/alerts/{alertId}/resolve
```

**Response**
```
200 OK (빈 응답)
```

---

## 🎨 Flutter 모델 클래스

### PriceStatistics
```dart
class PriceStatistics {
  final String itemName;
  final int averagePrice;
  final int minPrice;
  final int maxPrice;
  final int recentPrice;
  final int recommendedPrice;
  final int dataPoints;
  final double volatility;
  final String volatilityLevel;

  PriceStatistics({
    required this.itemName,
    required this.averagePrice,
    required this.minPrice,
    required this.maxPrice,
    required this.recentPrice,
    required this.recommendedPrice,
    required this.dataPoints,
    required this.volatility,
    required this.volatilityLevel,
  });

  factory PriceStatistics.fromJson(Map<String, dynamic> json) {
    return PriceStatistics(
      itemName: json['itemName'] ?? '',
      averagePrice: json['averagePrice'] ?? 0,
      minPrice: json['minPrice'] ?? 0,
      maxPrice: json['maxPrice'] ?? 0,
      recentPrice: json['recentPrice'] ?? 0,
      recommendedPrice: json['recommendedPrice'] ?? 0,
      dataPoints: json['dataPoints'] ?? 0,
      volatility: (json['volatility'] ?? 0).toDouble(),
      volatilityLevel: json['volatilityLevel'] ?? 'LOW',
    );
  }
  
  /// 현재 가격이 추천가 대비 얼마나 비싼지 (%)
  double get priceGapPercent {
    if (recommendedPrice == 0) return 0;
    return ((recentPrice - recommendedPrice) / recommendedPrice) * 100;
  }
  
  /// 가격 상태 (싸다/적정/비싸다)
  String get priceStatus {
    final gap = priceGapPercent;
    if (gap < -10) return '저렴';
    if (gap > 10) return '비쌈';
    return '적정';
  }
}
```

### PriceHistory
```dart
class PriceHistory {
  final int id;
  final String itemName;
  final int unitPrice;
  final String unit;
  final String orderId;
  final String distributorId;
  final String storeId;
  final DateTime recordedAt;

  PriceHistory({
    required this.id,
    required this.itemName,
    required this.unitPrice,
    required this.unit,
    required this.orderId,
    required this.distributorId,
    required this.storeId,
    required this.recordedAt,
  });

  factory PriceHistory.fromJson(Map<String, dynamic> json) {
    return PriceHistory(
      id: json['id'] ?? 0,
      itemName: json['itemName'] ?? '',
      unitPrice: json['unitPrice'] ?? 0,
      unit: json['unit'] ?? '',
      orderId: json['orderId'] ?? '',
      distributorId: json['distributorId'] ?? '',
      storeId: json['storeId'] ?? '',
      recordedAt: DateTime.parse(json['recordedAt']),
    );
  }
}
```

### PriceAlert
```dart
enum AlertType { MODERATE_SURGE, HIGH_SURGE, EXTREME_SURGE }
enum AlertStatus { ACTIVE, ACKNOWLEDGED, RESOLVED }

class PriceAlert {
  final String alertId;
  final String itemName;
  final int currentPrice;
  final int averagePrice;
  final double surgePercentage;
  final AlertType alertType;
  final AlertStatus status;
  final String orderId;
  final String distributorId;
  final String message;
  final DateTime createdAt;
  final DateTime? acknowledgedAt;
  final DateTime? resolvedAt;

  PriceAlert({
    required this.alertId,
    required this.itemName,
    required this.currentPrice,
    required this.averagePrice,
    required this.surgePercentage,
    required this.alertType,
    required this.status,
    required this.orderId,
    required this.distributorId,
    required this.message,
    required this.createdAt,
    this.acknowledgedAt,
    this.resolvedAt,
  });

  factory PriceAlert.fromJson(Map<String, dynamic> json) {
    return PriceAlert(
      alertId: json['alertId'] ?? '',
      itemName: json['itemName'] ?? '',
      currentPrice: json['currentPrice'] ?? 0,
      averagePrice: json['averagePrice'] ?? 0,
      surgePercentage: (json['surgePercentage'] ?? 0).toDouble(),
      alertType: AlertType.values.firstWhere(
        (e) => e.name == json['alertType'],
        orElse: () => AlertType.MODERATE_SURGE,
      ),
      status: AlertStatus.values.firstWhere(
        (e) => e.name == json['status'],
        orElse: () => AlertStatus.ACTIVE,
      ),
      orderId: json['orderId'] ?? '',
      distributorId: json['distributorId'] ?? '',
      message: json['message'] ?? '',
      createdAt: DateTime.parse(json['createdAt']),
      acknowledgedAt: json['acknowledgedAt'] != null 
          ? DateTime.parse(json['acknowledgedAt']) 
          : null,
      resolvedAt: json['resolvedAt'] != null 
          ? DateTime.parse(json['resolvedAt']) 
          : null,
    );
  }
  
  /// 경고 심각도 색상
  Color get severityColor {
    switch (alertType) {
      case AlertType.MODERATE_SURGE:
        return Colors.orange;
      case AlertType.HIGH_SURGE:
        return Colors.deepOrange;
      case AlertType.EXTREME_SURGE:
        return Colors.red;
    }
  }
}
```

---

## 🛠️ Flutter Service 클래스

```dart
import 'dart:convert';
import 'package:http/http.dart' as http;

class PriceLearningService {
  final String baseUrl;
  final String accessToken;

  PriceLearningService({
    required this.baseUrl,
    required this.accessToken,
  });

  Map<String, String> get _headers => {
    'Authorization': 'Bearer $accessToken',
    'Content-Type': 'application/json',
  };

  /// 전체 품목 목록 조회
  Future<List<String>> getItemList() async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/price-learning/items'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      return List<String>.from(jsonDecode(response.body));
    }
    throw Exception('품목 목록 조회 실패: ${response.statusCode}');
  }

  /// 품목별 단가 통계 조회
  Future<PriceStatistics> getStatistics(String itemName, {int days = 30}) async {
    final encodedName = Uri.encodeComponent(itemName);
    final response = await http.get(
      Uri.parse('$baseUrl/api/price-learning/items/$encodedName/statistics?days=$days'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      return PriceStatistics.fromJson(jsonDecode(response.body));
    }
    throw Exception('통계 조회 실패: ${response.statusCode}');
  }

  /// 품목별 단가 이력 조회
  Future<List<PriceHistory>> getPriceHistory(String itemName, {int days = 30}) async {
    final encodedName = Uri.encodeComponent(itemName);
    final response = await http.get(
      Uri.parse('$baseUrl/api/price-learning/items/$encodedName/history?days=$days'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => PriceHistory.fromJson(json)).toList();
    }
    throw Exception('이력 조회 실패: ${response.statusCode}');
  }

  /// 추천 단가 조회
  Future<int> getRecommendedPrice(String itemName) async {
    final encodedName = Uri.encodeComponent(itemName);
    final response = await http.get(
      Uri.parse('$baseUrl/api/price-learning/items/$encodedName/recommended-price'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      return int.parse(response.body);
    }
    throw Exception('추천 단가 조회 실패: ${response.statusCode}');
  }

  /// 활성 경고 목록 조회
  Future<List<PriceAlert>> getActiveAlerts() async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/price-learning/alerts/active'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => PriceAlert.fromJson(json)).toList();
    }
    throw Exception('경고 목록 조회 실패: ${response.statusCode}');
  }

  /// 최근 경고 목록 조회
  Future<List<PriceAlert>> getRecentAlerts() async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/price-learning/alerts/recent'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => PriceAlert.fromJson(json)).toList();
    }
    throw Exception('경고 목록 조회 실패: ${response.statusCode}');
  }

  /// 경고 확인 처리
  Future<void> acknowledgeAlert(String alertId) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/price-learning/alerts/$alertId/acknowledge'),
      headers: _headers,
    );

    if (response.statusCode != 200) {
      throw Exception('경고 확인 처리 실패: ${response.statusCode}');
    }
  }

  /// 경고 해결 처리
  Future<void> resolveAlert(String alertId) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/price-learning/alerts/$alertId/resolve'),
      headers: _headers,
    );

    if (response.statusCode != 200) {
      throw Exception('경고 해결 처리 실패: ${response.statusCode}');
    }
  }
}
```

---

## 📱 UI 구현 예시

### 가격 비교 카드 위젯
```dart
class PriceComparisonCard extends StatelessWidget {
  final PriceStatistics stats;

  const PriceComparisonCard({Key? key, required this.stats}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final gap = stats.priceGapPercent;
    final isExpensive = gap > 10;
    final isCheap = gap < -10;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 품목명
            Text(
              stats.itemName,
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            
            // 현재가 vs 추��가
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('최근 구매가', style: TextStyle(color: Colors.grey)),
                    Text(
                      '${_formatPrice(stats.recentPrice)}원',
                      style: TextStyle(
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                        color: isExpensive ? Colors.red : (isCheap ? Colors.green : Colors.black),
                      ),
                    ),
                  ],
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    const Text('추천가', style: TextStyle(color: Colors.grey)),
                    Text(
                      '${_formatPrice(stats.recommendedPrice)}원',
                      style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w500),
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 12),
            
            // 가격 상태 배지
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: isExpensive ? Colors.red[50] : (isCheap ? Colors.green[50] : Colors.grey[100]),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                isExpensive 
                    ? '평균보다 ${gap.toStringAsFixed(1)}% 비쌈 😢' 
                    : (isCheap 
                        ? '평균보다 ${(-gap).toStringAsFixed(1)}% 저렴 🎉' 
                        : '적정 가격 👍'),
                style: TextStyle(
                  color: isExpensive ? Colors.red : (isCheap ? Colors.green : Colors.grey[700]),
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
            const SizedBox(height: 16),
            
            // 가격 범위
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _buildPriceInfo('최저가', stats.minPrice, Colors.green),
                _buildPriceInfo('평균가', stats.averagePrice, Colors.blue),
                _buildPriceInfo('최고가', stats.maxPrice, Colors.red),
              ],
            ),
            const SizedBox(height: 12),
            
            // 변동성
            Row(
              children: [
                const Icon(Icons.trending_up, size: 16, color: Colors.grey),
                const SizedBox(width: 4),
                Text(
                  '변동성: ${stats.volatilityLevel} (${stats.volatility.toStringAsFixed(1)}%)',
                  style: const TextStyle(color: Colors.grey, fontSize: 12),
                ),
                const SizedBox(width: 8),
                Text(
                  '데이터 ${stats.dataPoints}건',
                  style: const TextStyle(color: Colors.grey, fontSize: 12),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPriceInfo(String label, int price, Color color) {
    return Column(
      children: [
        Text(label, style: const TextStyle(color: Colors.grey, fontSize: 12)),
        Text(
          '${_formatPrice(price)}원',
          style: TextStyle(color: color, fontWeight: FontWeight.w600),
        ),
      ],
    );
  }

  String _formatPrice(int price) {
    return price.toString().replaceAllMapped(
      RegExp(r'(\d{1,3})(?=(\d{3})+(?!\d))'),
      (Match m) => '${m[1]},',
    );
  }
}
```

---

## 🧪 테스트 방법

### cURL로 API 테스트
```bash
# 1. 로그인하여 토큰 발급
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}' | jq -r '.accessToken')

# 2. 품목 목록 조회
curl -X GET http://localhost:8080/api/price-learning/items \
  -H "Authorization: Bearer $TOKEN"

# 3. 양파 통계 조회
curl -X GET "http://localhost:8080/api/price-learning/items/%EC%96%91%ED%8C%8C/statistics?days=30" \
  -H "Authorization: Bearer $TOKEN"

# 4. 양파 이력 조회
curl -X GET "http://localhost:8080/api/price-learning/items/%EC%96%91%ED%8C%8C/history?days=30" \
  -H "Authorization: Bearer $TOKEN"

# 5. 활성 경고 조회
curl -X GET http://localhost:8080/api/price-learning/alerts/active \
  -H "Authorization: Bearer $TOKEN"
```

---

## 💡 화면 구성 제안

### 메인 화면 구성
1. **상단**: 활성 경고 배너 (있을 경우)
2. **품목 선택**: 드롭다운 또는 검색
3. **가격 비교 카드**: 현재가 vs 추천가
4. **가격 추이 차트**: 최근 30일 그래프
5. **상세 이력**: 거래별 단가 리스트

### 추천 패키지
- 차트: `fl_chart` 또는 `syncfusion_flutter_charts`
- 상태관리: `riverpod` 또는 `bloc`
- HTTP: `dio` (인터셉터로 토큰 자동 추가)

---

## ❓ FAQ

**Q: 품목명에 한글이 있으면 어떻게 하나요?**
A: `Uri.encodeComponent()`로 URL 인코딩하세요.

**Q: 데이터가 없는 품목은 어떻게 되나요?**
A: 모든 값이 0으로 반환됩니다. `dataPoints`가 0인지 확인하세요.

**Q: 추천 단가는 어떻게 계산되나요?**
A: 최근 7일 평균 70% + 30일 평균 30% 가중치로 계산됩니다.

---

## 📞 문의

백엔드 관련 문의는 백엔드 개발팀에 연락해주세요.
