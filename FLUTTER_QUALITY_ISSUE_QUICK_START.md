# Flutter 품질 이슈 API - 빠른 시작 가이드

## 🚀 5분 안에 시작하기

### 1. 의존성 추가 (pubspec.yaml)
```yaml
dependencies:
  http: ^1.1.0
```

### 2. 모델 클래스 복사 (lib/models/quality_issue.dart)
```dart
enum IssueType { POOR_QUALITY, WRONG_ITEM, DAMAGED, EXPIRED, QUANTITY_MISMATCH }
enum RequestAction { REFUND, EXCHANGE }
enum IssueStatus { SUBMITTED, REVIEWING, APPROVED, REJECTED, PICKUP_SCHEDULED, PICKED_UP, REFUNDED, EXCHANGED }

class QualityIssue {
  final int id;
  final String itemName;
  final IssueStatus status;
  final String statusDescription;
  final DateTime submittedAt;
  
  QualityIssue({
    required this.id,
    required this.itemName,
    required this.status,
    required this.statusDescription,
    required this.submittedAt,
  });
  
  factory QualityIssue.fromJson(Map<String, dynamic> json) {
    return QualityIssue(
      id: json['id'],
      itemName: json['itemName'],
      status: IssueStatus.values.byName(json['status']),
      statusDescription: json['statusDescription'],
      submittedAt: DateTime.parse(json['submittedAt']),
    );
  }
}
```

### 3. API Service 클래스 (lib/services/quality_issue_service.dart)
```dart
import 'package:http/http.dart' as http;
import 'dart:convert';

class QualityIssueService {
  final String baseUrl = 'http://localhost:8080';
  String? token;

  // 품질 이슈 신고
  Future<QualityIssue> submitIssue({
    required int orderId,
    required int itemId,
    required String itemName,
    required String storeId,
    required String storeName,
    required String distributorId,
    required IssueType issueType,
    required List<String> photoUrls,
    required String description,
    required RequestAction requestAction,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/quality-issues'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      },
      body: jsonEncode({
        'orderId': orderId,
        'itemId': itemId,
        'itemName': itemName,
        'storeId': storeId,
        'storeName': storeName,
        'distributorId': distributorId,
        'issueType': issueType.name,
        'photoUrls': photoUrls,
        'description': description,
        'requestAction': requestAction.name,
      }),
    );

    if (response.statusCode == 200) {
      return QualityIssue.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
    }
    throw Exception('신고 실패');
  }

  // 내 이슈 목록
  Future<List<QualityIssue>> getMyIssues(String storeId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/quality-issues/store/$storeId'),
      headers: {'Authorization': 'Bearer $token'},
    );

    if (response.statusCode == 200) {
      final List data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((json) => QualityIssue.fromJson(json)).toList();
    }
    throw Exception('조회 실패');
  }
}
```

### 4. 사용 예시
```dart
final service = QualityIssueService();
service.token = 'your-jwt-token';

// 신고하기
final issue = await service.submitIssue(
  orderId: 123,
  itemId: 456,
  itemName: '양파 10kg',
  storeId: 'STORE_001',
  storeName: '홍길동 식당',
  distributorId: 'DIST_001',
  issueType: IssueType.POOR_QUALITY,
  photoUrls: ['https://example.com/photo.jpg'],
  description: '양파가 썩었습니다.',
  requestAction: RequestAction.REFUND,
);

print('신고 완료: ${issue.id}');

// 목록 조회
final issues = await service.getMyIssues('STORE_001');
print('총 ${issues.length}건');
```

---

## 📱 주요 API 목록

| API | 메서드 | 엔드포인트 | 설명 |
|-----|--------|-----------|------|
| 신고 | POST | `/api/quality-issues` | 품질 이슈 신고 |
| 조회 | GET | `/api/quality-issues/{id}` | 상세 조회 |
| 목록 | GET | `/api/quality-issues/store/{storeId}` | 내 이슈 목록 |
| 승인 | POST | `/api/quality-issues/{id}/approve` | 유통업자 승인 |
| 거절 | POST | `/api/quality-issues/{id}/reject` | 유통업자 거절 |

---

## 🎨 UI 예시

### 신고 버튼
```dart
ElevatedButton(
  onPressed: () async {
    final issue = await service.submitIssue(...);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('신고 완료: ${issue.id}')),
    );
  },
  child: Text('품질 이슈 신고'),
)
```

### 목록 표시
```dart
ListView.builder(
  itemCount: issues.length,
  itemBuilder: (context, index) {
    final issue = issues[index];
    return ListTile(
      title: Text(issue.itemName),
      subtitle: Text(issue.statusDescription),
      trailing: _buildStatusChip(issue.status),
    );
  },
)
```

---

## 🔑 인증

```dart
// 1. 로그인
final loginResponse = await http.post(
  Uri.parse('$baseUrl/api/auth/login'),
  headers: {'Content-Type': 'application/json'},
  body: jsonEncode({
    'username': 'user',
    'password': 'password',
  }),
);

final token = jsonDecode(loginResponse.body)['accessToken'];

// 2. 토큰 저장
service.token = token;

// 3. API 호출
final issues = await service.getMyIssues('STORE_001');
```

---

## 📚 전체 문서

더 자세한 내용은 [FLUTTER_QUALITY_ISSUE_API.md](./FLUTTER_QUALITY_ISSUE_API.md)를 참고하세요.

- 전체 API 목록 (11개)
- 상세 요청/응답 예시
- 에러 처리
- 프로세스 플로우
- 체크리스트
