#!/bin/bash

echo "🔥 거래소 시세 비교 API 테스트"
echo "================================"
echo ""

BASE_URL="http://localhost:8080"

echo "1️⃣ 모든 코인 프리미엄 조회 (업비트 기준)"
echo "GET /api/crypto/comparison/premiums?referenceExchange=UPBIT"
curl -s "${BASE_URL}/api/crypto/comparison/premiums?referenceExchange=UPBIT" | jq '.[0:3]'
echo ""
echo ""

echo "2️⃣ 특정 코인 프리미엄 조회 (BTC)"
echo "GET /api/crypto/comparison/premiums/KRW-BTC?referenceExchange=UPBIT"
curl -s "${BASE_URL}/api/crypto/comparison/premiums/KRW-BTC?referenceExchange=UPBIT" | jq '.'
echo ""
echo ""

echo "3️⃣ 차익거래 기회 찾기 (1% 이상)"
echo "GET /api/crypto/comparison/arbitrage?referenceExchange=UPBIT&minPremium=1.0"
curl -s "${BASE_URL}/api/crypto/comparison/arbitrage?referenceExchange=UPBIT&minPremium=1.0" | jq '.'
echo ""
echo ""

echo "4️⃣ 체결강도 조회 (업비트 BTC)"
echo "GET /api/crypto/comparison/trade-strength/UPBIT/KRW-BTC"
curl -s "${BASE_URL}/api/crypto/comparison/trade-strength/UPBIT/KRW-BTC" | jq '.'
echo ""
echo ""

echo "5️⃣ 실시간 스트리밍 테스트 (5초간)"
echo "GET /api/crypto/comparison/premiums/stream?referenceExchange=UPBIT"
echo "스트리밍 시작..."
timeout 5 curl -s "${BASE_URL}/api/crypto/comparison/premiums/stream?referenceExchange=UPBIT" | head -n 3
echo ""
echo "스트리밍 종료"
echo ""

echo "6️⃣ 호가 데이터 조회 (업비트 BTC)"
echo "GET /api/crypto/comparison/orderbook/UPBIT/KRW-BTC"
curl -s "${BASE_URL}/api/crypto/comparison/orderbook/UPBIT/KRW-BTC" | jq '.bestBidPrice, .bestAskPrice, .spread'
echo ""
echo ""

echo "7️⃣ 호가 스프레드 비교 (BTC)"
echo "GET /api/crypto/comparison/orderbook/spread/KRW-BTC"
curl -s "${BASE_URL}/api/crypto/comparison/orderbook/spread/KRW-BTC" | jq '.'
echo ""
echo ""

echo "✅ 테스트 완료!"
echo ""
echo "📱 웹 UI 접속:"
echo "  - 실시간 비교: ${BASE_URL}/crypto/comparison"
echo "  - API 문서: ${BASE_URL}/crypto/api-docs"
