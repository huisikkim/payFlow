#!/bin/bash

# 타이핑 인디케이터 테스트 스크립트
# WebSocket을 통한 타이핑 이벤트 전송 테스트

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "타이핑 인디케이터 API 테스트"
echo "=========================================="
echo ""

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 1. 매장 로그인
echo -e "${BLUE}[1단계] 매장 로그인${NC}"
STORE_LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "store1",
    "password": "password123"
  }')

STORE_TOKEN=$(echo $STORE_LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$STORE_TOKEN" ]; then
    echo -e "${RED}❌ 매장 로그인 실패${NC}"
    echo "Response: $STORE_LOGIN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✅ 매장 로그인 성공${NC}"
echo "Token: ${STORE_TOKEN:0:20}..."
echo ""

# 2. 유통업체 로그인
echo -e "${BLUE}[2단계] 유통업체 로그인${NC}"
DIST_LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "dist1",
    "password": "password123"
  }')

DIST_TOKEN=$(echo $DIST_LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$DIST_TOKEN" ]; then
    echo -e "${RED}❌ 유통업체 로그인 실패${NC}"
    echo "Response: $DIST_LOGIN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✅ 유통업체 로그인 성공${NC}"
echo "Token: ${DIST_TOKEN:0:20}..."
echo ""

# 3. 채팅방 생성 또는 조회
echo -e "${BLUE}[3단계] 채팅방 생성/조회${NC}"
CHATROOM_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/chat/rooms" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${STORE_TOKEN}" \
  -d '{
    "storeId": "store1",
    "distributorId": "dist1"
  }')

ROOM_ID=$(echo $CHATROOM_RESPONSE | grep -o '"roomId":"[^"]*' | cut -d'"' -f4)

if [ -z "$ROOM_ID" ]; then
    echo -e "${RED}❌ 채팅방 생성 실패${NC}"
    echo "Response: $CHATROOM_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✅ 채팅방 생성 성공${NC}"
echo "Room ID: $ROOM_ID"
echo ""

# 4. WebSocket 연결 안내
echo -e "${YELLOW}=========================================="
echo "WebSocket 타이핑 인디케이터 테스트 안내"
echo "==========================================${NC}"
echo ""
echo -e "${BLUE}📡 WebSocket 엔드포인트:${NC}"
echo "   연결: ws://localhost:8080/ws/chat"
echo ""
echo -e "${BLUE}📤 타이핑 시작 전송:${NC}"
echo "   Destination: /app/chat/${ROOM_ID}/typing"
echo "   Body:"
echo '   {
     "roomId": "'${ROOM_ID}'",
     "isTyping": true
   }'
echo ""
echo -e "${BLUE}📥 타이핑 이벤트 구독:${NC}"
echo "   Destination: /topic/chat/${ROOM_ID}/typing"
echo ""
echo -e "${BLUE}📤 타이핑 중단 전송:${NC}"
echo "   Destination: /app/chat/${ROOM_ID}/typing"
echo "   Body:"
echo '   {
     "roomId": "'${ROOM_ID}'",
     "isTyping": false
   }'
echo ""
echo -e "${BLUE}🔑 인증 헤더:${NC}"
echo "   매장: Authorization: Bearer ${STORE_TOKEN:0:30}..."
echo "   유통업체: Authorization: Bearer ${DIST_TOKEN:0:30}..."
echo ""

# 5. JavaScript 테스트 코드 생성
echo -e "${BLUE}[5단계] JavaScript 테스트 코드 생성${NC}"
cat > test-typing-websocket.html << 'HTMLEOF'
<!DOCTYPE html>
<html>
<head>
    <title>타이핑 인디케이터 테스트</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
    <style>
        body { font-family: Arial, sans-serif; padding: 20px; }
        .container { max-width: 800px; margin: 0 auto; }
        .section { margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }
        button { padding: 10px 20px; margin: 5px; cursor: pointer; }
        .log { background: #f5f5f5; padding: 10px; margin: 10px 0; border-radius: 3px; max-height: 300px; overflow-y: auto; }
        .typing-indicator { color: #666; font-style: italic; }
        .success { color: green; }
        .error { color: red; }
        input { padding: 8px; width: 300px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🔔 타이핑 인디케이터 테스트</h1>
        
        <div class="section">
            <h3>1. 연결 설정</h3>
            <label>토큰: <input type="text" id="token" placeholder="Bearer 토큰 입력" /></label><br/>
            <label>Room ID: <input type="text" id="roomId" placeholder="채팅방 ID" /></label><br/>
            <button onclick="connect()">연결</button>
            <button onclick="disconnect()">연결 해제</button>
            <div id="connectionStatus">상태: 연결 안됨</div>
        </div>
        
        <div class="section">
            <h3>2. 타이핑 이벤트 전송</h3>
            <button onclick="sendTypingStart()">입력 시작</button>
            <button onclick="sendTypingStop()">입력 중단</button>
        </div>
        
        <div class="section">
            <h3>3. 수신된 타이핑 이벤트</h3>
            <div id="typingIndicator" class="typing-indicator"></div>
        </div>
        
        <div class="section">
            <h3>4. 로그</h3>
            <button onclick="clearLog()">로그 지우기</button>
            <div id="log" class="log"></div>
        </div>
    </div>

    <script>
        let stompClient = null;
        let typingTimeout = null;

        function log(message, type = 'info') {
            const logDiv = document.getElementById('log');
            const time = new Date().toLocaleTimeString();
            const color = type === 'error' ? 'red' : type === 'success' ? 'green' : 'black';
            logDiv.innerHTML += `<div style="color: ${color}">[${time}] ${message}</div>`;
            logDiv.scrollTop = logDiv.scrollHeight;
        }

        function connect() {
            const token = document.getElementById('token').value;
            const roomId = document.getElementById('roomId').value;
            
            if (!token || !roomId) {
                alert('토큰과 Room ID를 입력하세요');
                return;
            }

            const socket = new SockJS('http://localhost:8080/ws/chat');
            stompClient = Stomp.over(socket);
            
            stompClient.connect(
                {'Authorization': 'Bearer ' + token},
                function(frame) {
                    log('✅ WebSocket 연결 성공', 'success');
                    document.getElementById('connectionStatus').innerHTML = '<span class="success">상태: 연결됨</span>';
                    
                    // 타이핑 이벤트 구독
                    stompClient.subscribe('/topic/chat/' + roomId + '/typing', function(message) {
                        const event = JSON.parse(message.body);
                        log('📥 타이핑 이벤트 수신: ' + JSON.stringify(event, null, 2));
                        
                        const indicator = document.getElementById('typingIndicator');
                        if (event.isTyping) {
                            indicator.textContent = `${event.userName || event.userId}님이 입력 중...`;
                            
                            // 자동으로 5초 후 사라지게
                            clearTimeout(typingTimeout);
                            typingTimeout = setTimeout(() => {
                                indicator.textContent = '';
                            }, 5000);
                        } else {
                            indicator.textContent = '';
                        }
                    });
                    
                    log('📡 구독 완료: /topic/chat/' + roomId + '/typing');
                },
                function(error) {
                    log('❌ 연결 실패: ' + error, 'error');
                    document.getElementById('connectionStatus').innerHTML = '<span class="error">상태: 연결 실패</span>';
                }
            );
        }

        function disconnect() {
            if (stompClient !== null) {
                stompClient.disconnect();
                log('연결 해제됨');
                document.getElementById('connectionStatus').innerHTML = '상태: 연결 안됨';
            }
        }

        function sendTypingStart() {
            if (stompClient === null || !stompClient.connected) {
                alert('먼저 연결하세요');
                return;
            }
            
            const roomId = document.getElementById('roomId').value;
            const event = {
                roomId: roomId,
                isTyping: true
            };
            
            stompClient.send('/app/chat/' + roomId + '/typing', {}, JSON.stringify(event));
            log('📤 타이핑 시작 전송: ' + JSON.stringify(event));
        }

        function sendTypingStop() {
            if (stompClient === null || !stompClient.connected) {
                alert('먼저 연결하세요');
                return;
            }
            
            const roomId = document.getElementById('roomId').value;
            const event = {
                roomId: roomId,
                isTyping: false
            };
            
            stompClient.send('/app/chat/' + roomId + '/typing', {}, JSON.stringify(event));
            log('📤 타이핑 중단 전송: ' + JSON.stringify(event));
        }

        function clearLog() {
            document.getElementById('log').innerHTML = '';
        }
    </script>
</body>
</html>
HTMLEOF

echo -e "${GREEN}✅ HTML 테스트 파일 생성 완료: test-typing-websocket.html${NC}"
echo ""

echo -e "${YELLOW}=========================================="
echo "테스트 방법"
echo "==========================================${NC}"
echo ""
echo "1. 브라우저에서 test-typing-websocket.html 파일을 엽니다"
echo "2. 토큰 입력란에 위의 토큰을 복사해서 붙여넣습니다"
echo "3. Room ID 입력란에 ${ROOM_ID}를 입력합니다"
echo "4. '연결' 버튼을 클릭합니다"
echo "5. '입력 시작' 버튼을 클릭하여 타이핑 이벤트를 전송합니다"
echo "6. 다른 브라우저 탭에서 다른 사용자로 연결하여 타이핑 인디케이터를 확인합니다"
echo ""
echo -e "${GREEN}✅ 모든 준비 완료!${NC}"
