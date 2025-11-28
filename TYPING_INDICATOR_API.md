# 타이핑 인디케이터 API 문서

## ✅ 구현 완료

백엔드에서 타이핑 인디케이터 기능이 구현되었습니다.

---

## 📡 WebSocket 엔드포인트

### 연결
```
ws://localhost:8080/ws/chat
```

### 인증
WebSocket 연결 시 STOMP 헤더에 JWT 토큰 포함:
```
Authorization: Bearer {your_jwt_token}
```

---

## 🔄 타이핑 이벤트 전송

### 엔드포인트
```
/app/chat/{roomId}/typing
```

### 요청 형식

#### 타이핑 시작
```json
{
  "roomId": "room123",
  "isTyping": true
}
```

#### 타이핑 중단
```json
{
  "roomId": "room123",
  "isTyping": false
}
```

### 주의사항
- `userId`와 `userName`은 서버에서 자동으로 설정되므로 클라이언트에서 보내지 않아도 됩니다
- 보안을 위해 JWT 토큰에서 추출한 사용자 정보를 사용합니다

---

## 📥 타이핑 이벤트 수신

### 구독 토픽
```
/topic/chat/{roomId}/typing
```

### 응답 형식
```json
{
  "roomId": "room123",
  "userId": "store1",
  "userName": "김밥천국 강남점",
  "isTyping": true,
  "timestamp": "2025-11-28T10:30:00"
}
```

### 필드 설명
| 필드 | 타입 | 설명 |
|------|------|------|
| roomId | String | 채팅방 ID |
| userId | String | 입력 중인 사용자 ID (서버에서 설정) |
| userName | String | 입력 중인 사용자 이름 (매장명 또는 유통업체명) |
| isTyping | boolean | true: 입력 중, false: 입력 중단 |
| timestamp | String | 이벤트 발생 시간 (ISO 8601 형식) |

---

## 🔒 보안 및 권한

### 자동 검증 사항
1. **인증 확인**: JWT 토큰이 유효한지 확인
2. **채팅방 접근 권한**: 사용자가 해당 채팅방에 접근 권한이 있는지 확인
3. **사용자 정보 보호**: 클라이언트가 보낸 userId는 무시하고 서버에서 JWT 토큰으로 확인한 정보 사용

### 권한 검증 로직
- 매장 사용자: 해당 채팅방의 storeId와 일치해야 함
- 유통업체 사용자: 해당 채팅방의 distributorId와 일치해야 함

---

## 💻 Flutter 구현 예제

### 1. 패키지 추가
```yaml
dependencies:
  stomp_dart_client: ^1.0.0
```

### 2. WebSocket 연결
```dart
import 'package:stomp_dart_client/stomp_dart_client.dart';

class ChatService {
  StompClient? stompClient;
  String? currentRoomId;
  
  void connect(String token, String roomId) {
    currentRoomId = roomId;
    
    stompClient = StompClient(
      config: StompConfig(
        url: 'http://localhost:8080/ws/chat',
        onConnect: (StompFrame frame) {
          print('✅ WebSocket 연결 성공');
          
          // 타이핑 이벤트 구독
          subscribeToTyping(roomId);
        },
        onWebSocketError: (dynamic error) {
          print('❌ WebSocket 에러: $error');
        },
        stompConnectHeaders: {
          'Authorization': 'Bearer $token',
        },
        webSocketConnectHeaders: {
          'Authorization': 'Bearer $token',
        },
      ),
    );
    
    stompClient?.activate();
  }
  
  void disconnect() {
    stompClient?.deactivate();
  }
}
```

### 3. 타이핑 이벤트 구독
```dart
import 'dart:convert';

void subscribeToTyping(String roomId) {
  stompClient?.subscribe(
    destination: '/topic/chat/$roomId/typing',
    callback: (StompFrame frame) {
      if (frame.body != null) {
        final event = json.decode(frame.body!);
        
        print('📥 타이핑 이벤트 수신: $event');
        
        // UI 업데이트
        if (event['isTyping'] == true) {
          showTypingIndicator(event['userName'] ?? event['userId']);
        } else {
          hideTypingIndicator();
        }
      }
    },
  );
}
```

### 4. 타이핑 이벤트 전송
```dart
void sendTypingEvent(String roomId, bool isTyping) {
  if (stompClient == null || !stompClient!.connected) {
    print('⚠️ WebSocket이 연결되지 않았습니다');
    return;
  }
  
  final event = {
    'roomId': roomId,
    'isTyping': isTyping,
  };
  
  stompClient?.send(
    destination: '/app/chat/$roomId/typing',
    body: json.encode(event),
  );
  
  print('📤 타이핑 이벤트 전송: isTyping=$isTyping');
}
```

### 5. TextField에 통합
```dart
class ChatInputWidget extends StatefulWidget {
  final String roomId;
  final ChatService chatService;
  
  const ChatInputWidget({
    required this.roomId,
    required this.chatService,
  });
  
  @override
  State<ChatInputWidget> createState() => _ChatInputWidgetState();
}

class _ChatInputWidgetState extends State<ChatInputWidget> {
  final TextEditingController _controller = TextEditingController();
  Timer? _typingTimer;
  bool _isTyping = false;
  
  @override
  void initState() {
    super.initState();
    _controller.addListener(_onTextChanged);
  }
  
  void _onTextChanged() {
    // 타이핑 시작
    if (!_isTyping && _controller.text.isNotEmpty) {
      _isTyping = true;
      widget.chatService.sendTypingEvent(widget.roomId, true);
    }
    
    // 기존 타이머 취소
    _typingTimer?.cancel();
    
    // 2초 후 타이핑 중단
    _typingTimer = Timer(Duration(seconds: 2), () {
      if (_isTyping) {
        _isTyping = false;
        widget.chatService.sendTypingEvent(widget.roomId, false);
      }
    });
  }
  
  @override
  void dispose() {
    _typingTimer?.cancel();
    if (_isTyping) {
      widget.chatService.sendTypingEvent(widget.roomId, false);
    }
    _controller.dispose();
    super.dispose();
  }
  
  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: _controller,
      decoration: InputDecoration(
        hintText: '메시지를 입력하세요...',
      ),
      onSubmitted: (text) {
        // 메시지 전송 시 타이핑 중단
        if (_isTyping) {
          _isTyping = false;
          widget.chatService.sendTypingEvent(widget.roomId, false);
        }
        // 메시지 전송 로직...
      },
    );
  }
}
```

### 6. 타이핑 인디케이터 UI
```dart
class TypingIndicator extends StatelessWidget {
  final String? typingUserName;
  
  const TypingIndicator({this.typingUserName});
  
  @override
  Widget build(BuildContext context) {
    if (typingUserName == null) {
      return SizedBox.shrink();
    }
    
    return Padding(
      padding: EdgeInsets.all(8.0),
      child: Row(
        children: [
          Text(
            '$typingUserName님이 입력 중',
            style: TextStyle(
              color: Colors.grey,
              fontStyle: FontStyle.italic,
              fontSize: 12,
            ),
          ),
          SizedBox(width: 4),
          SizedBox(
            width: 12,
            height: 12,
            child: CircularProgressIndicator(
              strokeWidth: 2,
              valueColor: AlwaysStoppedAnimation<Color>(Colors.grey),
            ),
          ),
        ],
      ),
    );
  }
}
```

---

## 🧪 테스트 방법

### 백엔드 테스트
```bash
# 테스트 스크립트 실행
./test-typing-indicator.sh

# HTML 테스트 페이지 열기
open test-typing-websocket.html
```

### Flutter 테스트 시나리오
1. **기본 동작 테스트**
   - 사용자 A가 입력 시작 → 사용자 B에게 "입력 중..." 표시
   - 사용자 A가 입력 중단 → 사용자 B의 인디케이터 사라짐

2. **권한 테스트**
   - 채팅방에 속하지 않은 사용자가 타이핑 이벤트 전송 → 무시됨
   - 잘못된 토큰으로 연결 시도 → 연결 실패

3. **타임아웃 테스트**
   - 2초간 입력 없으면 자동으로 타이핑 중단 이벤트 전송
   - 메시지 전송 시 타이핑 중단 이벤트 전송

---

## 📊 성능 고려사항

### Rate Limiting (권장)
클라이언트에서 타이핑 이벤트를 너무 자주 보내지 않도록 제한:
- 최소 간격: 500ms
- 디바운싱: 2초

### 예제 (Flutter)
```dart
Timer? _debounceTimer;
DateTime? _lastTypingSent;

void sendTypingEventThrottled(String roomId, bool isTyping) {
  final now = DateTime.now();
  
  // 500ms 이내에는 재전송하지 않음
  if (_lastTypingSent != null && 
      now.difference(_lastTypingSent!).inMilliseconds < 500) {
    return;
  }
  
  _lastTypingSent = now;
  sendTypingEvent(roomId, isTyping);
}
```

---

## 🐛 트러블슈팅

### 문제: 타이핑 이벤트가 수신되지 않음
**해결방법:**
1. WebSocket 연결 상태 확인
2. 올바른 토픽 구독 확인: `/topic/chat/{roomId}/typing`
3. JWT 토큰 유효성 확인
4. 채팅방 접근 권한 확인

### 문제: 자신의 타이핑 이벤트도 수신됨
**해결방법:**
```dart
void subscribeToTyping(String roomId, String myUserId) {
  stompClient?.subscribe(
    destination: '/topic/chat/$roomId/typing',
    callback: (StompFrame frame) {
      if (frame.body != null) {
        final event = json.decode(frame.body!);
        
        // 자신의 이벤트는 무시
        if (event['userId'] == myUserId) {
          return;
        }
        
        // 다른 사용자의 타이핑 이벤트만 처리
        if (event['isTyping'] == true) {
          showTypingIndicator(event['userName']);
        } else {
          hideTypingIndicator();
        }
      }
    },
  );
}
```

### 문제: 타이핑 인디케이터가 사라지지 않음
**해결방법:**
- 클라이언트에서 5-10초 타임아웃 설정
- 입력 중단 이벤트를 명시적으로 전송

---

## 📝 체크리스트

### 백엔드 (완료 ✅)
- [x] `TypingEvent` DTO 클래스 생성
- [x] `ChatWebSocketController`에 `handleTyping` 메서드 추가
- [x] `/app/chat/{roomId}/typing` 엔드포인트 구현
- [x] `/topic/chat/{roomId}/typing` 브로드캐스트 구현
- [x] 채팅방 접근 권한 검증 추가
- [x] 사용자 정보 자동 설정 (보안)
- [x] 테스트 스크립트 작성

### 프론트엔드 (Flutter 팀 작업)
- [ ] `stomp_dart_client` 패키지 추가
- [ ] WebSocket 연결 구현
- [ ] 타이핑 이벤트 구독 구현
- [ ] 타이핑 이벤트 전송 구현
- [ ] TextField에 타이핑 감지 통합
- [ ] 타이핑 인디케이터 UI 구현
- [ ] Rate limiting/디바운싱 적용
- [ ] 테스트 완료

---

## 🚀 배포 정보

### 환경별 엔드포인트
- **개발**: `ws://localhost:8080/ws/chat`
- **스테이징**: `wss://staging.example.com/ws/chat`
- **프로덕션**: `wss://api.example.com/ws/chat`

### 주의사항
- 프로덕션에서는 반드시 WSS (WebSocket Secure) 사용
- CORS 설정 확인
- 방화벽에서 WebSocket 포트 허용

---

## 📞 문의

백엔드 구현 완료되었습니다. 
테스트 중 문제가 있으면 로그를 확인해주세요.

**로그 확인:**
```bash
tail -f boot-run.log | grep -i typing
```
