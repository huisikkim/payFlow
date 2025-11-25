# Flutter 매장 정보 API 구현 가이드

## 1. 모델 클래스 (lib/models/)

### store_info_request.dart
```dart
class StoreInfoRequest {
  final String? storeId;        // null이면 자동으로 username 사용
  final String storeName;
  final String businessType;    // 업종
  final String region;          // 지역
  final String mainProducts;    // 주요 품목 (콤마로 구분)
  final String? description;
  final int? employeeCount;
  final String? operatingHours;
  final String phoneNumber;
  final String address;

  StoreInfoRequest({
    this.storeId,
    required this.storeName,
    required this.businessType,
    required this.region,
    required this.mainProducts,
    this.description,
    this.employeeCount,
    this.operatingHours,
    required this.phoneNumber,
    required this.address,
  });

  Map<String, dynamic> toJson() {
    return {
      if (storeId != null) 'storeId': storeId,
      'storeName': storeName,
      'businessType': businessType,
      'region': region,
      'mainProducts': mainProducts,
      if (description != null) 'description': description,
      if (employeeCount != null) 'employeeCount': employeeCount,
      if (operatingHours != null) 'operatingHours': operatingHours,
      'phoneNumber': phoneNumber,
      'address': address,
    };
  }
}
```

### store_info_response.dart
```dart
class StoreInfoResponse {
  final int id;
  final String storeId;
  final String storeName;
  final String ownerName;
  final String phoneNumber;
  final String address;
  final String? businessNumber;
  final String? businessType;
  final String? region;
  final String? mainProducts;
  final String? description;
  final int? employeeCount;
  final String? operatingHours;
  final bool isActive;
  final DateTime createdAt;
  final DateTime updatedAt;

  StoreInfoResponse({
    required this.id,
    required this.storeId,
    required this.storeName,
    required this.ownerName,
    required this.phoneNumber,
    required this.address,
    this.businessNumber,
    this.businessType,
    this.region,
    this.mainProducts,
    this.description,
    this.employeeCount,
    this.operatingHours,
    required this.isActive,
    required this.createdAt,
    required this.updatedAt,
  });

  factory StoreInfoResponse.fromJson(Map<String, dynamic> json) {
    return StoreInfoResponse(
      id: json['id'],
      storeId: json['storeId'],
      storeName: json['storeName'],
      ownerName: json['ownerName'],
      phoneNumber: json['phoneNumber'],
      address: json['address'],
      businessNumber: json['businessNumber'],
      businessType: json['businessType'],
      region: json['region'],
      mainProducts: json['mainProducts'],
      description: json['description'],
      employeeCount: json['employeeCount'],
      operatingHours: json['operatingHours'],
      isActive: json['isActive'],
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: DateTime.parse(json['updatedAt']),
    );
  }

  // 주요 품목을 리스트로 변환
  List<String> get mainProductsList {
    if (mainProducts == null || mainProducts!.isEmpty) return [];
    return mainProducts!.split(',').map((e) => e.trim()).toList();
  }
}
```

## 2. API 서비스 (lib/services/store_service.dart)

```dart
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/store_info_request.dart';
import '../models/store_info_response.dart';

class StoreService {
  static const String baseUrl = 'http://localhost:8080/api/store';
  // 실제 기기: 'http://YOUR_IP:8080/api/store'
  // Android 에뮬레이터: 'http://10.0.2.2:8080/api/store'

  /// 매장 정보 등록/수정
  Future<StoreInfoResponse> registerStoreInfo(
    String token,
    StoreInfoRequest request,
  ) async {
    final response = await http.post(
      Uri.parse('$baseUrl/info'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      },
      body: jsonEncode(request.toJson()),
    );

    if (response.statusCode == 200) {
      return StoreInfoResponse.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('매장 정보 등록 실패: ${response.body}');
    }
  }

  /// 내 매장 정보 조회
  Future<StoreInfoResponse?> getMyStoreInfo(String token) async {
    final response = await http.get(
      Uri.parse('$baseUrl/info'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      },
    );

    if (response.statusCode == 200) {
      return StoreInfoResponse.fromJson(jsonDecode(response.body));
    } else if (response.statusCode == 404) {
      return null; // 매장 정보 없음
    } else {
      throw Exception('매장 정보 조회 실패: ${response.body}');
    }
  }

  /// 특정 매장 정보 조회 (storeId로)
  Future<StoreInfoResponse> getStoreInfoById(
    String token,
    String storeId,
  ) async {
    final response = await http.get(
      Uri.parse('$baseUrl/info/$storeId'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      },
    );

    if (response.statusCode == 200) {
      return StoreInfoResponse.fromJson(jsonDecode(response.body));
    } else if (response.statusCode == 404) {
      throw Exception('매장을 찾을 수 없습니다');
    } else {
      throw Exception('매장 정보 조회 실패: ${response.body}');
    }
  }

  /// 매장 활성화/비활성화
  Future<String> toggleStoreStatus(String token, bool activate) async {
    final response = await http.put(
      Uri.parse('$baseUrl/status?activate=$activate'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      },
    );

    if (response.statusCode == 200) {
      return response.body;
    } else {
      throw Exception('매장 상태 변경 실패: ${response.body}');
    }
  }
}
```

## 3. 사용 예제

### 매장 정보 등록 화면
```dart
import 'package:flutter/material.dart';

class StoreInfoRegisterScreen extends StatefulWidget {
  final String token;

  const StoreInfoRegisterScreen({required this.token});

  @override
  _StoreInfoRegisterScreenState createState() => _StoreInfoRegisterScreenState();
}

class _StoreInfoRegisterScreenState extends State<StoreInfoRegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final _storeService = StoreService();

  final _storeNameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _addressController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _employeeCountController = TextEditingController();
  final _operatingHoursController = TextEditingController();

  String? _selectedBusinessType;
  String? _selectedRegion;
  List<String> _selectedProducts = [];

  bool _isLoading = false;

  // 업종 목록
  final List<String> _businessTypes = [
    '한식',
    '중식',
    '일식',
    '양식',
    '카페',
    '베이커리',
    '분식',
    '치킨',
    '피자',
    '패스트푸드',
    '기타',
  ];

  // 지역 목록
  final List<String> _regions = [
    '서울 강남구',
    '서울 강북구',
    '서울 송파구',
    '서울 마포구',
    '부산 해운대구',
    '부산 남구',
    '인천 남동구',
    '대구 중구',
    '광주 서구',
    '대전 유성구',
  ];

  // 주요 품목 목록
  final List<String> _productOptions = [
    '쌀/곡물',
    '채소',
    '과일',
    '육류',
    '수산물',
    '유제품',
    '조미료',
    '냉동식품',
    '음료',
    '주류',
  ];

  Future<void> _registerStore() async {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedBusinessType == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('업종을 선택하세요')),
      );
      return;
    }
    if (_selectedRegion == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('지역을 선택하세요')),
      );
      return;
    }
    if (_selectedProducts.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('주요 품목을 선택하세요')),
      );
      return;
    }

    setState(() => _isLoading = true);

    try {
      final request = StoreInfoRequest(
        storeName: _storeNameController.text,
        businessType: _selectedBusinessType!,
        region: _selectedRegion!,
        mainProducts: _selectedProducts.join(','),
        description: _descriptionController.text,
        employeeCount: int.tryParse(_employeeCountController.text),
        operatingHours: _operatingHoursController.text,
        phoneNumber: _phoneController.text,
        address: _addressController.text,
      );

      final response = await _storeService.registerStoreInfo(
        widget.token,
        request,
      );

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('매장 정보가 등록되었습니다!')),
      );

      Navigator.pop(context, response);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('오류: $e')),
      );
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('매장 정보 등록')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: EdgeInsets.all(16),
          children: [
            // 매장명
            TextFormField(
              controller: _storeNameController,
              decoration: InputDecoration(
                labelText: '매장명 *',
                border: OutlineInputBorder(),
              ),
              validator: (v) => v!.isEmpty ? '매장명을 입력하세요' : null,
            ),
            SizedBox(height: 16),

            // 업종 선택
            DropdownButtonFormField<String>(
              value: _selectedBusinessType,
              decoration: InputDecoration(
                labelText: '업종 *',
                border: OutlineInputBorder(),
              ),
              items: _businessTypes.map((type) {
                return DropdownMenuItem(value: type, child: Text(type));
              }).toList(),
              onChanged: (value) {
                setState(() => _selectedBusinessType = value);
              },
            ),
            SizedBox(height: 16),

            // 지역 선택
            DropdownButtonFormField<String>(
              value: _selectedRegion,
              decoration: InputDecoration(
                labelText: '지역 *',
                border: OutlineInputBorder(),
              ),
              items: _regions.map((region) {
                return DropdownMenuItem(value: region, child: Text(region));
              }).toList(),
              onChanged: (value) {
                setState(() => _selectedRegion = value);
              },
            ),
            SizedBox(height: 16),

            // 주요 품목 (다중 선택)
            Text('주요 품목 *', style: TextStyle(fontSize: 16)),
            Wrap(
              spacing: 8,
              children: _productOptions.map((product) {
                final isSelected = _selectedProducts.contains(product);
                return FilterChip(
                  label: Text(product),
                  selected: isSelected,
                  onSelected: (selected) {
                    setState(() {
                      if (selected) {
                        _selectedProducts.add(product);
                      } else {
                        _selectedProducts.remove(product);
                      }
                    });
                  },
                );
              }).toList(),
            ),
            SizedBox(height: 16),

            // 연락처
            TextFormField(
              controller: _phoneController,
              decoration: InputDecoration(
                labelText: '연락처 *',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.phone,
              validator: (v) => v!.isEmpty ? '연락처를 입력하세요' : null,
            ),
            SizedBox(height: 16),

            // 주소
            TextFormField(
              controller: _addressController,
              decoration: InputDecoration(
                labelText: '주소 *',
                border: OutlineInputBorder(),
              ),
              validator: (v) => v!.isEmpty ? '주소를 입력하세요' : null,
            ),
            SizedBox(height: 16),

            // 직원 수
            TextFormField(
              controller: _employeeCountController,
              decoration: InputDecoration(
                labelText: '직원 수',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.number,
            ),
            SizedBox(height: 16),

            // 영업시간
            TextFormField(
              controller: _operatingHoursController,
              decoration: InputDecoration(
                labelText: '영업시간 (예: 09:00-22:00)',
                border: OutlineInputBorder(),
              ),
            ),
            SizedBox(height: 16),

            // 매장 설명
            TextFormField(
              controller: _descriptionController,
              decoration: InputDecoration(
                labelText: '매장 설명',
                border: OutlineInputBorder(),
              ),
              maxLines: 3,
            ),
            SizedBox(height: 24),

            // 등록 버튼
            ElevatedButton(
              onPressed: _isLoading ? null : _registerStore,
              style: ElevatedButton.styleFrom(
                padding: EdgeInsets.symmetric(vertical: 16),
              ),
              child: _isLoading
                  ? CircularProgressIndicator()
                  : Text('등록하기', style: TextStyle(fontSize: 16)),
            ),
          ],
        ),
      ),
    );
  }
}
```

### 매장 정보 조회 화면
```dart
class StoreInfoScreen extends StatefulWidget {
  final String token;

  const StoreInfoScreen({required this.token});

  @override
  _StoreInfoScreenState createState() => _StoreInfoScreenState();
}

class _StoreInfoScreenState extends State<StoreInfoScreen> {
  final _storeService = StoreService();
  StoreInfoResponse? _storeInfo;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadStoreInfo();
  }

  Future<void> _loadStoreInfo() async {
    setState(() => _isLoading = true);
    try {
      final info = await _storeService.getMyStoreInfo(widget.token);
      setState(() {
        _storeInfo = info;
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
    if (_isLoading) {
      return Scaffold(
        appBar: AppBar(title: Text('매장 정보')),
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (_storeInfo == null) {
      return Scaffold(
        appBar: AppBar(title: Text('매장 정보')),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text('등록된 매장 정보가 없습니다'),
              SizedBox(height: 16),
              ElevatedButton(
                onPressed: () {
                  // 매장 등록 화면으로 이동
                },
                child: Text('매장 정보 등록하기'),
              ),
            ],
          ),
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: Text('매장 정보'),
        actions: [
          IconButton(
            icon: Icon(Icons.edit),
            onPressed: () {
              // 수정 화면으로 이동
            },
          ),
        ],
      ),
      body: ListView(
        padding: EdgeInsets.all(16),
        children: [
          _buildInfoCard('매장명', _storeInfo!.storeName),
          _buildInfoCard('업종', _storeInfo!.businessType ?? '-'),
          _buildInfoCard('지역', _storeInfo!.region ?? '-'),
          _buildInfoCard('주요 품목', _storeInfo!.mainProducts ?? '-'),
          _buildInfoCard('연락처', _storeInfo!.phoneNumber),
          _buildInfoCard('주소', _storeInfo!.address),
          _buildInfoCard('직원 수', '${_storeInfo!.employeeCount ?? '-'}명'),
          _buildInfoCard('영업시간', _storeInfo!.operatingHours ?? '-'),
          _buildInfoCard('매장 설명', _storeInfo!.description ?? '-'),
          _buildInfoCard(
            '상태',
            _storeInfo!.isActive ? '활성화' : '비활성화',
          ),
        ],
      ),
    );
  }

  Widget _buildInfoCard(String label, String value) {
    return Card(
      margin: EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              label,
              style: TextStyle(
                fontSize: 12,
                color: Colors.grey[600],
              ),
            ),
            SizedBox(height: 4),
            Text(
              value,
              style: TextStyle(fontSize: 16),
            ),
          ],
        ),
      ),
    );
  }
}
```

## 4. API 엔드포인트 요약

### 매장 정보 등록/수정
- **URL**: `POST /api/store/info`
- **Headers**: `Authorization: Bearer {token}`
- **Body**:
```json
{
  "storeName": "맛있는 식당",
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

### 내 매장 정보 조회
- **URL**: `GET /api/store/info`
- **Headers**: `Authorization: Bearer {token}`

### 특정 매장 정보 조회
- **URL**: `GET /api/store/info/{storeId}`
- **Headers**: `Authorization: Bearer {token}`

### 매장 활성화/비활성화
- **URL**: `PUT /api/store/status?activate=true`
- **Headers**: `Authorization: Bearer {token}`

## 5. 주의사항

1. **권한**: 매장 정보 등록/수정은 `ROLE_STORE_OWNER`만 가능
2. **storeId**: 생략 시 자동으로 로그인한 username 사용
3. **주요 품목**: 콤마(,)로 구분하여 저장
4. **지역/업종**: 드롭다운으로 선택하도록 구현 권장
