# 📱 Flutter 채팅 기능 구현 가이드

## 개요
매장과 유통업체 간 실시간 채팅 기능을 Flutter 앱에 구현하기 위한 API 가이드입니다.

---

## 🔐 인증

모든 API 요청에는 JWT 토큰이 필요합니다.

### Header 형식
```
Authorization: Bearer {accessToken}
```

### 로그인 API
```
POST /api/auth/login
Content-Type: application/json

Request Body:
{
  "username": "store1",
  "password": "password123"
}

Response:
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "username": "store1",
  "userId": 37,
  "userType": "STORE_OWNER",  // 또는 "DISTRIBUTOR"
  "businessName": "테스트 매장"
}
```

**Flutter 구현 팁:**
- `accessToken`을 secure storage에 저장
- 모든 HTTP 요청 헤더에 자동으로 추가
- `userType`으로 매장/유통업체 구분

---

## 📡 REST API

### 1. 채팅방 생성 또는 조회

특정 매장과 유통업체 간의 채팅방을 생성하거나 기존 채팅방을 조회합니다.

```
POST /api/chat/rooms
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "storeId": "store1",
  "distributorId": "dist1"
}

Response (200 OK):
{
  "id": 1,
  "roomId": "store_store1_dist_dist1",
  "storeId": "store1",
  "distributorId": "dist1",
  "storeName": "테스트 매장",
  "distributorName": "테스트 유통업체",
  "isActive": true,
  "lastMessageAt": "2025-11-27T17:45:30",  // 마지막 메시지 시간 (null 가능)
  "unreadCount": 3  // 읽지 않은 메시지 수
}
```

**사용 시나리오:**
- 견적 요청 화면에서 "문의하기" 버튼 클릭 시
- 주문 상세 화면에서 "채팅하기" 버튼 클릭 시
- 채팅 목록에서 특정 상대방 선택 시

---

### 2. 내 채팅방 목록 조회

현재 로그인한 사용자의 모든 채팅방 목록을 조회합니다.

```
GET /api/chat/rooms
Authorization: Bearer {token}

Response (200 OK):
[
  {
    "id": 1,
    "roomId": "store_store1_dist_dist1",
    "storeId": "store1",
    "distributorId": "dist1",
    "storeName": "테스트 매장",
    "distributorName": "테스트 유통업체",
    "isActive": true,
    "lastMessageAt": "2025-11-27T17:45:30",
    "unreadCount": 3
  },
  {
    "id": 2,
    "roomId": "store_store1_dist_dist2",
    "storeId": "store1",
    "distributorId": "dist2",
    "storeName": "테스트 매장",
    "distributorName": "다른 유통업체",
    "isActive": true,
    "lastMessageAt": "2025-11-27T16:30:00",
    "unreadCount": 0
  }
]
```

**UI 구현 팁:**
- `lastMessageAt` 기준으로 최신순 정렬
- `unreadCount > 0`이면 배지 표시
- 매장이면 `distributorName`, 유통업체면 `storeName` 표시

---

### 3. 채팅 메시지 목록 조회 (페이징)

특정 채팅방의 메시지 목록을 페이징으로 조회합니다.

```
GET /api/chat/rooms/{roomId}/messages?page=0&size=50
Authorization: Bearer {token}

Path Parameter:
- roomId: 채팅방 ID (예: "store_store1_dist_dist1")

Query Parameters:
- page: 페이지 번호 (0부터 시작, 기본값: 0)
- size: 페이지 크기 (기본값: 50)

Response (200 OK):
{
  "content": [
    {
      "id": 1,
      "roomId": "store_store1_dist_dist1",
      "senderId": "store1",
      "senderType": "STORE",  // STORE 또는 DISTRIBUTOR
      "messageType": "TEXT",  // TEXT, ORDER_INQUIRY, QUOTE_REQUEST, QUOTE_RESPONSE, SYSTEM
      "content": "안녕하세요, 견적 문의드립니다.",
      "metadata": null,  // JSON 문자열 (주문ID, 견적ID 등 추가 정보)
      "isRead": true,
      "createdAt": "2025-11-27T17:45:30"
    },
    {
      "id": 2,
      "roomId": "store_store1_dist_dist1",
      "senderId": "dist1",
      "senderType": "DISTRIBUTOR",
      "messageType": "TEXT",
      "content": "네, 무엇을 도와드릴까요?",
      "metadata": null,
      "isRead": false,
      "createdAt": "2025-11-27T17:46:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50
  },
  "totalPages": 1,
  "totalElements": 2,
  "last": true,
  "first": true,
  "empty": false
}
```

**페이징 구현 팁:**
- 최신 메시지가 먼저 오므로 역순으로 표시
- 스크롤 상단에 도달하면 다음 페이지 로드 (page++)
- `last: true`이면 더 이상 로드할 메시지 없음

---

### 4. 읽지 않은 메시지 수 조회

특정 채팅방의 읽지 않은 메시지 수를 조회합니다.

```
GET /api/chat/rooms/{roomId}/unread-count
Authorization: Bearer {token}

Response (200 OK):
5
```

**사용 시나리오:**
- 채팅 목록 화면에서 각 채팅방의 배지 표시
- 앱 전체 알림 배지 수 계산

---

### 5. 메시지 읽음 처리

채팅방에 입장하거나 메시지를 확인했을 때 읽음 처리합니다.

```
PUT /api/chat/rooms/{roomId}/read
Authorization: Bearer {token}

Response (200 OK):
(빈 응답)
```

**호출 시점:**
- 채팅방 화면 진입 시
- 앱이 포그라운드로 전환되고 채팅방이 열려있을 때

---

## 🔌 WebSocket (실시간 메시지)

### 필요한 Flutter 패키지
```yaml
dependencies:
  stomp_dart_client: ^2.0.0  # STOMP 프로토콜
  web_socket_channel: ^2.4.0  # WebSocket
```

### 연결 흐름

#### 1. WebSocket 연결
```
엔드포인트: ws://your-server:8080/ws/chat
프로토콜: STOMP over SockJS
```

#### 2. 연결 시 인증 (중요!)
```
CONNECT 프레임 헤더에 반드시 포함:
Authorization: Bearer {accessToken}

⚠️ 주의사항:
- 연결 시 반드시 유효한 JWT 토큰을 전송해야 합니다
- 토큰이 없거나 유효하지 않으면 연결이 거부됩니다
- 서버는 토큰에서 username을 추출하여 senderId로 사용합니다
- 클라이언트가 보낸 senderId는 무시됩니다 (보안)
```

#### 3. 채팅방 구독
```
구독 주소: /topic/chat/{roomId}

예시: /topic/chat/store_store1_dist_dist1
```

#### 4. 메시지 전송
```
발행 주소: /app/chat/{roomId}

메시지 본문 (JSON):
{
  "content": "안녕하세요!",
  "messageType": "TEXT",
  "metadata": null
}

⚠️ 중요: senderId 필드를 보내지 마세요!
- 서버가 JWT 토큰에서 자동으로 추출합니다
- 클라이언트가 보낸 senderId는 보안상 무시됩니다
- 인증된 사용자의 username이 자동으로 senderId로 설정됩니다
```

#### 5. 메시지 수신
```
구독한 주소로 메시지 수신:

수신 데이터 (JSON):
{
  "id": 123,
  "roomId": "store_store1_dist_dist1",
  "senderId": "dist1",
  "senderType": "DISTRIBUTOR",
  "messageType": "TEXT",
  "content": "네, 무엇을 도와드릴까요?",
  "metadata": null,
  "isRead": false,
  "createdAt": "2025-11-27T17:46:00"
}
```

---

## 📋 메시지 타입 (MessageType)

### TEXT
일반 텍스트 메시지

**사용:**
```json
{
  "content": "안녕하세요!",
  "messageType": "TEXT",
  "metadata": null
}
```

### ORDER_INQUIRY
주문 관련 문의

**사용:**
```json
{
  "content": "주문 배송은 언제쯤 가능한가요?",
  "messageType": "ORDER_INQUIRY",
  "metadata": "{\"orderId\": 123}"
}
```

**UI 표시:** 주문 정보 카드와 함께 표시

### QUOTE_REQUEST
견적 요청

**사용:**
```json
{
  "content": "쌀 20kg 견적 요청드립니다.",
  "messageType": "QUOTE_REQUEST",
  "metadata": "{\"productId\": 456, \"quantity\": 20}"
}
```

**UI 표시:** 견적 요청 카드 형태로 표시

### QUOTE_RESPONSE
견적 응답

**사용:**
```json
{
  "content": "쌀 20kg 견적입니다. 50,000원",
  "messageType": "QUOTE_RESPONSE",
  "metadata": "{\"quoteId\": 789, \"price\": 50000}"
}
```

**UI 표시:** 견적서 카드 형태로 표시, "주문하기" 버튼 추가

### SYSTEM
시스템 메시지 (자동 생성)

**예시:**
```json
{
  "content": "주문이 완료되었습니다. 주문번호: ORD-001",
  "messageType": "SYSTEM",
  "metadata": "{\"orderId\": 123, \"orderNumber\": \"ORD-001\"}"
}
```

**UI 표시:** 중앙 정렬, 회색 배경

---

## 🎨 Flutter UI 구현 권장사항

### 1. 채팅 목록 화면
```
- ListView with pull-to-refresh
- 각 항목: 상대방 이름, 마지막 메시지, 시간, 읽지 않은 배지
- 정렬: lastMessageAt 기준 최신순
- 빈 상태: "아직 대화가 없습니다" 메시지
```

### 2. 채팅 화면
```
- 상단: 상대방 이름, 뒤로가기 버튼
- 중앙: 메시지 리스트 (역순 스크롤)
  - 내 메시지: 오른쪽 정렬, 파란색 배경
  - 상대방 메시지: 왼쪽 정렬, 회색 배경
  - 시스템 메시지: 중앙 정렬, 연한 회색
- 하단: 텍스트 입력창, 전송 버튼
- 스크롤 상단 도달 시: 이전 메시지 로드 (페이징)
```

### 3. 메시지 타입별 UI
```
TEXT: 일반 말풍선
ORDER_INQUIRY: 주문 아이콘 + 말풍선
QUOTE_REQUEST: 견적 요청 카드 (제품 정보 표시)
QUOTE_RESPONSE: 견적 응답 카드 (가격 정보 + 주문 버튼)
SYSTEM: 중앙 정렬 회색 텍스트
```

---

## 🔄 상태 관리 권장사항

### 1. 채팅 목록 상태
```
- 채팅방 리스트
- 전체 읽지 않은 메시지 수
- 로딩 상태
- 에러 상태
```

### 2. 채팅 화면 상태
```
- 현재 채팅방 정보
- 메시지 리스트
- 페이징 정보 (현재 페이지, 마지막 페이지 여부)
- WebSocket 연결 상태
- 메시지 전송 중 상태
```

### 3. WebSocket 관리
```
- 앱 포그라운드: 연결 유지
- 앱 백그라운드: 연결 해제 (푸시 알림으로 대체)
- 채팅 화면 진입: 해당 채팅방 구독
- 채팅 화면 이탈: 구독 해제
- 연결 끊김: 자동 재연결 (exponential backoff)
```

---

## 🚀 구현 순서 권장

### Phase 1: REST API 기본 구현
1. HTTP 클라이언트 설정 (dio + interceptor)
2. 채팅방 목록 조회 및 표시
3. 채팅 메시지 목록 조회 및 표시
4. 메시지 읽음 처리

### Phase 2: WebSocket 실시간 통신
1. STOMP 클라이언트 설정
2. 채팅방 구독
3. 메시지 전송
4. 실시간 메시지 수신 및 UI 업데이트

### Phase 3: 고급 기능
1. 페이징 (무한 스크롤)
2. 메시지 타입별 UI 커스터마이징
3. 읽지 않은 메시지 배지
4. 푸시 알림 연동

---

## �  보안 및 인증 상세

### WebSocket 인증 흐름

1. **연결 시 (CONNECT)**
   - 클라이언트: `Authorization: Bearer {token}` 헤더 전송
   - 서버: JWT 토큰 검증
   - 서버: 토큰에서 username과 roles 추출
   - 서버: WebSocket 세션에 인증 정보 저장
   - 실패 시: 연결 거부

2. **메시지 전송 시 (SEND)**
   - 서버: 세션에 저장된 인증 정보 확인
   - 서버: 인증된 username을 senderId로 사용
   - 클라이언트가 보낸 senderId는 무시 (보안)
   - 인증 정보 없으면: 메시지 거부

3. **구독 시 (SUBSCRIBE)**
   - 서버: 세션에 저장된 인증 정보 확인
   - 인증 정보 없으면: 구독 거부

### 왜 클라이언트의 senderId를 무시하나요?

**보안 문제:**
```
만약 클라이언트가 보낸 senderId를 그대로 사용하면:
- 사용자 A가 사용자 B인 척 메시지를 보낼 수 있음
- 악의적인 사용자가 다른 사람의 이름으로 메시지 전송 가능
- 메시지 위조 및 사기 가능
```

**올바른 방법:**
```
서버에서 JWT 토큰으로 인증된 사용자 정보 사용:
1. 클라이언트: JWT 토큰으로 연결
2. 서버: 토큰에서 username 추출 (예: "store1")
3. 서버: 추출한 username을 senderId로 사용
4. 결과: 메시지의 senderId는 항상 인증된 사용자
```

### Flutter 구현 시 주의사항

**❌ 잘못된 구현:**
```dart
// senderId를 클라이언트에서 보내지 마세요!
final message = {
  'senderId': currentUserId,  // ❌ 이렇게 하지 마세요
  'content': '안녕하세요',
  'messageType': 'TEXT'
};
stompClient.send(destination: '/app/chat/$roomId', body: jsonEncode(message));
```

**✅ 올바른 구현:**
```dart
// senderId 없이 메시지만 보내세요
final message = {
  'content': '안녕하세요',
  'messageType': 'TEXT',
  'metadata': null
};
stompClient.send(destination: '/app/chat/$roomId', body: jsonEncode(message));

// 서버가 JWT 토큰에서 자동으로 senderId를 설정합니다
```

### 토큰 만료 처리

```dart
// WebSocket 연결 실패 시
onWebSocketError: (error) {
  if (error.contains('401') || error.contains('Unauthorized')) {
    // 토큰 만료 가능성
    // 1. 토큰 갱신 시도
    // 2. 실패 시 재로그인 유도
    // 3. 재로그인 후 WebSocket 재연결
  }
}
```

## 🐛 에러 처리

### HTTP 에러
```
400 Bad Request: 잘못된 요청 (roomId 없음 등)
401 Unauthorized: 토큰 만료 또는 유효하지 않음 → 재로그인 유도
403 Forbidden: 권한 없음 (다른 사람의 채팅방 접근 시도)
404 Not Found: 채팅방 또는 리소스 없음
500 Internal Server Error: 서버 오류 → 재시도 또는 고객센터 안내
```

### WebSocket 에러
```
연결 실패: 네트워크 확인 후 재연결
인증 실패: 토큰 갱신 후 재연결
연결 끊김: 자동 재연결 (최대 3회)
```

---

## 📝 metadata 활용 예시

### 주문 관련 메시지
```json
{
  "content": "주문이 완료되었습니다.",
  "messageType": "SYSTEM",
  "metadata": "{\"orderId\": 123, \"orderNumber\": \"ORD-001\", \"totalAmount\": 50000}"
}
```

**Flutter에서 파싱:**
```dart
final metadata = jsonDecode(message.metadata);
final orderId = metadata['orderId'];
final orderNumber = metadata['orderNumber'];

// UI: "주문 상세 보기" 버튼 추가
// 클릭 시 주문 상세 화면으로 이동 (orderId 전달)
```

### 견적 관련 메시지
```json
{
  "content": "견적서를 보내드립니다.",
  "messageType": "QUOTE_RESPONSE",
  "metadata": "{\"quoteId\": 789, \"items\": [{\"name\": \"쌀\", \"quantity\": 20, \"price\": 50000}], \"totalPrice\": 50000}"
}
```

**Flutter에서 파싱:**
```dart
final metadata = jsonDecode(message.metadata);
final quoteId = metadata['quoteId'];
final items = metadata['items'];
final totalPrice = metadata['totalPrice'];

// UI: 견적서 카드 표시 + "주문하기" 버튼
```

---

## 🔔 푸시 알림 연동 (향후)

### 알림 시나리오
1. 새 메시지 도착 (앱이 백그라운드일 때)
2. 견적 응답 도착
3. 주문 상태 변경

### 알림 페이로드 예시
```json
{
  "type": "NEW_MESSAGE",
  "roomId": "store_store1_dist_dist1",
  "senderName": "테스트 유통업체",
  "messagePreview": "견적서를 보내드립니다.",
  "messageType": "QUOTE_RESPONSE"
}
```

**클릭 시 동작:**
- 앱 실행
- 해당 채팅방으로 이동
- 메시지 읽음 처리

---

**테스트 계정:**
- 매장: username=`store1`, password=`password123`
- 유통업체: username=`dist1`, password=`password123`

**테스트 서버:**
- REST API: `http://localhost:8080`
- WebSocket: `ws://localhost:8080/ws/chat`
