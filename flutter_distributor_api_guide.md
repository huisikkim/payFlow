# Flutter 유통업체 정보 API 구현 가이드

## 📍 유통업체 정보 등록 API

### 엔드포인트
```
POST http://localhost:8080/api/distributor/info
```
**실제 기기**: `http://YOUR_IP:8080/api/distributor/info`  
**Android 에뮬레이터**: `http://10.0.2.2:8080/api/distributor/info`

### 🔑 헤더
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

### 📦 요청 Body
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

### ✅ 응답
```json
{
  "id": 1,
  "distributorId": "distributor1",
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
  "address": "서울시 송파구 올림픽로 456",
  "isActive": true,
  "createdAt": "2025-11-25T21:30:00",
  "updatedAt": "2025-11-25T21:30:00"
}
```

---

## 📋 필드 설명

| 필드 | 필수 | 타입 | 설명 | 예시 |
|------|------|------|------|------|
| `distributorName` | ✅ | String | 업체명 | "신선식자재 유통" |
| `supplyProducts` | ✅ | String | 공급 품목 (콤마 구분) | "쌀/곡물,채소,육류" |
| `serviceRegions` | ✅ | String | 서비스 지역 (콤마 구분) | "서울,경기,인천" |
| `deliveryAvailable` | ✅ | Boolean | 배송 가능 여부 | true |
| `phoneNumber` | ✅ | String | 연락처 | "010-9876-5432" |
| `email` | ✅ | String | 이메일 | "dist@example.com" |
| `address` | ✅ | String | 주소 | "서울시 송파구..." |
| `deliveryInfo` | ❌ | String | 배송 정보 | "배송비 무료..." |
| `description` | ❌ | String | 업체 소개 | "신선한 식자재..." |
| `certifications` | ❌ | String | 인증 정보 (콤마 구분) | "HACCP,ISO22000" |
| `minOrderAmount` | ❌ | int | 최소 주문 금액 | 100000 |
| `operatingHours` | ❌ | String | 운영 시간 | "09:00-18:00" |

---

## 🎨 선택 옵션

### 공급 품목 (supplyProducts)
```dart
final supplyProducts = [
  '쌀/곡물', '채소', '과일', '육류', '수산물',
  '유제품', '조미료', '냉동식품', '음료', '주류',
  '가공식품', '베이커리', '건어물', '건강식품'
];
```

### 서비스 지역 (serviceRegions)
```dart
final serviceRegions = [
  '서울', '경기', '인천', '부산', '대구', '광주', '대전',
  '울산', '세종', '강원', '충북', '충남', '전북', '전남',
  '경북', '경남', '제주'
];
```

### 인증 정보 (certifications)
```dart
final certifications = [
  'HACCP',
  'ISO22000',
  '유기농인증',
  'GAP인증',
  'FSSC22000',
  '할랄인증',
  'GMP'
];
```

---

## 💻 Flutter 코드 예제

### 1️⃣ 유통업체 정보 등록 함수
```dart
import 'dart:convert';
import 'package:http/http.dart' as http;

Future<Map<String, dynamic>> registerDistributorInfo({
  required String token,
  required String distributorName,
  required String supplyProducts,
  required String serviceRegions,
  required bool deliveryAvailable,
  String? deliveryInfo,
  String? description,
  String? certifications,
  int? minOrderAmount,
  String? operatingHours,
  required String phoneNumber,
  required String email,
  required String address,
}) async {
  final url = Uri.parse('http://10.0.2.2:8080/api/distributor/info');
  
  final response = await http.post(
    url,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    },
    body: jsonEncode({
      'distributorName': distributorName,
      'supplyProducts': supplyProducts,
      'serviceRegions': serviceRegions,
      'deliveryAvailable': deliveryAvailable,
      if (deliveryInfo != null) 'deliveryInfo': deliveryInfo,
      if (description != null) 'description': description,
      if (certifications != null) 'certifications': certifications,
      if (minOrderAmount != null) 'minOrderAmount': minOrderAmount,
      if (operatingHours != null) 'operatingHours': operatingHours,
      'phoneNumber': phoneNumber,
      'email': email,
      'address': address,
    }),
  );

  if (response.statusCode == 200) {
    return jsonDecode(response.body);
  } else {
    throw Exception('유통업체 정보 등록 실패: ${response.body}');
  }
}
```

### 2️⃣ 내 유통업체 정보 조회
```dart
Future<Map<String, dynamic>?> getMyDistributorInfo(String token) async {
  final url = Uri.parse('http://10.0.2.2:8080/api/distributor/info');
  
  final response = await http.get(
    url,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    },
  );

  if (response.statusCode == 200) {
    return jsonDecode(response.body);
  } else if (response.statusCode == 404) {
    return null; // 유통업체 정보 없음
  } else {
    throw Exception('조회 실패: ${response.body}');
  }
}
```

### 3️⃣ 특정 유통업체 정보 조회 (매장이 유통업체 확인용)
```dart
Future<Map<String, dynamic>> getDistributorInfoById(
  String token,
  String distributorId,
) async {
  final url = Uri.parse('http://10.0.2.2:8080/api/distributor/info/$distributorId');
  
  final response = await http.get(
    url,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    },
  );

  if (response.statusCode == 200) {
    return jsonDecode(response.body);
  } else {
    throw Exception('유통업체를 찾을 수 없습니다');
  }
}
```

### 4️⃣ 유통업체 활성화/비활성화
```dart
Future<String> toggleDistributorStatus(String token, bool activate) async {
  final url = Uri.parse('http://10.0.2.2:8080/api/distributor/status?activate=$activate');
  
  final response = await http.put(
    url,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    },
  );

  if (response.statusCode == 200) {
    return response.body;
  } else {
    throw Exception('상태 변경 실패: ${response.body}');
  }
}
```

---

## 🎨 UI 예제

### 유통업체 정보 등록 화면
```dart
class DistributorInfoRegisterScreen extends StatefulWidget {
  final String token;

  const DistributorInfoRegisterScreen({required this.token});

  @override
  _DistributorInfoRegisterScreenState createState() => _DistributorInfoRegisterScreenState();
}

class _DistributorInfoRegisterScreenState extends State<DistributorInfoRegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  
  final _distributorNameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _emailController = TextEditingController();
  final _addressController = TextEditingController();
  final _deliveryInfoController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _minOrderAmountController = TextEditingController();
  final _operatingHoursController = TextEditingController();

  List<String> _selectedProducts = [];
  List<String> _selectedRegions = [];
  List<String> _selectedCertifications = [];
  bool _deliveryAvailable = true;
  bool _isLoading = false;

  final List<String> _productOptions = [
    '쌀/곡물', '채소', '과일', '육류', '수산물',
    '유제품', '조미료', '냉동식품', '음료', '주류',
  ];

  final List<String> _regionOptions = [
    '서울', '경기', '인천', '부산', '대구', '광주', '대전',
    '울산', '세종', '강원', '충북', '충남', '전북', '전남',
    '경북', '경남', '제주'
  ];

  final List<String> _certificationOptions = [
    'HACCP', 'ISO22000', '유기농인증', 'GAP인증', 'FSSC22000'
  ];

  Future<void> _register() async {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedProducts.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('공급 품목을 선택하세요')),
      );
      return;
    }
    if (_selectedRegions.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('서비스 지역을 선택하세요')),
      );
      return;
    }

    setState(() => _isLoading = true);

    try {
      final result = await registerDistributorInfo(
        token: widget.token,
        distributorName: _distributorNameController.text,
        supplyProducts: _selectedProducts.join(','),
        serviceRegions: _selectedRegions.join(','),
        deliveryAvailable: _deliveryAvailable,
        deliveryInfo: _deliveryInfoController.text,
        description: _descriptionController.text,
        certifications: _selectedCertifications.join(','),
        minOrderAmount: int.tryParse(_minOrderAmountController.text),
        operatingHours: _operatingHoursController.text,
        phoneNumber: _phoneController.text,
        email: _emailController.text,
        address: _addressController.text,
      );

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('유통업체 정보가 등록되었습니다!')),
      );

      Navigator.pop(context, result);
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
      appBar: AppBar(title: Text('유통업체 정보 등록')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: EdgeInsets.all(16),
          children: [
            // 업체명
            TextFormField(
              controller: _distributorNameController,
              decoration: InputDecoration(
                labelText: '업체명 *',
                border: OutlineInputBorder(),
              ),
              validator: (v) => v!.isEmpty ? '업체명을 입력하세요' : null,
            ),
            SizedBox(height: 16),

            // 공급 품목 (다중 선택)
            Text('공급 품목 *', style: TextStyle(fontSize: 16)),
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

            // 서비스 지역 (다중 선택)
            Text('서비스 지역 *', style: TextStyle(fontSize: 16)),
            Wrap(
              spacing: 8,
              children: _regionOptions.map((region) {
                final isSelected = _selectedRegions.contains(region);
                return FilterChip(
                  label: Text(region),
                  selected: isSelected,
                  onSelected: (selected) {
                    setState(() {
                      if (selected) {
                        _selectedRegions.add(region);
                      } else {
                        _selectedRegions.remove(region);
                      }
                    });
                  },
                );
              }).toList(),
            ),
            SizedBox(height: 16),

            // 배송 가능 여부
            SwitchListTile(
              title: Text('배송 가능'),
              value: _deliveryAvailable,
              onChanged: (value) {
                setState(() => _deliveryAvailable = value);
              },
            ),
            SizedBox(height: 16),

            // 배송 정보
            TextFormField(
              controller: _deliveryInfoController,
              decoration: InputDecoration(
                labelText: '배송 정보',
                hintText: '예: 배송비 무료 (10만원 이상), 익일 배송',
                border: OutlineInputBorder(),
              ),
              maxLines: 2,
            ),
            SizedBox(height: 16),

            // 인증 정보
            Text('인증 정보', style: TextStyle(fontSize: 16)),
            Wrap(
              spacing: 8,
              children: _certificationOptions.map((cert) {
                final isSelected = _selectedCertifications.contains(cert);
                return FilterChip(
                  label: Text(cert),
                  selected: isSelected,
                  onSelected: (selected) {
                    setState(() {
                      if (selected) {
                        _selectedCertifications.add(cert);
                      } else {
                        _selectedCertifications.remove(cert);
                      }
                    });
                  },
                );
              }).toList(),
            ),
            SizedBox(height: 16),

            // 최소 주문 금액
            TextFormField(
              controller: _minOrderAmountController,
              decoration: InputDecoration(
                labelText: '최소 주문 금액 (원)',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.number,
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

            // 이메일
            TextFormField(
              controller: _emailController,
              decoration: InputDecoration(
                labelText: '이메일 *',
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.emailAddress,
              validator: (v) => v!.isEmpty ? '이메일을 입력하세요' : null,
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

            // 운영 시간
            TextFormField(
              controller: _operatingHoursController,
              decoration: InputDecoration(
                labelText: '운영 시간 (예: 09:00-18:00)',
                border: OutlineInputBorder(),
              ),
            ),
            SizedBox(height: 16),

            // 업체 소개
            TextFormField(
              controller: _descriptionController,
              decoration: InputDecoration(
                labelText: '업체 소개',
                border: OutlineInputBorder(),
              ),
              maxLines: 3,
            ),
            SizedBox(height: 24),

            // 등록 버튼
            ElevatedButton(
              onPressed: _isLoading ? null : _register,
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

---

## 📌 API 엔드포인트 요약

| 메서드 | 엔드포인트 | 권한 | 설명 |
|--------|-----------|------|------|
| POST | `/api/distributor/info` | DISTRIBUTOR | 유통업체 정보 등록/수정 |
| GET | `/api/distributor/info` | DISTRIBUTOR | 내 유통업체 정보 조회 |
| GET | `/api/distributor/info/{id}` | STORE_OWNER, DISTRIBUTOR | 특정 유통업체 정보 조회 |
| PUT | `/api/distributor/status` | DISTRIBUTOR | 활성화/비활성화 |

---

## 💡 팁

1. **공급 품목/서비스 지역은 콤마로 구분**: `"쌀/곡물,채소,육류"`
2. **같은 API로 수정도 가능**: 다시 POST 요청하면 업데이트
3. **distributorId는 자동**: 로그인한 username이 자동으로 사용됨
4. **매장이 유통업체 정보 조회 가능**: 거래처 확인용
