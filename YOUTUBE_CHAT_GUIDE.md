# YouTube 익명 채팅 기능 가이드

## 구현 완료 내용

YouTube 인기 영상 페이지(`/youtube/popular`)에 **로그인 없이 사용 가능한 실시간 익명 채팅** 기능을 추가했습니다.

## 주요 기능

### 1. 익명 채팅
- 로그인 없이 자동으로 랜덤 닉네임 생성 (예: "빠른호랑이123")
- 실시간 WebSocket 통신으로 즉시 메시지 전달
- 메시지는 DB에 저장하지 않고 실시간으로만 전송

### 2. 채팅 UI
- 우측 하단에 고정된 채팅창
- 최소화/최대화 기능
- 자동 스크롤
- 입장 알림 메시지

### 3. 보안
- 메시지 길이 제한 (500자)
- HTML 이스케이프 처리
- 익명 사용자 전용 엔드포인트 분리

## 기술 스택

- **Backend**: Spring WebSocket + STOMP
- **Frontend**: SockJS + STOMP.js
- **인증**: 익명 사용자 (JWT 불필요)

## 구현된 파일

### Backend
1. `WebSocketConfig.java` - `/ws/youtube-chat` 엔드포인트 추가
2. `WebSocketSecurityConfig.java` - 익명 사용자 인증 우회 로직
3. `YouTubeChatController.java` - 채팅 메시지 처리 컨트롤러

### Frontend
1. `youtube-chat.css` - 채팅 UI 스타일
2. `youtube-chat.js` - WebSocket 연결 및 메시지 처리
3. `popular.html` - 채팅 UI 추가

## 사용 방법

### 1. 서버 실행
```bash
./gradlew bootRun
```

### 2. 브라우저에서 접속
```
http://localhost:8080/youtube/popular
```

### 3. 채팅 사용
- 우측 하단 채팅창에서 메시지 입력
- Enter 키 또는 전송 버튼으로 메시지 전송
- 여러 브라우저/탭에서 동시 접속하여 테스트 가능

## 테스트 방법

### 다중 사용자 테스트
1. 여러 브라우저 창 열기 (Chrome, Firefox, Safari 등)
2. 각 창에서 `/youtube/popular` 접속
3. 각 창에서 메시지 전송
4. 모든 창에서 실시간으로 메시지 확인

### 기능 테스트
- ✅ 자동 닉네임 생성
- ✅ 실시간 메시지 전송/수신
- ✅ 입장 알림
- ✅ 채팅창 최소화/최대화
- ✅ 자동 스크롤
- ✅ Enter 키로 전송

## 주요 엔드포인트

### WebSocket
- **연결**: `/ws/youtube-chat`
- **구독**: `/topic/youtube/popular`
- **메시지 전송**: `/app/youtube/chat`
- **입장 알림**: `/app/youtube/join`

## 향후 개선 가능 사항

1. **온라인 사용자 수 표시**
   - 현재 접속 중인 사용자 수 실시간 표시

2. **이모지 지원**
   - 이모지 선택기 추가

3. **메시지 필터링**
   - 욕설 필터링
   - 스팸 방지

4. **채팅 기록**
   - 최근 N개 메시지 DB 저장 및 로드

5. **닉네임 변경**
   - 사용자가 원하는 닉네임으로 변경 가능

6. **영상별 채팅방**
   - 각 영상마다 독립적인 채팅방 생성

## 문제 해결

### 채팅이 연결되지 않을 때
1. 브라우저 콘솔에서 에러 확인
2. 서버 로그 확인
3. WebSocket 포트 확인 (기본: 8080)

### 메시지가 전송되지 않을 때
1. 네트워크 탭에서 WebSocket 연결 상태 확인
2. 메시지 길이 확인 (500자 제한)
3. 서버 로그에서 에러 확인

## 로그 확인

서버 로그에서 다음과 같은 메시지를 확인할 수 있습니다:

```
YouTube 익명 채팅 연결 - user: 빠른호랑이123
YouTube 채팅 입장 - user: 빠른호랑이123
YouTube 채팅 메시지 전송 - user: 빠른호랑이123, content: 안녕하세요!
```

## 보안 고려사항

- 익명 채팅이므로 악의적인 사용자 대응 필요
- 메시지 길이 제한으로 스팸 방지
- HTML 이스케이프로 XSS 공격 방지
- 필요시 IP 기반 Rate Limiting 추가 권장
