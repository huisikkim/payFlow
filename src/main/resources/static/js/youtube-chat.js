// YouTube 익명 채팅 기능
let stompClient = null;
let isConnected = false;
let anonymousUsername = null;
let onlineUsers = 0;

// 랜덤 익명 사용자명 생성
function generateAnonymousUsername() {
    const adjectives = ['빠른', '용감한', '멋진', '귀여운', '강한', '똑똑한', '재미있는', '신비한'];
    const nouns = ['호랑이', '토끼', '독수리', '여우', '사자', '팬더', '늑대', '고양이'];
    const adj = adjectives[Math.floor(Math.random() * adjectives.length)];
    const noun = nouns[Math.floor(Math.random() * nouns.length)];
    const num = Math.floor(Math.random() * 1000);
    return `${adj}${noun}${num}`;
}

// 타이핑 중인 사용자 목록
let typingUsers = new Set();
let typingTimeout = null;

// WebSocket 연결
function connectChat() {
    if (isConnected) return;
    
    anonymousUsername = generateAnonymousUsername();
    
    const socket = new SockJS('/ws/youtube-chat');
    stompClient = Stomp.over(socket);
    
    // 디버그 로그 비활성화
    stompClient.debug = null;
    
    // 연결 헤더에 익명 사용자 정보 추가
    const headers = {
        'X-Anonymous-User': anonymousUsername
    };
    
    stompClient.connect(headers, function(frame) {
        console.log('YouTube 채팅 연결됨:', anonymousUsername);
        isConnected = true;
        
        // 채팅방 구독
        stompClient.subscribe('/topic/youtube/popular', function(message) {
            const data = JSON.parse(message.body);
            displayMessage(data);
        });
        
        // 타이핑 인디케이터 구독
        stompClient.subscribe('/topic/youtube/popular/typing', function(message) {
            const data = JSON.parse(message.body);
            handleTypingIndicator(data);
        });
        
        // 입장 알림
        stompClient.send('/app/youtube/join', {}, JSON.stringify({}));
        
        // 입력창 활성화
        document.getElementById('chat-input').disabled = false;
        document.getElementById('btn-send').disabled = false;
        
    }, function(error) {
        console.error('채팅 연결 실패:', error);
        isConnected = false;
        
        // 재연결 시도
        setTimeout(connectChat, 3000);
    });
}

// 메시지 전송
function sendMessage() {
    const input = document.getElementById('chat-input');
    const content = input.value.trim();
    
    if (!content || !isConnected) return;
    
    const message = {
        content: content
    };
    
    stompClient.send('/app/youtube/chat', {}, JSON.stringify(message));
    input.value = '';
}

// 메시지 표시
function displayMessage(data) {
    const messagesContainer = document.getElementById('chat-messages');
    const messageDiv = document.createElement('div');
    
    // 내 메시지인지 확인
    const isMyMessage = data.username === anonymousUsername;
    messageDiv.className = `chat-message ${data.type || 'message'} ${isMyMessage ? 'my-message' : 'other-message'}`;
    
    if (data.type === 'join') {
        messageDiv.innerHTML = `
            <div class="chat-message-content">${data.content}</div>
        `;
    } else {
        messageDiv.innerHTML = `
            <div class="chat-message-header">
                <span class="chat-username">${escapeHtml(data.username)}</span>
                <span class="chat-timestamp">${data.timestamp}</span>
            </div>
            <div class="chat-message-content">${escapeHtml(data.content)}</div>
        `;
    }
    
    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
    
    // 메시지가 100개 이상이면 오래된 메시지 삭제
    if (messagesContainer.children.length > 100) {
        messagesContainer.removeChild(messagesContainer.firstChild);
    }
}

// HTML 이스케이프
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 타이핑 인디케이터 처리
function handleTypingIndicator(data) {
    // 자기 자신의 타이핑은 무시
    if (data.username === anonymousUsername) {
        return;
    }
    
    if (data.isTyping) {
        typingUsers.add(data.username);
    } else {
        typingUsers.delete(data.username);
    }
    
    updateTypingIndicator();
}

// 타이핑 인디케이터 UI 업데이트
function updateTypingIndicator() {
    let indicator = document.getElementById('typing-indicator');
    
    if (typingUsers.size === 0) {
        if (indicator) {
            indicator.remove();
        }
        return;
    }
    
    if (!indicator) {
        indicator = document.createElement('div');
        indicator.id = 'typing-indicator';
        indicator.className = 'typing-indicator';
        document.getElementById('chat-messages').appendChild(indicator);
    }
    
    const userList = Array.from(typingUsers);
    let text = '';
    
    if (userList.length === 1) {
        text = `${userList[0]}님이 입력 중`;
    } else if (userList.length === 2) {
        text = `${userList[0]}님, ${userList[1]}님이 입력 중`;
    } else {
        text = `${userList[0]}님 외 ${userList.length - 1}명이 입력 중`;
    }
    
    indicator.innerHTML = `
        <div class="typing-dots">
            <span></span><span></span><span></span>
        </div>
        <span class="typing-text">${text}</span>
    `;
    
    // 자동 스크롤
    const messagesContainer = document.getElementById('chat-messages');
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// 타이핑 이벤트 전송
function sendTypingIndicator(isTyping) {
    if (!isConnected) return;
    
    const data = {
        isTyping: isTyping
    };
    
    stompClient.send('/app/youtube/typing', {}, JSON.stringify(data));
}

// 입력 중 감지
function onInputChange() {
    // 타이핑 시작 알림
    sendTypingIndicator(true);
    
    // 기존 타이머 취소
    if (typingTimeout) {
        clearTimeout(typingTimeout);
    }
    
    // 1초 후 타이핑 중지 알림
    typingTimeout = setTimeout(() => {
        sendTypingIndicator(false);
    }, 1000);
}

// 채팅창 최소화/최대화
function toggleChat() {
    const chatContainer = document.querySelector('.chat-container');
    chatContainer.classList.toggle('minimized');
    
    const icon = document.querySelector('.btn-minimize .material-symbols-outlined');
    if (chatContainer.classList.contains('minimized')) {
        icon.textContent = 'expand_less';
    } else {
        icon.textContent = 'expand_more';
    }
}

// Enter 키로 메시지 전송
document.addEventListener('DOMContentLoaded', function() {
    const chatInput = document.getElementById('chat-input');
    if (chatInput) {
        // Enter 키로 전송
        chatInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
                // 타이핑 중지 알림
                if (typingTimeout) {
                    clearTimeout(typingTimeout);
                }
                sendTypingIndicator(false);
            }
        });
        
        // 입력 중 감지
        chatInput.addEventListener('input', onInputChange);
    }
    
    // 채팅 연결
    connectChat();
});

// 페이지 언로드 시 연결 해제
window.addEventListener('beforeunload', function() {
    if (stompClient && isConnected) {
        stompClient.disconnect();
    }
});
