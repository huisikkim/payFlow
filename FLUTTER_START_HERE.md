# 🎯 Flutter 개발자 시작 가이드

## 👋 환영합니다!

품질 이슈 신고 및 반품/교환 시스템의 Flutter 앱을 개발하시는군요!
이 문서는 어디서부터 시작해야 할지 안내해드립니다.

---

## 📖 문서 읽는 순서

### 1단계: 빠른 시작 (5분) ⚡
**[FLUTTER_QUALITY_ISSUE_QUICK_START.md](./FLUTTER_QUALITY_ISSUE_QUICK_START.md)**
- 최소한의 코드로 빠르게 시작
- 모델 클래스 복사
- API Service 클래스 복사
- 간단한 사용 예시

👉 **지금 바로 시작하려면 이 문서부터 읽으세요!**

---

### 2단계: 전체 API 이해 (30분) 📚
**[FLUTTER_QUALITY_ISSUE_API.md](./FLUTTER_QUALITY_ISSUE_API.md)**
- 11개 API 엔드포인트 상세 설명
- 요청/응답 예시
- 완전한 API Service 클래스
- 가게사장님 화면 예시
- 유통업자 화면 예시
- 에러 처리
- 프로세스 플로우
- 체크리스트

👉 **본격적으로 개발하기 전에 이 문서를 정독하세요!**

---

### 3단계: 백엔드 이해 (선택) 🔧
**[QUALITY_ISSUE_GUIDE.md](./QUALITY_ISSUE_GUIDE.md)**
- 백엔드 API 상세 문서
- 비즈니스 로직
- 도메인 모델

👉 **백엔드 동작 원리가 궁금하면 읽어보세요.**

---

## 🚀 빠른 체크리스트

### 필수 작업
- [ ] [빠른 시작 가이드](./FLUTTER_QUALITY_ISSUE_QUICK_START.md) 읽기
- [ ] 의존성 추가 (`http` 패키지)
- [ ] 모델 클래스 복사 (IssueType, RequestAction, IssueStatus, QualityIssue)
- [ ] API Service 클래스 복사
- [ ] 로그인 및 토큰 관리 구현

### 가게사장님 화면
- [ ] 품질 이슈 신고 화면
  - [ ] 주문 정보 입력
  - [ ] 이슈 유형 선택
  - [ ] 사진 업로드
  - [ ] 설명 입력
  - [ ] 환불/교환 선택
- [ ] 내 이슈 목록 화면
  - [ ] 목록 표시
  - [ ] 상태별 색상 구분
  - [ ] 새로고침
- [ ] 이슈 상세 화면
  - [ ] 상세 정보 표시
  - [ ] 사진 갤러리
  - [ ] 상태 추적

### 유통업자 화면
- [ ] 대기 중인 이슈 목록
  - [ ] 목록 표시
  - [ ] 사진 확인
  - [ ] 승인/거절 버튼
- [ ] 승인 처리 화면
  - [ ] 코멘트 입력
  - [ ] 수거 예약
- [ ] 수거 관리 화면
  - [ ] 수거 예정 목록
  - [ ] 수거 완료 처리
- [ ] 완료 처리 화면
  - [ ] 환불/교환 완료
  - [ ] 완료 노트 입력

### 공통 기능
- [ ] 에러 처리
- [ ] 로딩 상태 표시
- [ ] 토스트/스낵바 알림
- [ ] 새로고침 기능

---

## 💡 주요 API 요약

### 가게사장님이 사용할 API
```dart
// 1. 품질 이슈 신고
POST /api/quality-issues

// 2. 내 이슈 목록
GET /api/quality-issues/store/{storeId}

// 3. 이슈 상세 조회
GET /api/quality-issues/{issueId}
```

### 유통업자가 사용할 API
```dart
// 1. 대기 중인 이슈
GET /api/quality-issues/distributor/{distributorId}/pending

// 2. 승인
POST /api/quality-issues/{issueId}/approve

// 3. 거절
POST /api/quality-issues/{issueId}/reject

// 4. 수거 예약
POST /api/quality-issues/{issueId}/schedule-pickup

// 5. 수거 완료
POST /api/quality-issues/{issueId}/complete-pickup

// 6. 환불/교환 완료
POST /api/quality-issues/{issueId}/complete-resolution
```

---

## 🎨 UI 디자인 가이드

### 상태별 색상
```dart
Color getStatusColor(IssueStatus status) {
  switch (status) {
    case IssueStatus.SUBMITTED:
      return Colors.orange;      // 접수됨
    case IssueStatus.REVIEWING:
      return Colors.blue;        // 검토 중
    case IssueStatus.APPROVED:
      return Colors.green;       // 승인됨
    case IssueStatus.REJECTED:
      return Colors.red;         // 거절됨
    case IssueStatus.PICKUP_SCHEDULED:
      return Colors.purple;      // 수거 예정
    case IssueStatus.PICKED_UP:
      return Colors.teal;        // 수거 완료
    case IssueStatus.REFUNDED:
    case IssueStatus.EXCHANGED:
      return Colors.indigo;      // 환불/교환 완료
  }
}
```

### 이슈 유형 아이콘
```dart
IconData getIssueTypeIcon(IssueType type) {
  switch (type) {
    case IssueType.POOR_QUALITY:
      return Icons.warning;           // 품질 불량
    case IssueType.WRONG_ITEM:
      return Icons.swap_horiz;        // 오배송
    case IssueType.DAMAGED:
      return Icons.broken_image;      // 파손
    case IssueType.EXPIRED:
      return Icons.schedule;          // 유통기한
    case IssueType.QUANTITY_MISMATCH:
      return Icons.numbers;           // 수량 불일치
  }
}
```

---

## 🔑 인증 흐름

```dart
// 1. 로그인
final response = await http.post(
  Uri.parse('$baseUrl/api/auth/login'),
  headers: {'Content-Type': 'application/json'},
  body: jsonEncode({
    'username': 'user',
    'password': 'password',
  }),
);

// 2. 토큰 추출
final token = jsonDecode(response.body)['accessToken'];

// 3. 토큰 저장 (flutter_secure_storage 권장)
await storage.write(key: 'jwt_token', value: token);

// 4. API 호출 시 토큰 포함
final apiService = QualityIssueService();
apiService.token = token;
```

---

## 📱 테스트 계정

### 로컬 서버
```
URL: http://localhost:8080
Username: user
Password: password
```

### 테스트 데이터
```
가게 ID: STORE_001
유통사 ID: DIST_001
주문 ID: 123
품목 ID: 456
```

---

## 🆘 도움이 필요하신가요?

### 자주 묻는 질문

**Q: 어떤 HTTP 패키지를 사용하나요?**
A: `http: ^1.1.0` 패키지를 사용합니다.

**Q: 토큰은 어떻게 저장하나요?**
A: `flutter_secure_storage` 패키지 사용을 권장합니다.

**Q: 사진 업로드는 어떻게 하나요?**
A: 현재는 URL 문자열 배열로 전달합니다. 실제 파일 업로드는 별도 구현이 필요합니다.

**Q: 날짜/시간 형식은?**
A: ISO 8601 형식 (`2025-11-30T14:00:00`)을 사용합니다.

**Q: 한글이 깨져요!**
A: `utf8.decode(response.bodyBytes)` 를 사용하세요.

---

## 📞 문의

- **API 문제**: 백엔드 팀에 문의
- **Flutter 구현 문제**: 프론트엔드 팀 내부 논의
- **문서 오류**: 문서 작성자에게 문의

---

## 🎉 시작하기

준비되셨나요? 그럼 [빠른 시작 가이드](./FLUTTER_QUALITY_ISSUE_QUICK_START.md)로 이동하세요!

**행운을 빕니다! 🚀**
