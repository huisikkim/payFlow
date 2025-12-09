# 🔥 거래소 시세 비교 시스템

## 📋 개요

업비트와 빗썸의 실시간 코인 시세를 비교하고, 거래소 간 프리미엄과 차익거래 기회를 분석하는 시스템입니다.

## ✨ 주요 기능

### 1. 실시간 가격 비교
- **기준 거래소 선택**: 업비트 또는 빗썸을 기준으로 설정
- **프리미엄 계산**: 각 거래소의 가격 차이를 % 로 표시
- **VWAP 계산**: 거래량 기반 가중평균 가격 제공
- **색상 코딩**: 
  - 🔴 빨간색: 양수 프리미엄 (기준가보다 비쌈)
  - 🔵 파란색: 음수 프리미엄 (기준가보다 저렴)
  - ⚪ 회색: 미미한 차이 (±0.1% 이내)

### 2. 거래량 & 체결강도 분석
- **24시간 거래대금**: 각 거래소별 거래량 비교
- **1분/5분 거래량 변화율**: 단기 거래량 추세 파악
- **체결강도**: 매수/매도 비율 분석
  - 120% 이상: 매수 우위 (BUY_DOMINANT)
  - 80% 이하: 매도 우위 (SELL_DOMINANT)
  - 80~120%: 중립 (NEUTRAL)

### 3. 차익거래 기회 탐지
- **프리미엄 1% 이상**: 차익거래 가능한 코인 자동 필터링
- **예상 수익률**: 수수료 고려한 실제 수익률 계산
- **실시간 알림**: 차익거래 기회 발생 시 즉시 표시

### 4. 실시간 스트리밍
- **Server-Sent Events (SSE)**: WebFlux 기반 실시간 데이터 스트리밍
- **1초 간격 업데이트**: 최신 시세 정보 자동 갱신
- **자동 재연결**: 연결 끊김 시 자동 복구

## 🏗️ 아키텍처

### 기술 스택
- **Backend**: Spring Boot 3.5.7 + Spring WebFlux
- **WebSocket**: Reactor Netty (업비트/빗썸 웹소켓 연결)
- **Frontend**: Vanilla JavaScript + SSE
- **데이터 처리**: Reactive Streams (Flux/Mono)

### 계층 구조
```
presentation/
├── ExchangeComparisonController.java    # REST API (WebFlux)
└── CryptoComparisonWebController.java   # 웹 UI

application/
├── UpbitWebSocketService.java           # 업비트 웹소켓 클라이언트
├── BithumbWebSocketService.java         # 빗썸 웹소켓 클라이언트
└── ExchangeComparisonServiceV2.java     # 비교 로직 (프리미엄, VWAP, 체결강도)

domain/
├── StandardizedTicker.java              # 표준화된 시세 데이터
├── ExchangePremium.java                 # 프리미엄 비교 데이터
├── TradeStrength.java                   # 체결강도 데이터
└── OrderbookData.java                   # 호가 데이터
```

## 📊 데이터 모델

### ExchangePremium (프리미엄 비교)
```java
{
  "market": "KRW-BTC",
  "koreanName": "비트코인",
  "referenceExchange": "UPBIT",
  "referencePrice": 50000000,
  "exchangePrices": {
    "UPBIT": {
      "price": 50000000,
      "premium": 0.0,
      "spread": 0,
      "volume24h": 1000000000
    },
    "BITHUMB": {
      "price": 50500000,
      "premium": 1.0,
      "spread": 500000,
      "volume24h": 800000000
    }
  },
  "vwap": 50222222,
  "maxPremium": 1.0,
  "minPremium": 0.0
}
```

### 계산 로직

#### 1. 프리미엄 계산
```java
premium = ((targetPrice - referencePrice) / referencePrice) * 100
```

#### 2. VWAP 계산
```java
vwap = (price1 * volume1 + price2 * volume2) / (volume1 + volume2)
```

#### 3. 체결강도 계산
```java
strength = (buyVolume / sellVolume) * 100
// 실제로는 거래량 변화율로 추정
```

## 🚀 API 엔드포인트

### REST API

#### 1. 모든 코인 프리미엄 조회
```bash
GET /api/crypto/comparison/premiums?referenceExchange=UPBIT
```

#### 2. 특정 코인 프리미엄 조회
```bash
GET /api/crypto/comparison/premiums/KRW-BTC?referenceExchange=UPBIT
```

#### 3. 실시간 프리미엄 스트리밍 (SSE)
```bash
GET /api/crypto/comparison/premiums/stream?referenceExchange=UPBIT
```

#### 4. 체결강도 조회
```bash
GET /api/crypto/comparison/trade-strength/UPBIT/KRW-BTC
```

#### 5. 차익거래 기회 찾기
```bash
GET /api/crypto/comparison/arbitrage?referenceExchange=UPBIT&minPremium=1.0
```

#### 6. 차익거래 기회 실시간 스트리밍
```bash
GET /api/crypto/comparison/arbitrage/stream?referenceExchange=UPBIT&minPremium=1.0
```

### 웹 UI
```
http://localhost:8080/crypto/comparison
```

## 🎨 UI 구성

### 1. 헤더
- 제목
- 기준 거래소 선택 드롭다운

### 2. 통계 바
- 총 코인 수
- 최대 프리미엄
- 차익거래 기회 수
- 평균 VWAP

### 3. 탭 메뉴
- **가격 비교**: 거래소별 가격 및 프리미엄
- **거래량 & 체결강도**: 거래량 분석
- **차익거래 기회**: 1% 이상 프리미엄 코인

### 4. 검색 기능
- 코인명 또는 심볼로 실시간 필터링

## 🧪 테스트

### 테스트 스크립트 실행
```bash
./test-crypto-comparison.sh
```

### 수동 테스트
```bash
# 1. 프리미엄 조회
curl http://localhost:8080/api/crypto/comparison/premiums?referenceExchange=UPBIT | jq '.'

# 2. 실시간 스트리밍 (5초간)
timeout 5 curl http://localhost:8080/api/crypto/comparison/premiums/stream?referenceExchange=UPBIT

# 3. 차익거래 기회
curl http://localhost:8080/api/crypto/comparison/arbitrage?minPremium=1.0 | jq '.'
```

## 📈 사용 시나리오

### 시나리오 1: 차익거래 기회 찾기
1. 웹 UI 접속: `http://localhost:8080/crypto/comparison`
2. "차익거래 기회" 탭 클릭
3. 프리미엄 1% 이상인 코인 확인
4. 저렴한 거래소에서 매수 → 비싼 거래소에서 매도

### 시나리오 2: 시장 심리 파악
1. "거래량 & 체결강도" 탭 클릭
2. 체결강도 120% 이상 → 매수 우위 (상승 가능성)
3. 체결강도 80% 이하 → 매도 우위 (하락 가능성)

### 시나리오 3: 최적 거래소 선택
1. 기준 거래소를 업비트로 설정
2. 각 코인의 프리미엄 확인
3. 음수 프리미엄 거래소에서 매수 (더 저렴)

## 🔧 설정

### 구독 코인 변경
`UpbitWebSocketService.java` 및 `BithumbWebSocketService.java`에서 수정:
```java
List<String> defaultMarkets = Arrays.asList(
    "KRW-BTC", "KRW-ETH", "KRW-XRP", // 원하는 코인 추가
);
```

### 스트리밍 주기 변경
`ExchangeComparisonController.java`에서 수정:
```java
Flux.interval(Duration.ofSeconds(1))  // 1초 → 원하는 주기로 변경
```

### 차익거래 최소 프리미엄 변경
API 호출 시 파라미터로 지정:
```bash
?minPremium=2.0  # 2% 이상
```

## 🎯 핵심 알고리즘

### 1. 프리미엄 계산
```java
private BigDecimal calculatePremiumPercent(BigDecimal targetPrice, BigDecimal referencePrice) {
    return targetPrice.subtract(referencePrice)
        .divide(referencePrice, 6, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
}
```

### 2. VWAP 계산
```java
private BigDecimal calculateVWAP(BigDecimal price1, BigDecimal volume1, 
                                  BigDecimal price2, BigDecimal volume2) {
    BigDecimal totalVolume = volume1.add(volume2);
    BigDecimal weightedSum = price1.multiply(volume1).add(price2.multiply(volume2));
    return weightedSum.divide(totalVolume, 2, RoundingMode.HALF_UP);
}
```

### 3. 거래량 변화율 계산
```java
private BigDecimal calculateVolumeChange(List<VolumeSnapshot> history, int minutes) {
    BigDecimal oldVolume = getVolumeAt(history, minutes);
    BigDecimal newVolume = getCurrentVolume(history);
    return newVolume.subtract(oldVolume)
        .divide(oldVolume, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
}
```

## 🚨 주의사항

1. **수수료 고려**: 실제 차익거래 시 각 거래소의 수수료(약 0.05~0.25%) 고려 필요
2. **슬리피지**: 대량 거래 시 호가 스프레드로 인한 가격 변동 발생 가능
3. **출금 시간**: 거래소 간 코인 이동 시간(10~30분) 고려
4. **시장 변동성**: 프리미엄은 실시간으로 변하므로 빠른 판단 필요

## 🎨 새로 추가된 기능

### 1. 호가 스프레드 비교 ✅
- 거래소별 최우선 매수/매도 호가 조회
- 스프레드 및 스프레드 비율 계산
- REST API로 실시간 호가 데이터 제공

### 2. 실시간 차트 오버레이 ✅
- Chart.js를 사용한 라인 차트
- 업비트, 빗썸, VWAP 가격 동시 표시
- 30개 데이터 포인트 유지 (약 30초 히스토리)
- 코인 선택 기능

### 3. API 문서 페이지 ✅
- 인터랙티브 API 문서
- 각 엔드포인트별 상세 설명
- 요청/응답 예제
- 바로 테스트 가능한 버튼

### 4. 개선된 UI/UX ✅
- 5개 탭 구조 (가격 비교, 호가 스프레드, 거래량, 차익거래, 차트)
- 다크 테마 디자인
- 실시간 검색 필터링
- 통계 대시보드

## 🌐 웹 페이지

### 실시간 비교 UI
```
http://localhost:8080/crypto/comparison
```

### API 문서
```
http://localhost:8080/crypto/api-docs
```

## 📝 향후 개선 사항

- [ ] 더 많은 거래소 추가 (코인원, 코빗 등)
- [ ] 급등/급락 알림 기능 (WebSocket Push)
- [ ] 과거 프리미엄 데이터 저장 및 분석
- [ ] 모바일 반응형 UI 개선
- [ ] 실제 체결 데이터 기반 체결강도 계산
- [ ] 텔레그램/슬랙 알림 연동

## 🤝 기여

버그 리포트나 기능 제안은 이슈로 등록해주세요!

## 📄 라이선스

MIT License
