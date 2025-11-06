#!/bin/bash

echo "🧪 PayFlow API 테스트"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# 1. 주문 생성 테스트
echo "1️⃣  주문 생성 테스트..."
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderName": "테스트 상품",
    "amount": 1000,
    "customerEmail": "test@example.com",
    "customerName": "테스트"
  }')

echo "응답: $ORDER_RESPONSE"
ORDER_ID=$(echo $ORDER_RESPONSE | grep -o '"orderId":"[^"]*"' | cut -d'"' -f4)
echo "주문 ID: $ORDER_ID"
echo ""

# 2. 주문 조회 테스트
echo "2️⃣  주문 조회 테스트..."
sleep 2
curl -s http://localhost:8080/api/orders/$ORDER_ID | jq '.'
echo ""

# 3. 결제 조회 테스트
echo "3️⃣  결제 조회 테스트..."
sleep 2
curl -s http://localhost:8080/api/payments/$ORDER_ID | jq '.'
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "API 테스트 완료!"
echo ""
echo "💡 다음 단계:"
echo "   1. 브라우저에서 http://localhost:8080 접속"
echo "   2. 결제 진행"
echo "   3. 토스 테스트 카드로 결제 완료"
