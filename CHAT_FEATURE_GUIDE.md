# 💬 채팅 기능 가이드

## 개요
매장과 유통업체 간 실시간 채팅 기능입니다. WebSocket을 사용하여 실시간 메시지 전송이 가능하며, 견적 요청, 주문 문의 등 다양한 메시지 타입을 지원합니다.

## 주요 기능

### 1. 채팅방 관리
- 매장-유통업체 간 1:1 채팅방 자동 생성
- 채팅방 목록 조회
- 읽지 않은 메시지 수 표시

### 2. 메시지 타입
- **TEXT**: 일반 텍스트 메시지
- **ORDER_INQUIRY**: 주문 문의
- **QUOTE_REQUEST**: 견적 요청
- **QUOTE_RESPONSE**: 견적 응답
- **SYSTEM**: 시스템 메시지 (주문 완료, 배송 시작 등)

### 3. 실시간 통신
- WebSocket (STOMP) 기반 실시간 메시지 전송
- 채팅방 구독을 통한 메시지 수신
- JWT 인증 기반 보안

## API 엔드포인트

### REST API

#### 1. 채팅방 생성/조회
```bash
POST /api/chat/rooms
Authorization: Bearer {token}
Content-Type: application/json

{
  "storeId": "store1",
  "distributorId": "dist1"
}
```

#### 2. 내 채팅방 목록 조회
```bash
GET /api/chat/rooms
Authorization: Bearer {token}
```

#### 3. 메시지 목록 조회 (페이징)
```bash
GET /api/chat/rooms/{roomId}/messages?page=0&size=50
Authorization: Bearer {token}
```

#### 4. 읽지 않은 메시지 수 조회
```bash
GET /api/chat/rooms/{roomId}/unread-count
Authorization: Bearer {token}
```

#### 5. 메시지 읽음 처리
```bash
PUT /api/chat/rooms/{roomId}/read
Authorization: Bearer {token}
```

### WebSocket API

#### 연결
```javascript
const socket = new SockJS('/ws/chat');
const stompClient = Stomp.over(socket);

stompClient.connect(
  { 'Authorization': 'Bearer ' + token },
  function(frame) {
    console.log('Connected: ' + frame);
  }
);
```

#### 채팅방 구독
```javascript
stompClient.subscribe('/topic/chat/' + roomId, function(message) {
  const msg = JSON.parse(message.body);
  console.log('Received:', msg);
});
```

#### 메시지 전송
```javascript
const message = {
  content: "안녕하세요!",
  messageType: "TEXT",
  metadata: null
};

stompClient.send('/app/chat/' + roomId, {}, JSON.stringify(message));
```

## 테스트 방법

### 1. 쉘 스크립트로 REST API 테스트
```bash
./test-chat-api.sh
```

### 2. 웹 브라우저로 WebSocket 테스트
```
http://localhost:8080/chat-test.html
```

테스트 페이지에서:
1. 매장(store1) 또는 유통업체(dist1)로 로그인
2. 채팅방 생성
3. WebSocket 연결
4. 메시지 전송/수신 테스트

### 3. 두 개의 브라우저로 실시간 채팅 테스트
- 브라우저 1: 매장(store1)으로 로그인
- 브라우저 2: 유통업체(dist1)로 로그인
- 같은 채팅방에 연결하여 실시간 메시지 교환

## 데이터베이스 스키마

### chat_rooms 테이블
```sql
CREATE TABLE chat_rooms (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  room_id VARCHAR(255) UNIQUE NOT NULL,
  store_id VARCHAR(255) NOT NULL,
  distributor_id VARCHAR(255) NOT NULL,
  store_name VARCHAR(255),
  distributor_name VARCHAR(255),
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  last_message_at TIMESTAMP
);
```

### chat_messages 테이블
```sql
CREATE TABLE chat_messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  room_id VARCHAR(255) NOT NULL,
  sender_id VARCHAR(255) NOT NULL,
  sender_type VARCHAR(50) NOT NULL,
  message_type VARCHAR(50) NOT NULL,
  content VARCHAR(2000) NOT NULL,
  metadata VARCHAR(1000),
  is_read BOOLEAN DEFAULT false,
  created_at TIMESTAMP,
  INDEX idx_room_id (room_id),
  INDEX idx_created_at (created_at)
);
```

## 아키텍처

```
chat/
├── domain/
│   ├── ChatRoom.java              # 채팅방 엔티티
│   ├── ChatMessage.java           # 메시지 엔티티
│   ├── ChatRoomRepository.java
│   └── ChatMessageRepository.java
├── application/
│   └── ChatService.java           # 비즈니스 로직
├── presentation/
│   ├── ChatController.java        # REST API
│   ├── ChatWebSocketController.java  # WebSocket 핸들러
│   └── dto/
└── config/
    ├── WebSocketConfig.java       # WebSocket 설정
    └── WebSocketSecurityConfig.java  # WebSocket 보안
```

## 기존 기능과의 통합 예시

### 견적 요청 시 채팅방 자동 생성
```java
// QuoteService.java
@Transactional
public Quote createQuote(String storeId, String distributorId, ...) {
    // 견적 생성
    Quote quote = new Quote(...);
    quoteRepository.save(quote);
    
    // 채팅방 자동 생성
    ChatRoom chatRoom = chatService.getOrCreateChatRoom(storeId, distributorId);
    
    // 시스템 메시지 전송
    chatService.sendMessage(
        chatRoom.getRoomId(),
        "SYSTEM",
        ChatMessage.SenderType.STORE,
        ChatMessage.MessageType.QUOTE_REQUEST,
        "견적 요청이 전송되었습니다.",
        "{\"quoteId\": " + quote.getId() + "}"
    );
    
    return quote;
}
```

### 주문 완료 시 알림
```java
// OrderService.java
@Transactional
public Order completeOrder(Long orderId) {
    Order order = orderRepository.findById(orderId)...;
    order.complete();
    
    // 채팅방에 시스템 메시지
    ChatRoom chatRoom = chatService.getOrCreateChatRoom(
        order.getStoreId(), 
        order.getDistributorId()
    );
    
    chatService.sendMessage(
        chatRoom.getRoomId(),
        "SYSTEM",
        ChatMessage.SenderType.STORE,
        ChatMessage.MessageType.SYSTEM,
        "주문이 완료되었습니다. 주문번호: " + order.getOrderNumber(),
        "{\"orderId\": " + orderId + "}"
    );
    
    return order;
}
```

## 보안

- JWT 기반 인증
- 채팅방 접근 권한 검증 (해당 매장/유통업체만 접근 가능)
- WebSocket 연결 시 토큰 검증
- STOMP 메시지 레벨 보안

## 향후 개선 사항

1. **파일 첨부**: 이미지, 문서 파일 전송
2. **읽음 확인**: 실시간 읽음 상태 표시
3. **타이핑 인디케이터**: 상대방이 입력 중일 때 표시
4. **푸시 알림**: 새 메시지 도착 시 알림
5. **메시지 검색**: 채팅 내용 검색 기능
6. **메시지 삭제/수정**: 전송한 메시지 수정/삭제
7. **이모지 지원**: 이모지 반응 추가