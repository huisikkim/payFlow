# Flutter 매장/유통업체 인증 API 구현 가이드

## 1. 모델 클래스 (lib/models/)

### user_type.dart
```dart
enum UserType {
  STORE_OWNER,  // 매장 사장님
  DISTRIBUTOR   // 유통업체
}
```

### signup_request.dart
```dart
class SignupRequest {
  final String username;
  final String password;
  final String email;
  final UserType userType;
  final String businessNumber;
  final String businessName;
  final String ownerName;
  final String phoneNumber;
  final String address;

  SignupRequest({
    required this.username,
    required this.password,
    required this.email,
    required this.userType,
    required this.businessNumber,
    required this.businessName,
    required this.ownerName,
    required this.phoneNumber,
    required this.address,
  });

  Map<String, dynamic> toJson() {
    return {
      'username': username,
      'password': password,
      'email': email,
      'userType': userType.name,
      'businessNumber': businessNumber,
      'businessName': businessName,
      'ownerName': ownerName,
      'phoneNumber': phoneNumber,
      'address': address,
    };
  }
}
```

### login_request.dart
```dart
class LoginRequest {
  final String username;
  final String password;

  LoginRequest({
    required this.username,
    required this.password,
  });

  Map<String, dynamic> toJson() {
    return {
      'username': username,
      'password': password,
    };
  }
}
```

### login_response.dart
```dart
class LoginResponse {
  final String accessToken;
  final String tokenType;
  final String username;
  final int userId;
  final UserType userType;
  final String businessName;

  LoginResponse({
    required this.accessToken,
    required this.tokenType,
    required this.username,
    required this.userId,
    required this.userType,
    required this.businessName,
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      accessToken: json['accessToken'],
      tokenType: json['tokenType'],
      username: json['username'],
      userId: json['userId'],
      userType: UserType.values.firstWhere(
        (e) => e.name == json['userType'],
      ),
      businessName: json['businessName'],
    );
  }
}
```

## 2. API 서비스 (lib/services/auth_service.dart)

```dart
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/signup_request.dart';
import '../models/login_request.dart';
import '../models/login_response.dart';

class AuthService {
  static const String baseUrl = 'http://localhost:8080/api/auth';
  // 실제 기기 테스트 시: 'http://YOUR_IP:8080/api/auth'
  // Android 에뮬레이터: 'http://10.0.2.2:8080/api/auth'

  // 회원가입
  Future<String> signup(SignupRequest request) async {
    final response = await http.post(
      Uri.parse('$baseUrl/signup'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(request.toJson()),
    );

    if (response.statusCode == 200) {
      return response.body; // "User registered successfully"
    } else {
      throw Exception('회원가입 실패: ${response.body}');
    }
  }

  // 로그인
  Future<LoginResponse> login(LoginRequest request) async {
    final response = await http.post(
      Uri.parse('$baseUrl/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(request.toJson()),
    );

    if (response.statusCode == 200) {
      return LoginResponse.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('로그인 실패: ${response.body}');
    }
  }

  // 프로필 조회 (토큰 필요)
  Future<Map<String, dynamic>> getUserProfile(String token) async {
    final response = await http.get(
      Uri.parse('http://localhost:8080/api/user/profile'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      },
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('프로필 조회 실패: ${response.body}');
    }
  }
}
```

## 3. 사용 예제

### 매장 사장님 회원가입 화면
```dart
import 'package:flutter/material.dart';

class StoreOwnerSignupScreen extends StatefulWidget {
  @override
  _StoreOwnerSignupScreenState createState() => _StoreOwnerSignupScreenState();
}

class _StoreOwnerSignupScreenState extends State<StoreOwnerSignupScreen> {
  final _formKey = GlobalKey<FormState>();
  final _authService = AuthService();
  
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  final _emailController = TextEditingController();
  final _businessNumberController = TextEditingController();
  final _businessNameController = TextEditingController();
  final _ownerNameController = TextEditingController();
  final _phoneNumberController = TextEditingController();
  final _addressController = TextEditingController();

  bool _isLoading = false;

  Future<void> _signup() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    try {
      final request = SignupRequest(
        username: _usernameController.text,
        password: _passwordController.text,
        email: _emailController.text,
        userType: UserType.STORE_OWNER,
        businessNumber: _businessNumberController.text,
        businessName: _businessNameController.text,
        ownerName: _ownerNameController.text,
        phoneNumber: _phoneNumberController.text,
        address: _addressController.text,
      );

      final result = await _authService.signup(request);
      
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('회원가입 성공! $result')),
      );
      
      Navigator.pop(context);
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
      appBar: AppBar(title: Text('매장 사장님 회원가입')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: EdgeInsets.all(16),
          children: [
            TextFormField(
              controller: _usernameController,
              decoration: InputDecoration(labelText: '아이디'),
              validator: (v) => v!.isEmpty ? '아이디를 입력하세요' : null,
            ),
            TextFormField(
              controller: _passwordController,
              decoration: InputDecoration(labelText: '비밀번호'),
              obscureText: true,
              validator: (v) => v!.isEmpty ? '비밀번호를 입력하세요' : null,
            ),
            TextFormField(
              controller: _emailController,
              decoration: InputDecoration(labelText: '이메일'),
              validator: (v) => v!.isEmpty ? '이메일을 입력하세요' : null,
            ),
            TextFormField(
              controller: _businessNumberController,
              decoration: InputDecoration(labelText: '사업자등록번호'),
              validator: (v) => v!.isEmpty ? '사업자등록번호를 입력하세요' : null,
            ),
            TextFormField(
              controller: _businessNameController,
              decoration: InputDecoration(labelText: '상호명'),
              validator: (v) => v!.isEmpty ? '상호명을 입력하세요' : null,
            ),
            TextFormField(
              controller: _ownerNameController,
              decoration: InputDecoration(labelText: '대표자명'),
              validator: (v) => v!.isEmpty ? '대표자명을 입력하세요' : null,
            ),
            TextFormField(
              controller: _phoneNumberController,
              decoration: InputDecoration(labelText: '연락처'),
              validator: (v) => v!.isEmpty ? '연락처를 입력하세요' : null,
            ),
            TextFormField(
              controller: _addressController,
              decoration: InputDecoration(labelText: '주소'),
              validator: (v) => v!.isEmpty ? '주소를 입력하세요' : null,
            ),
            SizedBox(height: 20),
            ElevatedButton(
              onPressed: _isLoading ? null : _signup,
              child: _isLoading
                  ? CircularProgressIndicator()
                  : Text('회원가입'),
            ),
          ],
        ),
      ),
    );
  }
}
```

### 로그인 화면
```dart
class LoginScreen extends StatefulWidget {
  @override
  _LoginScreenState createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _authService = AuthService();
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _isLoading = false;

  Future<void> _login() async {
    setState(() => _isLoading = true);

    try {
      final request = LoginRequest(
        username: _usernameController.text,
        password: _passwordController.text,
      );

      final response = await _authService.login(request);
      
      // 토큰 저장 (SharedPreferences 사용 권장)
      // await SharedPreferences.getInstance().then((prefs) {
      //   prefs.setString('token', response.accessToken);
      //   prefs.setString('userType', response.userType.name);
      // });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            '로그인 성공!\n'
            '상호명: ${response.businessName}\n'
            '회원유형: ${response.userType.name}'
          ),
        ),
      );

      // 회원 유형에 따라 다른 화면으로 이동
      if (response.userType == UserType.STORE_OWNER) {
        // Navigator.pushReplacement(context, StoreOwnerHomeScreen());
      } else {
        // Navigator.pushReplacement(context, DistributorHomeScreen());
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('로그인 실패: $e')),
      );
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('로그인')),
      body: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            TextField(
              controller: _usernameController,
              decoration: InputDecoration(labelText: '아이디'),
            ),
            TextField(
              controller: _passwordController,
              decoration: InputDecoration(labelText: '비밀번호'),
              obscureText: true,
            ),
            SizedBox(height: 20),
            ElevatedButton(
              onPressed: _isLoading ? null : _login,
              child: _isLoading
                  ? CircularProgressIndicator()
                  : Text('로그인'),
            ),
            TextButton(
              onPressed: () {
                // 회원가입 화면으로 이동
              },
              child: Text('회원가입'),
            ),
          ],
        ),
      ),
    );
  }
}
```

## 4. pubspec.yaml 의존성 추가

```yaml
dependencies:
  flutter:
    sdk: flutter
  http: ^1.1.0
  shared_preferences: ^2.2.2  # 토큰 저장용
```

## 5. API 호출 요약

### 회원가입 API
- **URL**: `POST http://localhost:8080/api/auth/signup`
- **Body**:
```json
{
  "username": "store_owner1",
  "password": "password123",
  "email": "store1@example.com",
  "userType": "STORE_OWNER",
  "businessNumber": "123-45-67890",
  "businessName": "맛있는 식당",
  "ownerName": "김사장",
  "phoneNumber": "010-1234-5678",
  "address": "서울시 강남구 테헤란로 123"
}
```

### 로그인 API
- **URL**: `POST http://localhost:8080/api/auth/login`
- **Body**:
```json
{
  "username": "store_owner1",
  "password": "password123"
}
```
- **Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "store_owner1",
  "userId": 1,
  "userType": "STORE_OWNER",
  "businessName": "맛있는 식당"
}
```

### 프로필 조회 API
- **URL**: `GET http://localhost:8080/api/user/profile`
- **Headers**: `Authorization: Bearer {token}`

## 6. 주의사항

1. **Android 에뮬레이터**: `localhost` 대신 `10.0.2.2` 사용
2. **실제 기기**: 컴퓨터의 로컬 IP 주소 사용 (예: `192.168.0.10`)
3. **토큰 저장**: `shared_preferences` 패키지로 토큰 저장
4. **에러 처리**: try-catch로 네트워크 오류 처리
5. **로딩 상태**: 사용자 경험을 위해 로딩 인디케이터 표시
