# 🪙 실시간 코인 시세 가이드

업비트 웹소켓을 이용한 실시간 코인 시세 조회 기능입니다.

## 빠른 시작

### 1. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 2. 웹 브라우저에서 접속

```
http://localhost:8080/crypto
```

실시간으로 업데이트되는 코인 시세를 확인할 수 있습니다!

## 주요 기능

### 📊 실시간 시세 정보

각 코인 카드에서 다음 정보를 확인할 수 있습니다:

- **현재가**: 실시간 거래 가격
- **변동률**: 전일 대비 변동률 (%, 상승/하락 색상 구분)
- **전일대비**: 전일 대비 가격 변화
- **시가**: 당일 시작 가격
- **고가**: 당일 최고 가격 (빨간색)
- **저가**: 당일 최저 가격 (파란색)
- **24시간 거래대금**: 최근 24시간 총 거래 금액

### 🎨 UI 특징

- **실시간 업데이트**: 업비트로부터 실시간 데이터 수신
- **자동 정렬**: 거래대금 순으로 자동 정렬
- **색상 구분**: 
  - 🔴 상승: 빨간색
  - 🔵 하락: 파란색
  - ⚪ 보합: 회색
- **반응형 디자인**: 모바일/태블릿/데스크톱 모두 지원
- **연결 상태 표시**: 실시간 연결 상태 확인
- **자동 재연결**: 연결 끊김 시 자동으로 재연결

## API 사용법

### REST API

#### 전체 코인 시세 조회

```bash
curl http://localhost:8080/api/crypto/tickers
```

**응답 예시:**
```json
[
  {
    "market": "KRW-BTC",
    "koreanName": "비트코인",
    "tradePrice": 50000000,
    "changePrice": 1000000,
    "changeRate": 2.0,
    "signedChangeRate": 2.0,
    "highPrice": 51000000,
    "lowPrice": 49000000,
    "openingPrice": 49500000,
    "prevClosingPrice": 49000000,
    "accTradePrice24h": 500000000000,
    "accTradeVolume24h": 10000,
    "change": "RISE",
    "timestamp": "2025-11-20T10:30:00"
  }
]
```

#### 특정 코인 시세 조회

```bash
# 비트코인
curl http://localhost:8080/api/crypto/tickers/KRW-BTC

# 이더리움
curl http://localhost:8080/api/crypto/tickers/KRW-ETH

# 리플
curl http://localhost:8080/api/crypto/tickers/KRW-XRP
```

### WebSocket API

JavaScript에서 웹소켓 연결:

```javascript
const ws = new WebSocket('ws://localhost:8080/ws/crypto');

ws.onopen = () => {
    console.log('✅ 연결 성공');
};

ws.onmessage = (event) => {
    const ticker = JSON.parse(event.data);
    console.log(`${ticker.koreanName}: ${ticker.tradePrice}원`);
    
    // 실시간 데이터 처리
    updateUI(ticker);
};

ws.onerror = (error) => {
    console.error('❌ 연결 오류:', error);
};

ws.onclose = () => {
    console.log('❌ 연결 종료');
    // 재연결 로직
};
```

## 지원 코인 목록

현재 다음 10개 코인의 시세를 제공합니다:

| 코인 | 마켓 코드 | 한글명 |
|------|-----------|--------|
| Bitcoin | KRW-BTC | 비트코인 |
| Ethereum | KRW-ETH | 이더리움 |
| Ripple | KRW-XRP | 리플 |
| Cardano | KRW-ADA | 에이다 |
| Solana | KRW-SOL | 솔라나 |
| Dogecoin | KRW-DOGE | 도지코인 |
| Avalanche | KRW-AVAX | 아발란체 |
| Polygon | KRW-MATIC | 폴리곤 |
| Polkadot | KRW-DOT | 폴카닷 |
| Shiba Inu | KRW-SHIB | 시바이누 |

## 테스트

PowerShell 테스트 스크립트 실행:

```powershell
.\test-crypto-api.ps1
```

테스트 내용:
- ✅ 전체 코인 시세 조회
- ✅ 비트코인 상세 정보
- ✅ 이더리움 상세 정보

## 기술 스택

- **Backend**: Spring Boot 3.5.7, Java 17
- **WebSocket**: Spring WebSocket
- **외부 API**: 업비트 웹소켓 API (wss://api.upbit.com/websocket/v1)
- **JSON**: Gson
- **Frontend**: Vanilla JavaScript, HTML5, CSS3

## 아키텍처

```
┌─────────────────────┐
│  업비트 웹소켓 API   │
└──────────┬──────────┘
           │ 실시간 시세 데이터
           ↓
┌─────────────────────────────┐
│ UpbitWebSocketService       │
│ - 업비트 연결 관리           │
│ - 시세 데이터 캐싱           │
│ - 클라이언트 세션 관리       │
└──────────┬──────────────────┘
           │ 브로드캐스트
           ↓
┌─────────────────────────────┐
│ CryptoWebSocketHandler      │
│ - 클라이언트 연결 처리       │
│ - 메시지 전송                │
└──────────┬──────────────────┘
           │ WebSocket
           ↓
┌─────────────────────────────┐
│  웹 클라이언트 (브라우저)    │
│ - 실시간 UI 업데이트         │
│ - 자동 재연결                │
└─────────────────────────────┘
```

## DDD 패턴

```
crypto/
├── domain/
│   └── CoinTicker.java          # 도메인 모델
│       - 코인 시세 정보
│       - 불변 객체
│
├── application/
│   └── UpbitWebSocketService.java  # 애플리케이션 서비스
│       - 업비트 연결 관리
│       - 비즈니스 로직
│       - 데이터 캐싱
│
├── infrastructure/
│   ├── WebSocketConfig.java        # 인프라 설정
│   └── CryptoWebSocketHandler.java # 웹소켓 핸들러
│       - 기술적 구현
│
└── presentation/
    ├── CryptoController.java       # REST API
    └── CryptoWebController.java    # 웹 페이지
        - 사용자 인터페이스
```

## 확장 가능성

### 새로운 코인 추가

`UpbitWebSocketService.java`의 `initKoreanNames()` 메서드에 추가:

```java
koreanNames.put("KRW-ATOM", "코스모스");
```

그리고 `connectToUpbit()` 메서드의 `defaultMarkets`에 추가:

```java
List<String> defaultMarkets = Arrays.asList(
    "KRW-BTC", "KRW-ETH", "KRW-XRP", 
    "KRW-ATOM"  // 새로운 코인
);
```

### 추가 기능 아이디어

- 📈 차트 기능 (Chart.js)
- 🔔 가격 알림 (특정 가격 도달 시 알림)
- 📊 거래량 분석
- 💰 모의 투자 기능
- 📱 모바일 앱 연동
- 🔐 사용자별 관심 코인 설정

## 문제 해결

### 연결이 안 될 때

1. 애플리케이션이 실행 중인지 확인
2. 포트 8080이 사용 가능한지 확인
3. 방화벽 설정 확인
4. 브라우저 콘솔에서 에러 메시지 확인

### 데이터가 업데이트되지 않을 때

1. 웹소켓 연결 상태 확인 (화면 상단 상태 표시)
2. 브라우저 새로고침
3. 애플리케이션 로그 확인

### 로그 확인

```bash
# 애플리케이션 로그에서 웹소켓 관련 메시지 확인
tail -f boot-run.log | grep -i "websocket\|crypto\|upbit"
```

## 참고 자료

- [업비트 API 문서](https://docs.upbit.com/reference)
- [Spring WebSocket 문서](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [WebSocket API (MDN)](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket)

## 라이선스

이 프로젝트는 교육 목적으로 제작되었습니다.
업비트 API 사용 시 [업비트 이용약관](https://upbit.com/service_center/terms)을 준수해야 합니다.
