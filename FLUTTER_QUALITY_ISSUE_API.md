# Flutter 개발자를 위한 품질 이슈 API 가이드

## 📋 목차
1. [개요](#개요)
2. [인증](#인증)
3. [API 엔드포인트](#api-엔드포인트)
4. [데이터 모델](#데이터-모델)
5. [Flutter 구현 예시](#flutter-구현-예시)
6. [에러 처리](#에러-처리)

---

## 개요

### Base URL
```
http://localhost:8080
또는
https://your-production-domain.com
```

### 주요 기능
- 가게사장님: 품질 이슈 신고, 내 이슈 목록 조회
- 유통업자: 대기 중인 이슈 확인, 승인/거절, 수거 예약, 환불/교환 처리

---

## 인증

모든 API는 JWT 토큰 인증이 필요합니다.

### 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "password"
}
```

**응답:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "user"
}
```

### 헤더에 토큰 포함
```
Authorization: Bearer {accessToken}
```

---

## API 엔드포인트

### 1. 품질 이슈 신고 (가게사장님)

**가게사장님이 문제 상품을 신고합니다.**

```http
POST /api/quality-issues
Authorization: Bearer {token}
Content-Type: application/json

{
  "orderId": 123,
  "itemId": 456,
  "itemName": "양파 10kg",
  "storeId": "STORE_001",
  "storeName": "홍길동 식당",
  "distributorId": "DIST_001",
  "issueType": "POOR_QUALITY",
  "photoUrls": [
    "https://example.com/photo1.jpg",
    "https://example.com/photo2.jpg"
  ],
  "description": "양파가 썩었습니다. 절반 이상이 사용 불가능한 상태입니다.",
  "requestAction": "REFUND"
}
```

**응답 (200 OK):**
```json
{
  "id": 1,
  "orderId": 123,
  "itemId": 456,
  "itemName": "양파 10kg",
  "storeId": "STORE_001",
  "storeName": "홍길동 식당",
  "distributorId": "DIST_001",
  "issueType": "POOR_QUALITY",
  "issueTypeDescription": "품질 불량",
  "photoUrls": ["https://example.com/photo1.jpg"],
  "description": "양파가 썩었습니다.",
  "requestAction": "REFUND",
  "requestActionDescription": "환불",
  "status": "SUBMITTED",
  "statusDescription": "접수됨",
  "submittedAt": "2025-11-30T10:30:00",
  "reviewedAt": null,
  "reviewerComment": null,
  "pickupScheduledAt": null,
  "resolvedAt": null,
  "resolutionNote": null
}
```

---

### 2. 품질 이슈 상세 조회

**특정 품질 이슈의 상세 정보를 조회합니다.**

```http
GET /api/quality-issues/{issueId}
Authorization: Bearer {token}
```

**예시:**
```http
GET /api/quality-issues/1
```

**응답 (200 OK):**
```json
{
  "id": 1,
  "orderId": 123,
  "itemName": "양파 10kg",
  "status": "APPROVED",
  "statusDescription": "승인됨",
  "reviewerComment": "확인했습니다. 환불 처리하겠습니다.",
  "submittedAt": "2025-11-30T10:30:00",
  "reviewedAt": "2025-11-30T10:45:00"
}
```

---

### 3. 가게별 품질 이슈 목록 조회 (가게사장님)

**내 가게의 모든 품질 이슈를 조회합니다.**

```http
GET /api/quality-issues/store/{storeId}
Authorization: Bearer {token}
```

**예시:**
```http
GET /api/quality-issues/store/STORE_001
```

**응답 (200 OK):**
```json
[
  {
    "id": 1,
    "itemName": "양파 10kg",
    "issueType": "POOR_QUALITY",
    "status": "REFUNDED",
    "submittedAt": "2025-11-30T10:30:00",
    "resolvedAt": "2025-11-30T14:30:00"
  },
  {
    "id": 2,
    "itemName": "감자 20kg",
    "issueType": "DAMAGED",
    "status": "EXCHANGED",
    "submittedAt": "2025-11-30T11:00:00",
    "resolvedAt": "2025-11-30T16:00:00"
  }
]
```

---

### 4. 유통사별 대기 중인 품질 이슈 조회 (유통업자)

**유통업자가 처리해야 할 대기 중인 이슈를 조회합니다.**

```http
GET /api/quality-issues/distributor/{distributorId}/pending
Authorization: Bearer {token}
```

**예시:**
```http
GET /api/quality-issues/distributor/DIST_001/pending
```

**응답 (200 OK):**
```json
[
  {
    "id": 3,
    "itemName": "당근 5kg",
    "storeName": "행복한 카페",
    "issueType": "POOR_QUALITY",
    "status": "SUBMITTED",
    "submittedAt": "2025-11-30T12:00:00"
  }
]
```

---

### 5. 유통사별 전체 품질 이슈 조회 (유통업자)

**유통업자의 모든 품질 이슈를 조회합니다.**

```http
GET /api/quality-issues/distributor/{distributorId}
Authorization: Bearer {token}
```

**예시:**
```http
GET /api/quality-issues/distributor/DIST_001
```

---

### 6. 품질 이슈 검토 시작 (유통업자)

**유통업자가 품질 이슈 검토를 시작합니다.**

```http
POST /api/quality-issues/{issueId}/review
Authorization: Bearer {token}
```

**예시:**
```http
POST /api/quality-issues/1/review
```

**응답 (200 OK):**
```json
{
  "id": 1,
  "status": "REVIEWING",
  "statusDescription": "검토 중",
  "reviewedAt": "2025-11-30T10:40:00"
}
```

---

### 7. 품질 이슈 승인 (유통업자)

**유통업자가 품질 이슈를 승인합니다.**

```http
POST /api/quality-issues/{issueId}/approve
Authorization: Bearer {token}
Content-Type: application/json

{
  "comment": "확인했습니다. 품질 문제가 맞습니다. 환불 처리하겠습니다."
}
```

**응답 (200 OK):**
```json
{
  "id": 1,
  "status": "APPROVED",
  "statusDescription": "승인됨",
  "reviewerComment": "확인했습니다. 품질 문제가 맞습니다.",
  "reviewedAt": "2025-11-30T10:45:00"
}
```

---

### 8. 품질 이슈 거절 (유통업자)

**유통업자가 품질 이슈를 거절합니다.**

```http
POST /api/quality-issues/{issueId}/reject
Authorization: Bearer {token}
Content-Type: application/json

{
  "comment": "사진 확인 결과 정상 제품으로 보입니다."
}
```

**응답 (200 OK):**
```json
{
  "id": 1,
  "status": "REJECTED",
  "statusDescription": "거절됨",
  "reviewerComment": "사진 확인 결과 정상 제품으로 보입니다.",
  "reviewedAt": "2025-11-30T10:45:00",
  "resolvedAt": "2025-11-30T10:45:00"
}
```

---

### 9. 수거 예약 (유통업자)

**유통업자가 불량 상품 수거를 예약합니다.**

```http
POST /api/quality-issues/{issueId}/schedule-pickup
Authorization: Bearer {token}
Content-Type: application/json

{
  "pickupTime": "2025-11-30T14:00:00"
}
```

**응답 (200 OK):**
```json
{
  "id": 1,
  "status": "PICKUP_SCHEDULED",
  "statusDescription": "수거 예정",
  "pickupScheduledAt": "2025-11-30T14:00:00"
}
```

---

### 10. 수거 완료 (유통업자)

**유통업자가 불량 상품 수거를 완료 처리합니다.**

```http
POST /api/quality-issues/{issueId}/complete-pickup
Authorization: Bearer {token}
```

**응답 (200 OK):**
```json
{
  "id": 1,
  "status": "PICKED_UP",
  "statusDescription": "수거 완료"
}
```

---

### 11. 환불/교환 완료 (유통업자)

**유통업자가 환불 또는 교환을 완료 처리합니다.**

```http
POST /api/quality-issues/{issueId}/complete-resolution
Authorization: Bearer {token}
Content-Type: application/json

{
  "note": "환불 처리 완료했습니다. 다음 주문 시 할인 쿠폰을 제공하겠습니다."
}
```

**응답 (200 OK):**
```json
{
  "id": 1,
  "status": "REFUNDED",
  "statusDescription": "환불 완료",
  "resolutionNote": "환불 처리 완료했습니다.",
  "resolvedAt": "2025-11-30T14:30:00"
}
```

---

## 데이터 모델

### IssueType (이슈 유형)
```dart
enum IssueType {
  POOR_QUALITY,      // 품질 불량
  WRONG_ITEM,        // 오배송
  DAMAGED,           // 파손
  EXPIRED,           // 유통기한 임박/경과
  QUANTITY_MISMATCH  // 수량 불일치
}
```

### RequestAction (요청 액션)
```dart
enum RequestAction {
  REFUND,   // 환불
  EXCHANGE  // 교환
}
```

### IssueStatus (이슈 상태)
```dart
enum IssueStatus {
  SUBMITTED,         // 접수됨
  REVIEWING,         // 검토 중
  APPROVED,          // 승인됨
  REJECTED,          // 거절됨
  PICKUP_SCHEDULED,  // 수거 예정
  PICKED_UP,         // 수거 완료
  REFUNDED,          // 환불 완료
  EXCHANGED          // 교환 완료
}
```

### QualityIssue 모델
```dart
class QualityIssue {
  final int id;
  final int orderId;
  final int itemId;
  final String itemName;
  final String storeId;
  final String storeName;
  final String distributorId;
  final IssueType issueType;
  final String issueTypeDescription;
  final List<String> photoUrls;
  final String description;
  final RequestAction requestAction;
  final String requestActionDescription;
  final IssueStatus status;
  final String statusDescription;
  final DateTime submittedAt;
  final DateTime? reviewedAt;
  final String? reviewerComment;
  final DateTime? pickupScheduledAt;
  final DateTime? resolvedAt;
  final String? resolutionNote;

  QualityIssue({
    required this.id,
    required this.orderId,
    required this.itemId,
    required this.itemName,
    required this.storeId,
    required this.storeName,
    required this.distributorId,
    required this.issueType,
    required this.issueTypeDescription,
    required this.photoUrls,
    required this.description,
    required this.requestAction,
    required this.requestActionDescription,
    required this.status,
    required this.statusDescription,
    required this.submittedAt,
    this.reviewedAt,
    this.reviewerComment,
    this.pickupScheduledAt,
    this.resolvedAt,
    this.resolutionNote,
  });

  factory QualityIssue.fromJson(Map<String, dynamic> json) {
    return QualityIssue(
      id: json['id'],
      orderId: json['orderId'],
      itemId: json['itemId'],
      itemName: json['itemName'],
      storeId: json['storeId'],
      storeName: json['storeName'],
      distributorId: json['distributorId'],
      issueType: IssueType.values.byName(json['issueType']),
      issueTypeDescription: json['issueTypeDescription'],
      photoUrls: List<String>.from(json['photoUrls']),
      description: json['description'],
      requestAction: RequestAction.values.byName(json['requestAction']),
      requestActionDescription: json['requestActionDescription'],
      status: IssueStatus.values.byName(json['status']),
      statusDescription: json['statusDescription'],
      submittedAt: DateTime.parse(json['submittedAt']),
      reviewedAt: json['reviewedAt'] != null 
          ? DateTime.parse(json['reviewedAt']) 
          : null,
      reviewerComment: json['reviewerComment'],
      pickupScheduledAt: json['pickupScheduledAt'] != null 
          ? DateTime.parse(json['pickupScheduledAt']) 
          : null,
      resolvedAt: json['resolvedAt'] != null 
          ? DateTime.parse(json['resolvedAt']) 
          : null,
      resolutionNote: json['resolutionNote'],
    );
  }
}
```

---

## Flutter 구현 예시

### 1. API Service 클래스

```dart
import 'package:http/http.dart' as http;
import 'dart:convert';

class QualityIssueApiService {
  final String baseUrl = 'http://localhost:8080';
  String? _token;

  void setToken(String token) {
    _token = token;
  }

  Map<String, String> get _headers => {
    'Content-Type': 'application/json',
    if (_token != null) 'Authorization': 'Bearer $_token',
  };

  // 1. 품질 이슈 신고
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
      headers: _headers,
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
    } else {
      throw Exception('품질 이슈 신고 실패: ${response.body}');
    }
  }

  // 2. 품질 이슈 상세 조회
  Future<QualityIssue> getIssue(int issueId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/quality-issues/$issueId'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      return QualityIssue.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
    } else {
      throw Exception('품질 이슈 조회 실패: ${response.body}');
    }
  }

  // 3. 가게별 품질 이슈 목록 조회
  Future<List<QualityIssue>> getStoreIssues(String storeId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/quality-issues/store/$storeId'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((json) => QualityIssue.fromJson(json)).toList();
    } else {
      throw Exception('품질 이슈 목록 조회 실패: ${response.body}');
    }
  }

  // 4. 유통사별 대기 중인 품질 이슈 조회
  Future<List<QualityIssue>> getPendingIssues(String distributorId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/quality-issues/distributor/$distributorId/pending'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((json) => QualityIssue.fromJson(json)).toList();
    } else {
      throw Exception('대기 중인 이슈 조회 실패: ${response.body}');
    }
  }

  // 5. 유통사별 전체 품질 이슈 조회
  Future<List<QualityIssue>> getDistributorIssues(String distributorId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/api/quality-issues/distributor/$distributorId'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(utf8.decode(response.bodyBytes));
      return data.map((json) => QualityIssue.fromJson(json)).toList();
    } else {
      throw Exception('품질 이슈 목록 조회 실패: ${response.body}');
    }
  }

  // 6. 품질 이슈 검토 시작
  Future<QualityIssue> startReview(int issueId) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/quality-issues/$issueId/review'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      return QualityIssue.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
    } else {
      throw Exception('검토 시작 실패: ${response.body}');
    }
  }

  // 7. 품질 이슈 승인
  Future<QualityIssue> approveIssue(int issueId, String comment) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/quality-issues/$issueId/approve'),
      headers: _headers,
      body: jsonEncode({'comment': comment}),
    );

    if (response.statusCode == 200) {
      return QualityIssue.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
    } else {
      throw Exception('승인 실패: ${response.body}');
    }
  }

  // 8. 품질 이슈 거절
  Future<QualityIssue> rejectIssue(int issueId, String comment) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/quality-issues/$issueId/reject'),
      headers: _headers,
      body: jsonEncode({'comment': comment}),
    );

    if (response.statusCode == 200) {
      return QualityIssue.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
    } else {
      throw Exception('거절 실패: ${response.body}');
    }
  }

  // 9. 수거 예약
  Future<QualityIssue> schedulePickup(int issueId, DateTime pickupTime) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/quality-issues/$issueId/schedule-pickup'),
      headers: _headers,
      body: jsonEncode({
        'pickupTime': pickupTime.toIso8601String(),
      }),
    );

    if (response.statusCode == 200) {
      return QualityIssue.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
    } else {
      throw Exception('수거 예약 실패: ${response.body}');
    }
  }

  // 10. 수거 완료
  Future<QualityIssue> completePickup(int issueId) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/quality-issues/$issueId/complete-pickup'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      return QualityIssue.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
    } else {
      throw Exception('수거 완료 실패: ${response.body}');
    }
  }

  // 11. 환불/교환 완료
  Future<QualityIssue> completeResolution(int issueId, String note) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/quality-issues/$issueId/complete-resolution'),
      headers: _headers,
      body: jsonEncode({'note': note}),
    );

    if (response.statusCode == 200) {
      return QualityIssue.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
    } else {
      throw Exception('환불/교환 완료 실패: ${response.body}');
    }
  }
}
```

---

### 2. 사용 예시 - 가게사장님 화면

```dart
class StoreQualityIssuePage extends StatefulWidget {
  final String storeId;
  
  const StoreQualityIssuePage({required this.storeId});

  @override
  _StoreQualityIssuePageState createState() => _StoreQualityIssuePageState();
}

class _StoreQualityIssuePageState extends State<StoreQualityIssuePage> {
  final QualityIssueApiService _apiService = QualityIssueApiService();
  List<QualityIssue> _issues = [];
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _loadIssues();
  }

  Future<void> _loadIssues() async {
    setState(() => _isLoading = true);
    try {
      final issues = await _apiService.getStoreIssues(widget.storeId);
      setState(() {
        _issues = issues;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('목록 조회 실패: $e')),
      );
    }
  }

  Future<void> _submitIssue() async {
    try {
      final issue = await _apiService.submitIssue(
        orderId: 123,
        itemId: 456,
        itemName: '양파 10kg',
        storeId: widget.storeId,
        storeName: '홍길동 식당',
        distributorId: 'DIST_001',
        issueType: IssueType.POOR_QUALITY,
        photoUrls: ['https://example.com/photo1.jpg'],
        description: '양파가 썩었습니다.',
        requestAction: RequestAction.REFUND,
      );

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('품질 이슈 신고 완료: ${issue.id}')),
      );
      
      _loadIssues(); // 목록 새로고침
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('신고 실패: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('품질 이슈 관리')),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : ListView.builder(
              itemCount: _issues.length,
              itemBuilder: (context, index) {
                final issue = _issues[index];
                return ListTile(
                  title: Text(issue.itemName),
                  subtitle: Text(issue.statusDescription),
                  trailing: _buildStatusChip(issue.status),
                  onTap: () => _showIssueDetail(issue),
                );
              },
            ),
      floatingActionButton: FloatingActionButton(
        onPressed: _submitIssue,
        child: Icon(Icons.add),
      ),
    );
  }

  Widget _buildStatusChip(IssueStatus status) {
    Color color;
    switch (status) {
      case IssueStatus.SUBMITTED:
        color = Colors.orange;
        break;
      case IssueStatus.APPROVED:
        color = Colors.green;
        break;
      case IssueStatus.REJECTED:
        color = Colors.red;
        break;
      case IssueStatus.REFUNDED:
      case IssueStatus.EXCHANGED:
        color = Colors.blue;
        break;
      default:
        color = Colors.grey;
    }
    
    return Chip(
      label: Text(status.name),
      backgroundColor: color.withOpacity(0.2),
    );
  }

  void _showIssueDetail(QualityIssue issue) {
    // 상세 화면으로 이동
  }
}
```

---

### 3. 사용 예시 - 유통업자 화면

```dart
class DistributorQualityIssuePage extends StatefulWidget {
  final String distributorId;
  
  const DistributorQualityIssuePage({required this.distributorId});

  @override
  _DistributorQualityIssuePageState createState() => 
      _DistributorQualityIssuePageState();
}

class _DistributorQualityIssuePageState 
    extends State<DistributorQualityIssuePage> {
  final QualityIssueApiService _apiService = QualityIssueApiService();
  List<QualityIssue> _pendingIssues = [];
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _loadPendingIssues();
  }

  Future<void> _loadPendingIssues() async {
    setState(() => _isLoading = true);
    try {
      final issues = await _apiService.getPendingIssues(widget.distributorId);
      setState(() {
        _pendingIssues = issues;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('목록 조회 실패: $e')),
      );
    }
  }

  Future<void> _approveIssue(int issueId) async {
    try {
      await _apiService.approveIssue(
        issueId,
        '확인했습니다. 환불 처리하겠습니다.',
      );
      
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('승인 완료')),
      );
      
      _loadPendingIssues(); // 목록 새로고침
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('승인 실패: $e')),
      );
    }
  }

  Future<void> _rejectIssue(int issueId) async {
    try {
      await _apiService.rejectIssue(
        issueId,
        '사진 확인 결과 정상 제품으로 보입니다.',
      );
      
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('거절 완료')),
      );
      
      _loadPendingIssues(); // 목록 새로고침
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('거절 실패: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('대기 중인 품질 이슈')),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : ListView.builder(
              itemCount: _pendingIssues.length,
              itemBuilder: (context, index) {
                final issue = _pendingIssues[index];
                return Card(
                  margin: EdgeInsets.all(8),
                  child: Padding(
                    padding: EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          issue.itemName,
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        SizedBox(height: 8),
                        Text('가게: ${issue.storeName}'),
                        Text('이슈: ${issue.issueTypeDescription}'),
                        Text('설명: ${issue.description}'),
                        SizedBox(height: 8),
                        // 사진 표시
                        if (issue.photoUrls.isNotEmpty)
                          SizedBox(
                            height: 100,
                            child: ListView.builder(
                              scrollDirection: Axis.horizontal,
                              itemCount: issue.photoUrls.length,
                              itemBuilder: (context, photoIndex) {
                                return Padding(
                                  padding: EdgeInsets.only(right: 8),
                                  child: Image.network(
                                    issue.photoUrls[photoIndex],
                                    width: 100,
                                    height: 100,
                                    fit: BoxFit.cover,
                                  ),
                                );
                              },
                            ),
                          ),
                        SizedBox(height: 16),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.end,
                          children: [
                            TextButton(
                              onPressed: () => _rejectIssue(issue.id),
                              child: Text('거절'),
                              style: TextButton.styleFrom(
                                foregroundColor: Colors.red,
                              ),
                            ),
                            SizedBox(width: 8),
                            ElevatedButton(
                              onPressed: () => _approveIssue(issue.id),
                              child: Text('승인'),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
    );
  }
}
```

---

## 에러 처리

### HTTP 상태 코드

| 상태 코드 | 의미 | 처리 방법 |
|----------|------|----------|
| 200 | 성공 | 정상 처리 |
| 400 | 잘못된 요청 | 요청 데이터 확인 |
| 401 | 인증 실패 | 로그인 필요 |
| 403 | 권한 없음 | 권한 확인 |
| 404 | 리소스 없음 | ID 확인 |
| 500 | 서버 오류 | 재시도 또는 관리자 문의 |

### 에러 응답 예시

```json
{
  "timestamp": "2025-11-30T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "품질 이슈를 찾을 수 없습니다: 999",
  "path": "/api/quality-issues/999"
}
```

### Flutter 에러 처리 예시

```dart
Future<QualityIssue> getIssueWithErrorHandling(int issueId) async {
  try {
    final response = await http.get(
      Uri.parse('$baseUrl/api/quality-issues/$issueId'),
      headers: _headers,
    );

    if (response.statusCode == 200) {
      return QualityIssue.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
    } else if (response.statusCode == 401) {
      throw UnauthorizedException('로그인이 필요합니다.');
    } else if (response.statusCode == 404) {
      throw NotFoundException('품질 이슈를 찾을 수 없습니다.');
    } else {
      throw ApiException('서버 오류: ${response.statusCode}');
    }
  } on SocketException {
    throw NetworkException('네트워크 연결을 확인해주세요.');
  } on TimeoutException {
    throw NetworkException('요청 시간이 초과되었습니다.');
  } catch (e) {
    throw ApiException('알 수 없는 오류: $e');
  }
}

// 커스텀 예외 클래스
class ApiException implements Exception {
  final String message;
  ApiException(this.message);
}

class UnauthorizedException extends ApiException {
  UnauthorizedException(String message) : super(message);
}

class NotFoundException extends ApiException {
  NotFoundException(String message) : super(message);
}

class NetworkException extends ApiException {
  NetworkException(String message) : super(message);
}
```

---

## 프로세스 플로우

### 환불 프로세스
```
1. 가게사장님: submitIssue() → SUBMITTED
2. 유통업자: startReview() → REVIEWING
3. 유통업자: approveIssue() → APPROVED
4. 유통업자: schedulePickup() → PICKUP_SCHEDULED
5. 유통업자: completePickup() → PICKED_UP
6. 유통업자: completeResolution() → REFUNDED
```

### 교환 프로세스
```
1. 가게사장님: submitIssue() → SUBMITTED
2. 유통업자: startReview() → REVIEWING
3. 유통업자: approveIssue() → APPROVED
4. 유통업자: schedulePickup() → PICKUP_SCHEDULED
5. 유통업자: completePickup() → PICKED_UP
6. 유통업자: completeResolution() → EXCHANGED
```

### 거절 프로세스
```
1. 가게사장님: submitIssue() → SUBMITTED
2. 유통업자: startReview() → REVIEWING
3. 유통업자: rejectIssue() → REJECTED (종료)
```

---

## 테스트 가이드

### 1. 로컬 서버 실행
```bash
cd payFlow
./gradlew bootRun
```

서버가 `http://localhost:8080`에서 실행됩니다.

### 2. 테스트 계정
```
Username: user
Password: password
```

### 3. Postman 테스트
1. 로그인하여 토큰 발급
2. 토큰을 Authorization 헤더에 포함
3. 각 API 엔드포인트 테스트

### 4. Flutter 앱 테스트
```dart
void main() async {
  final apiService = QualityIssueApiService();
  
  // 1. 로그인 (토큰 설정)
  apiService.setToken('your-jwt-token');
  
  // 2. 품질 이슈 신고
  final issue = await apiService.submitIssue(
    orderId: 123,
    itemId: 456,
    itemName: '양파 10kg',
    storeId: 'STORE_001',
    storeName: '홍길동 식당',
    distributorId: 'DIST_001',
    issueType: IssueType.POOR_QUALITY,
    photoUrls: ['https://example.com/photo1.jpg'],
    description: '양파가 썩었습니다.',
    requestAction: RequestAction.REFUND,
  );
  
  print('신고 완료: ${issue.id}');
  
  // 3. 내 이슈 목록 조회
  final issues = await apiService.getStoreIssues('STORE_001');
  print('총 ${issues.length}건의 이슈');
}
```

---

## 주의사항

### 1. 날짜/시간 형식
- ISO 8601 형식 사용: `2025-11-30T14:00:00`
- Flutter에서는 `DateTime.toIso8601String()` 사용
- 파싱 시 `DateTime.parse()` 사용

### 2. 한글 인코딩
- UTF-8 인코딩 사용
- Flutter에서 응답 파싱 시 `utf8.decode(response.bodyBytes)` 사용

### 3. 토큰 관리
- 토큰은 안전하게 저장 (flutter_secure_storage 사용 권장)
- 토큰 만료 시 재로그인 필요
- 모든 API 호출 시 토큰 포함

### 4. 사진 업로드
- 현재는 URL 문자열 배열로 전달
- 실제 파일 업로드 기능은 별도 구현 필요
- 이미지 서버 또는 S3 등에 업로드 후 URL 전달

### 5. 상태 전이 규칙
- SUBMITTED → REVIEWING → APPROVED/REJECTED
- APPROVED → PICKUP_SCHEDULED → PICKED_UP → REFUNDED/EXCHANGED
- 잘못된 상태 전이 시 400 에러 발생

---

## 추가 리소스

### 관련 문서
- [전체 API 가이드](./QUALITY_ISSUE_GUIDE.md)
- [사용 예시](./QUALITY_ISSUE_EXAMPLE.md)
- [테스트 스크립트](./test-quality-issue-api.sh)

### 문의
- 백엔드 API 문제: 백엔드 팀에 문의
- Flutter 구현 문제: 프론트엔드 팀 내부 논의

---

## 체크리스트

구현 시 다음 항목을 확인하세요:

- [ ] API Service 클래스 구현
- [ ] 데이터 모델 (QualityIssue, Enum 등) 구현
- [ ] 로그인 및 토큰 관리
- [ ] 가게사장님 화면 (신고, 목록 조회)
- [ ] 유통업자 화면 (대기 목록, 승인/거절)
- [ ] 에러 처리
- [ ] 로딩 상태 표시
- [ ] 사진 표시 (Image.network)
- [ ] 상태별 UI 구분 (색상, 아이콘)
- [ ] 새로고침 기능
- [ ] 상세 화면
- [ ] 수거 예약 화면 (DateTimePicker)
- [ ] 완료 처리 화면

---

**마지막 업데이트: 2025-11-30**
