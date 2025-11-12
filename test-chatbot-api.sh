#!/bin/bash

echo "🤖 채용 검색 챗봇 API 테스트"
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
sleep 1

# 2. 검색 시작
echo "2️⃣ 검색 시작"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"채용 찾고 싶어요\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""
sleep 1

# 3. 지역 선택 (서울)
echo "3️⃣ 지역 선택 - 서울"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"서울에서 일하고 싶어요\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""
sleep 1

# 4. 업종 선택 (IT)
echo "4️⃣ 업종 선택 - IT"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"IT 업종이요\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""
sleep 1

# 5. 연봉 입력
echo "5️⃣ 연봉 입력 - 4000만원~6000만원"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"4000만원에서 6000만원 사이요\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""
sleep 1

# 6. 검색 재시작
echo "6️⃣ 검색 재시작"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"다시 검색할래요\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""
sleep 1

# 7. 새로운 검색 - 경기 금융
echo "7️⃣ 새 검색 시작"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"채용\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""
sleep 1

echo "8️⃣ 지역 - 부산"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"부산\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""
sleep 1

echo "9️⃣ 업종 - 금융"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"금융\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""
sleep 1

echo "🔟 연봉 - 3000만원 이상"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"3000만원\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""
sleep 1

# 11. 도움말 테스트
echo "1️⃣1️⃣ 도움말 테스트"
curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"도움말\",\"conversationId\":$CONVERSATION_ID}" | jq '.'
echo ""

# 12. 대화 히스토리 조회
echo "1️⃣2️⃣ 대화 히스토리 조회"
curl -s -X GET "$BASE_URL/conversations/$CONVERSATION_ID/history?userId=$USER_ID" | jq '.'
echo ""

# 13. Health Check
echo "1️⃣3️⃣ Health Check"
curl -s -X GET "$BASE_URL/health"
echo ""
echo ""

echo "✅ 테스트 완료!"
