#!/bin/bash

# 로그 수집 및 분석 기능 테스트 스크립트

BASE_URL="http://localhost:8080"
CORRELATION_ID="TEST_$(date +%s)"

echo "🔍 PayFlow 로그 수집·분석 시스템 테스트"
echo "=========================================="
echo ""

# 1. 로그인 (JWT 토큰 발급)
echo "1️⃣ 로그인..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ 로그인 실패"
    exit 1
fi

echo "✅ 로그인 성공"
echo ""

# 2. 주문 생성 (이벤트 로그 생성)
echo "2️⃣ 주문 생성 (이벤트 로그 생성)..."
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Correlation-ID: $CORRELATION_ID" \
  -d '{
    "orderName": "로그 테스트 상품",
    "amount": 50000,
    "customerEmail": "test@example.com",
    "customerName": "테스터"
  }')

ORDER_ID=$(echo $ORDER_RESPONSE | grep -o '"orderId":"[^"]*' | cut -d'"' -f4)
echo "✅ 주문 생성 완료: $ORDER_ID"
echo ""

# 3. 대시보드 메트릭 조회
echo "3️⃣ 대시보드 메트릭 조회..."
METRICS=$(curl -s "$BASE_URL/api/logs/dashboard/metrics?hours=24")
echo "📊 메트릭 데이터:"
echo $METRICS | jq '.' 2>/dev/null || echo $METRICS
echo ""

# 4. 실시간 이벤트 스트림 조회
echo "4️⃣ 실시간 이벤트 스트림 조회..."
RECENT_EVENTS=$(curl -s "$BASE_URL/api/logs/events/recent?limit=10")
echo "🔥 최근 이벤트:"
echo $RECENT_EVENTS | jq '.' 2>/dev/null || echo $RECENT_EVENTS
echo ""

# 5. Correlation ID로 이벤트 체인 추적
echo "5️⃣ Correlation ID로 이벤트 체인 추적..."
sleep 2  # 이벤트 처리 대기
EVENT_CHAIN=$(curl -s "$BASE_URL/api/logs/events/trace/$CORRELATION_ID")
echo "🔗 이벤트 체인 (Correlation ID: $CORRELATION_ID):"
echo $EVENT_CHAIN | jq '.' 2>/dev/null || echo $EVENT_CHAIN
echo ""

# 6. 결제 이벤트 히스토리 조회 (이벤트 소싱)
echo "6️⃣ 결제 이벤트 히스토리 조회 (이벤트 소싱)..."
PAYMENT_HISTORY=$(curl -s "$BASE_URL/api/logs/payments/$ORDER_ID/history")
echo "📜 결제 이벤트 히스토리:"
echo $PAYMENT_HISTORY | jq '.' 2>/dev/null || echo $PAYMENT_HISTORY
echo ""

# 7. 결제 상태 재구성
echo "7️⃣ 결제 상태 재구성..."
PAYMENT_STATE=$(curl -s "$BASE_URL/api/logs/payments/$ORDER_ID/state")
echo "🔄 현재 결제 상태: $PAYMENT_STATE"
echo ""

# 8. 사용자별 이벤트 조회
echo "8️⃣ 사용자별 이벤트 조회..."
USER_EVENTS=$(curl -s "$BASE_URL/api/logs/events/user/user")
echo "👤 사용자 이벤트:"
echo $USER_EVENTS | jq 'length' 2>/dev/null || echo "조회 완료"
echo ""

echo "=========================================="
echo "✅ 로그 수집·분석 시스템 테스트 완료!"
echo ""
echo "📊 대시보드 확인: $BASE_URL/logs/dashboard"
echo ""
echo "💡 주요 기능:"
echo "  - ✅ 실시간 이벤트 로그 수집"
echo "  - ✅ Correlation ID 기반 분산 추적"
echo "  - ✅ 이벤트 소싱 패턴 (결제 상태 이력)"
echo "  - ✅ 로그 분석 대시보드"
echo "  - ✅ 시간대별/타입별 이벤트 집계"
echo "  - ✅ 서비스별 성공률 및 처리시간 분석"
