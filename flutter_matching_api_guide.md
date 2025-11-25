# Flutter 유통업체 매칭 및 추천 API 가이드

## 🎯 개요

매장 정보(지역, 필요 품목)를 기반으로 최적의 유통업체를 자동으로 추천하는 AI 매칭 시스템입니다.

### 매칭 알고리즘

**총점 계산 (0-100점)**
- 지역 매칭: 40% (서비스 지역 일치도)
- 품목 매칭: 35% (필요 품목 공급 가능 여부)
- 배송 서비스: 15% (배송 가능 여부 및 정보)
- 인증 정보: 10% (HACCP, ISO 등 인증 보유)

---

## 📍 API 엔드포인트

### 1️⃣ 맞춤 유통업체 추천
```
GET http://localhost:8080/api/matching/recommend?limit=10
```

**설명**: 매장 정보를 기반으로 가장 적합한 유통업체를 추천합니다.

**파라미터**:
- `limit` (선택): 추천 개수 (기본값: 10)

**응답 예시**:
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
    "supplyProducts": "쌀/곡물,채소,과일,육류,수산물,유제품",
    "serviceRegions": "서울,경기,인천,충남,충북",
    "deliveryAvailable": true,
    "deliveryInfo": "배송비 무료 (10만원 이상), 당일/익일 배송 가능",
    "certifications": "HACCP,ISO22000,유기농인증",
    "minOrderAmount": 100000,
    "phoneNumber": "010-9876-5432",
    "email": "distributor1@example.com"
  }
]
```

---

### 2️⃣ 품목별 유통업체 검색
```
GET http://localhost:8080/api/matching/search/product?keyword=쌀
```

**설명**: 특정 품목을 공급하는 유통업체를 검색합니다.

**파라미터**:
- `keyword` (필수): 검색할 품목 (예: "쌀", "채소", "육류")

---

### 3️⃣ 지역별 유통업체 검색
```
GET http://localhost:8080/api/matching/search/region?keyword=서울
```

**설명**: 특정 지역에 서비스하는 유통업체를 검색합니다.

**파라미터**:
- `keyword` (필수): 검색할 지역 (예: "서울", "경기", "부산")

---

## 💻 Flutter 코드 예제

### 1️⃣ 맞춤 유통업체 추천
```dart
import 'dart:convert';
import 'package:http/http.dart' as http;

class MatchingScore {
  final String distributorId;
  final String distributorName;
  final double totalScore;
  final double regionScore;
  final double productScore;
  final double deliveryScore;
  final double certificationScore;
  final String matchReason;
  final String supplyProducts;
  final String serviceRegions;
  final bool deliveryAvailable;
  final String? deliveryInfo;
  final String? certifications;
  final int? minOrderAmount;
  final String phoneNumber;
  final String email;

  MatchingScore({
    required this.distributorId,
    required this.distributorName,
    required this.totalScore,
    required this.regionScore,
    required this.productScore,
    required this.deliveryScore,
    required this.certificationScore,
    required this.matchReason,
    required this.supplyProducts,
    required this.serviceRegions,
    required this.deliveryAvailable,
    this.deliveryInfo,
    this.certifications,
    this.minOrderAmount,
    required this.phoneNumber,
    required this.email,
  });

  factory MatchingScore.fromJson(Map<String, dynamic> json) {
    return MatchingScore(
      distributorId: json['distributorId'],
      distributorName: json['distributorName'],
      totalScore: (json['totalScore'] as num).toDouble(),
      regionScore: (json['regionScore'] as num).toDouble(),
      productScore: (json['productScore'] as num).toDouble(),
      deliveryScore: (json['deliveryScore'] as num).toDouble(),
      certificationScore: (json['certificationScore'] as num).toDouble(),
      matchReason: json['matchReason'],
      supplyProducts: json['supplyProducts'],
      serviceRegions: json['serviceRegions'],
      deliveryAvailable: json['deliveryAvailable'],
      deliveryInfo: json['deliveryInfo'],
      certifications: json['certifications'],
      minOrderAmount: json['minOrderAmount'],
      phoneNumber: json['phoneNumber'],
      email: json['email'],
    );
  }

  // 공급 품목 리스트로 변환
  List<String> get supplyProductsList {
    return supplyProducts.split(',').map((e) => e.trim()).toList();
  }

  // 서비스 지역 리스트로 변환
  List<String> get serviceRegionsList {
    return serviceRegions.split(',').map((e) => e.trim()).toList();
  }

  // 인증 리스트로 변환
  List<String> get certificationsList {
    if (certifications == null || certifications!.isEmpty) return [];
    return certifications!.split(',').map((e) => e.trim()).toList();
  }
}

// 맞춤 유통업체 추천
Future<List<MatchingScore>> getRecommendedDistributors(
  String token, {
  int limit = 10,
}) async {
  final url = Uri.parse('http://10.0.2.2:8080/api/matching/recommend?limit=$limit');
  
  final response = await http.get(
    url,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    },
  );

  if (response.statusCode == 200) {
    List<dynamic> jsonList = jsonDecode(response.body);
    return jsonList.map((json) => MatchingScore.fromJson(json)).toList();
  } else {
    throw Exception('추천 조회 실패: ${response.body}');
  }
}

// 품목별 검색
Future<List<MatchingScore>> searchDistributorsByProduct(
  String token,
  String keyword,
) async {
  final url = Uri.parse('http://10.0.2.2:8080/api/matching/search/product?keyword=$keyword');
  
  final response = await http.get(
    url,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    },
  );

  if (response.statusCode == 200) {
    List<dynamic> jsonList = jsonDecode(response.body);
    return jsonList.map((json) => MatchingScore.fromJson(json)).toList();
  } else {
    throw Exception('검색 실패: ${response.body}');
  }
}

// 지역별 검색
Future<List<MatchingScore>> searchDistributorsByRegion(
  String token,
  String keyword,
) async {
  final url = Uri.parse('http://10.0.2.2:8080/api/matching/search/region?keyword=$keyword');
  
  final response = await http.get(
    url,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    },
  );

  if (response.statusCode == 200) {
    List<dynamic> jsonList = jsonDecode(response.body);
    return jsonList.map((json) => MatchingScore.fromJson(json)).toList();
  } else {
    throw Exception('검색 실패: ${response.body}');
  }
}
```

---

## 🎨 UI 예제

### 추천 유통업체 화면
```dart
class RecommendedDistributorsScreen extends StatefulWidget {
  final String token;

  const RecommendedDistributorsScreen({required this.token});

  @override
  _RecommendedDistributorsScreenState createState() => _RecommendedDistributorsScreenState();
}

class _RecommendedDistributorsScreenState extends State<RecommendedDistributorsScreen> {
  List<MatchingScore> _recommendations = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadRecommendations();
  }

  Future<void> _loadRecommendations() async {
    setState(() => _isLoading = true);
    try {
      final recommendations = await getRecommendedDistributors(widget.token, limit: 10);
      setState(() {
        _recommendations = recommendations;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('오류: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('추천 유통업체'),
        actions: [
          IconButton(
            icon: Icon(Icons.refresh),
            onPressed: _loadRecommendations,
          ),
        ],
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : _recommendations.isEmpty
              ? Center(child: Text('추천 유통업체가 없습니다'))
              : ListView.builder(
                  padding: EdgeInsets.all(16),
                  itemCount: _recommendations.length,
                  itemBuilder: (context, index) {
                    final match = _recommendations[index];
                    return _buildDistributorCard(match);
                  },
                ),
    );
  }

  Widget _buildDistributorCard(MatchingScore match) {
    return Card(
      margin: EdgeInsets.only(bottom: 16),
      elevation: 2,
      child: InkWell(
        onTap: () {
          // 상세 화면으로 이동
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => DistributorDetailScreen(
                token: widget.token,
                distributorId: match.distributorId,
              ),
            ),
          );
        },
        child: Padding(
          padding: EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 업체명 및 매칭 점수
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(
                    child: Text(
                      match.distributorName,
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  Container(
                    padding: EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: _getScoreColor(match.totalScore),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      '${match.totalScore.toStringAsFixed(0)}점',
                      style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),
              SizedBox(height: 8),

              // 추천 이유
              Container(
                padding: EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: Colors.blue[50],
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  children: [
                    Icon(Icons.lightbulb, size: 16, color: Colors.blue[700]),
                    SizedBox(width: 4),
                    Expanded(
                      child: Text(
                        match.matchReason,
                        style: TextStyle(
                          fontSize: 12,
                          color: Colors.blue[700],
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              SizedBox(height: 12),

              // 상세 점수
              Row(
                children: [
                  _buildScoreChip('지역', match.regionScore),
                  SizedBox(width: 8),
                  _buildScoreChip('품목', match.productScore),
                  SizedBox(width: 8),
                  _buildScoreChip('배송', match.deliveryScore),
                  SizedBox(width: 8),
                  _buildScoreChip('인증', match.certificationScore),
                ],
              ),
              SizedBox(height: 12),

              // 공급 품목
              _buildInfoRow(
                Icons.inventory_2,
                '공급 품목',
                match.supplyProductsList.take(3).join(', ') +
                    (match.supplyProductsList.length > 3 ? ' 외' : ''),
              ),
              SizedBox(height: 8),

              // 서비스 지역
              _buildInfoRow(
                Icons.location_on,
                '서비스 지역',
                match.serviceRegionsList.take(3).join(', ') +
                    (match.serviceRegionsList.length > 3 ? ' 외' : ''),
              ),
              SizedBox(height: 8),

              // 배송 정보
              if (match.deliveryAvailable)
                _buildInfoRow(
                  Icons.local_shipping,
                  '배송',
                  match.deliveryInfo ?? '배송 가능',
                ),
              SizedBox(height: 8),

              // 최소 주문 금액
              if (match.minOrderAmount != null)
                _buildInfoRow(
                  Icons.attach_money,
                  '최소 주문',
                  '${_formatNumber(match.minOrderAmount!)}원',
                ),
              SizedBox(height: 12),

              // 연락처 버튼
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      icon: Icon(Icons.phone, size: 18),
                      label: Text('전화'),
                      onPressed: () {
                        // 전화 걸기
                      },
                    ),
                  ),
                  SizedBox(width: 8),
                  Expanded(
                    child: OutlinedButton.icon(
                      icon: Icon(Icons.email, size: 18),
                      label: Text('이메일'),
                      onPressed: () {
                        // 이메일 보내기
                      },
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildScoreChip(String label, double score) {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.grey[200],
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        '$label ${score.toStringAsFixed(0)}',
        style: TextStyle(fontSize: 11),
      ),
    );
  }

  Widget _buildInfoRow(IconData icon, String label, String value) {
    return Row(
      children: [
        Icon(icon, size: 16, color: Colors.grey[600]),
        SizedBox(width: 4),
        Text(
          '$label: ',
          style: TextStyle(
            fontSize: 12,
            color: Colors.grey[600],
          ),
        ),
        Expanded(
          child: Text(
            value,
            style: TextStyle(fontSize: 12),
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ],
    );
  }

  Color _getScoreColor(double score) {
    if (score >= 80) return Colors.green;
    if (score >= 60) return Colors.orange;
    return Colors.red;
  }

  String _formatNumber(int number) {
    return number.toString().replaceAllMapped(
          RegExp(r'(\d{1,3})(?=(\d{3})+(?!\d))'),
          (Match m) => '${m[1]},',
        );
  }
}
```

### 검색 화면
```dart
class DistributorSearchScreen extends StatefulWidget {
  final String token;

  const DistributorSearchScreen({required this.token});

  @override
  _DistributorSearchScreenState createState() => _DistributorSearchScreenState();
}

class _DistributorSearchScreenState extends State<DistributorSearchScreen> {
  final _searchController = TextEditingController();
  List<MatchingScore> _searchResults = [];
  bool _isLoading = false;
  String _searchType = 'product'; // 'product' or 'region'

  Future<void> _search() async {
    if (_searchController.text.isEmpty) return;

    setState(() => _isLoading = true);
    try {
      List<MatchingScore> results;
      if (_searchType == 'product') {
        results = await searchDistributorsByProduct(
          widget.token,
          _searchController.text,
        );
      } else {
        results = await searchDistributorsByRegion(
          widget.token,
          _searchController.text,
        );
      }
      setState(() {
        _searchResults = results;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('검색 실패: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('유통업체 검색')),
      body: Column(
        children: [
          Padding(
            padding: EdgeInsets.all(16),
            child: Column(
              children: [
                // 검색 타입 선택
                Row(
                  children: [
                    Expanded(
                      child: RadioListTile<String>(
                        title: Text('품목'),
                        value: 'product',
                        groupValue: _searchType,
                        onChanged: (value) {
                          setState(() => _searchType = value!);
                        },
                      ),
                    ),
                    Expanded(
                      child: RadioListTile<String>(
                        title: Text('지역'),
                        value: 'region',
                        groupValue: _searchType,
                        onChanged: (value) {
                          setState(() => _searchType = value!);
                        },
                      ),
                    ),
                  ],
                ),
                // 검색창
                TextField(
                  controller: _searchController,
                  decoration: InputDecoration(
                    hintText: _searchType == 'product' ? '품목 검색 (예: 쌀, 채소)' : '지역 검색 (예: 서울, 경기)',
                    border: OutlineInputBorder(),
                    suffixIcon: IconButton(
                      icon: Icon(Icons.search),
                      onPressed: _search,
                    ),
                  ),
                  onSubmitted: (_) => _search(),
                ),
              ],
            ),
          ),
          Expanded(
            child: _isLoading
                ? Center(child: CircularProgressIndicator())
                : _searchResults.isEmpty
                    ? Center(child: Text('검색 결과가 없습니다'))
                    : ListView.builder(
                        padding: EdgeInsets.all(16),
                        itemCount: _searchResults.length,
                        itemBuilder: (context, index) {
                          return _buildResultCard(_searchResults[index]);
                        },
                      ),
          ),
        ],
      ),
    );
  }

  Widget _buildResultCard(MatchingScore match) {
    // 위의 _buildDistributorCard와 동일
    return Card(
      margin: EdgeInsets.only(bottom: 16),
      child: ListTile(
        title: Text(match.distributorName),
        subtitle: Text('매칭 점수: ${match.totalScore.toStringAsFixed(0)}점'),
        trailing: Icon(Icons.arrow_forward_ios),
        onTap: () {
          // 상세 화면으로 이동
        },
      ),
    );
  }
}
```

---

## 💡 사용 시나리오

### 1. 매장 등록 후 자동 추천
```dart
// 매장 정보 등록 완료 후
final recommendations = await getRecommendedDistributors(token, limit: 5);
// 상위 5개 유통업체 표시
```

### 2. 특정 품목 필요 시
```dart
// "쌀이 필요해요" 버튼 클릭
final results = await searchDistributorsByProduct(token, '쌀');
// 쌀을 공급하는 유통업체 목록 표시
```

### 3. 지역 기반 검색
```dart
// "우리 지역 유통업체" 버튼 클릭
final results = await searchDistributorsByRegion(token, '서울');
// 서울 지역 서비스 유통업체 목록 표시
```

---

## 📊 매칭 점수 해석

| 점수 | 등급 | 의미 |
|------|------|------|
| 80-100 | 최우수 | 매우 적합한 유통업체 |
| 60-79 | 우수 | 적합한 유통업체 |
| 40-59 | 보통 | 고려 가능한 유통업체 |
| 0-39 | 낮음 | 매칭도가 낮음 |

---

## 🎯 추천 알고리즘 상세

### 지역 매칭 (40%)
- 정확히 일치: 100점
- 부분 일치 (같은 시/도): 70점
- 불일치: 0점

### 품목 매칭 (35%)
- 필요 품목 중 공급 가능한 비율로 계산
- 예: 5개 중 4개 공급 가능 = 80점

### 배송 서비스 (15%)
- 배송 가능 + 상세 정보: 100점
- 배송 가능: 70점
- 배송 불가: 0점

### 인증 정보 (10%)
- 3개 이상: 100점
- 2개: 85점
- 1개: 70점
- 없음: 50점
