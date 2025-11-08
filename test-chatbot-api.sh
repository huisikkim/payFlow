#!/bin/bash

echo "🤖 PayFlow 챗봇 API 테스트"
echo "================================"
echo ""

BASE_URL="http://localhost:8080/api/chatbot"
USER_ID="test_user_$(date +%s)"

echo "📝 사용자 ID: $USER_ID"
echo ""

# 1. 인사 테스트
echo "1️⃣ 인사 테스트"
RESPONSE=$(curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"안녕하세요\"}")
echo "응답: $RESPONSE"
CONVERSATION_ID=$(echo $RESPONSE | grep -o '"conversationId":[0-9]*' | grep -o '[0-9]*')
echo ""

# 2. 주문 조회 테스트
echo "2️⃣ 주문 조회 테스트"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"주문 조회하고 싶어요\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""

# 3. 결제 조회 테스트
echo "3️⃣ 결제 조회 테스트"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"결제 내역 확인\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""

# 4. 배송 조회 테스트
echo "4️⃣ 배송 조회 테스트"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"배송 언제 도착하나요?\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""

# 5. 환불 요청 테스트
echo "5️⃣ 환불 요청 테스트"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"환불하고 싶어요\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""

# 6. 정산 조회 테스트
echo "6️⃣ 정산 조회 테스트"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"정산 내역 확인\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""

# 7. 스테이지 안내 테스트
echo "7️⃣ 스테이지 안내 테스트"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"스테이지 어떻게 참여해?\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""

# 8. 도움말 테스트
echo "8️⃣ 도움말 테스트"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"도움말\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""

# 9. 알 수 없는 의도 테스트
echo "9️⃣ 알 수 없는 의도 테스트"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"날씨 어때?\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""

# 10. 대화 히스토리 조회
echo "🔟 대화 히스토리 조회"
curl -s -X GET "$BASE_URL/conversations/$CONVERSATION_ID/history?userId=$USER_ID" | jq '.'
echo ""

# 11. Health Check
echo "🏥 Health Check"
curl -s -X GET "$BASE_URL/health"
echo ""
echo ""

echo "✅ 테스트 완료!"
