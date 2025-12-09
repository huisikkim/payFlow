# 🚀 거래소 비교 시스템 - 빠른 시작

## 📦 설치 및 실행

```bash
# 1. 애플리케이션 실행
./gradlew bootRun

# 2. 웹 브라우저에서 접속
# 실시간 UI: http://localhost:8080/crypto/comparison
# API 문서: http://localhost:8080/crypto/api-docs
```

## 🎯 주요 기능

| 기능 | 설명 | 엔드포인트 |
|------|------|-----------|
| 💰 프리미엄 비교 | 거래소 간 가격 차이 % 표시 | `/api/crypto/comparison/premiums` |
| 📊 VWAP 계산 | 거래량 기반 가중평균 가격 | 프리미엄 API에 포함 |
| ⚡ 실시간 스트리밍 | SSE로 1초 간격 업데이트 | `/api/crypto/comparison/premiums/stream` |
| 🎯 차익거래 탐지 | 1% 이상 프리미엄 자동 필터링 | `/api/crypto/comparison/arbitrage` |
| 📈 체결강도 | 1분/5분 거래량 변화율 | `/api/crypto/comparison/trade-strength/{exchange}/{market}` |
| 📉 호가 스프레드 | 매수/매도 호가 비교 | `/api/crypto/comparison/orderbook/{exchange}/{market}` |

## 🖥️ UI 화면

### 1. 가격 비교 탭
- 거래소별 현재가, 프리미엄, 스프레드, 거래량
- 색상 코딩: 🔴 양수 프리미엄 / 🔵 음수 프리미엄 / ⚪ 중립

### 2. 호가 스프레드 탭
- 거래소별 최우선 매수/매도 호가
- 스프레드 및 스프레드 비율

### 3. 거래량 & 체결강도 탭
- 24시간 거래대금
- 1분/5분 거래량 변화율
- 체결강도 시각화

### 4. 차익거래 기회 탭
- 프리미엄 1% 이상 코인만 표시
- 저렴한 거래소 vs 비싼 거래소
- 예상 수익률 계산

### 5. 실시간 차트 탭
- 업비트, 빗썸, VWAP 가격 동시 표시
- 코인 선택 가능
- 30초 히스토리

## 🧪 API 테스트

```bash
# 테스트 스크립트 실행
./test-crypto-comparison.sh

# 또는 수동 테스트
curl http://localhost:8080/api/crypto/comparison/premiums?referenceExchange=UPBIT | jq '.'
```

## 📊 계산 로직

### 프리미엄 계산
```
premium = ((targetPrice - referencePrice) / referencePrice) * 100
```

### VWAP 계산
```
vwap = (price1 * volume1 + price2 * volume2) / (volume1 + volume2)
```

### 체결강도 추정
```
strength = 100 + volumeChangeRate
```

## 🎨 기준 거래소 변경

UI 우측 상단에서 선택:
- **업비트**: 업비트를 기준(0%)으로 빗썸 프리미엄 계산
- **빗썸**: 빗썸을 기준(0%)으로 업비트 프리미엄 계산

## 🔍 검색 기능

검색창에 코인명 또는 심볼 입력:
- "비트코인" 또는 "BTC"
- "이더리움" 또는 "ETH"
- 실시간 필터링

## 📱 지원 코인

- BTC (비트코인)
- ETH (이더리움)
- XRP (리플)
- ADA (에이다)
- SOL (솔라나)
- DOGE (도지코인)
- AVAX (아발란체)
- MATIC (폴리곤)
- DOT (폴카닷)
- SHIB (시바이누)

## 🚨 주의사항

1. **수수료**: 실제 차익거래 시 각 거래소 수수료(0.05~0.25%) 고려 필요
2. **슬리피지**: 대량 거래 시 호가 스프레드로 인한 가격 변동 발생
3. **출금 시간**: 거래소 간 코인 이동 시간(10~30분) 고려
4. **시장 변동성**: 프리미엄은 실시간으로 변하므로 빠른 판단 필요

## 📚 상세 문서

- [전체 가이드](CRYPTO_COMPARISON_GUIDE.md)
- [API 문서 UI](http://localhost:8080/crypto/api-docs)

## 🤝 문의

버그 리포트나 기능 제안은 이슈로 등록해주세요!
