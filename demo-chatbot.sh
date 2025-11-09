#!/bin/bash

echo "╔════════════════════════════════════════╗"
echo "║   🤖 PayFlow 챗봇 데모 시연               ║"
echo "║      MVP 프로젝트                        ║"
echo "╚════════════════════════════════════════╝"
echo ""

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080/api/chatbot"
USER_ID="demo_user"

echo -e "${BLUE}📝 데모 시나리오${NC}"
echo "================================"
echo ""

# 시나리오 1: 인사
echo -e "${GREEN}👤 사용자:${NC} 안녕하세요"
RESPONSE=$(curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"안녕하세요\"}")
CONVERSATION_ID=$(echo $RESPONSE | grep -o '"conversationId":[0-9]*' | grep -o '[0-9]*')
MESSAGE=$(echo $RESPONSE | grep -o '"message":"[^"]*"' | sed 's/"message":"//;s/"$//')
echo -e "${YELLOW}🤖 챗봇:${NC} $MESSAGE"
echo ""
sleep 2

# 시나리오 2: 주문 조회
echo -e "${GREEN}👤 사용자:${NC} 주문 조회하고 싶어요"
RESPONSE=$(curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"주문 조회하고 싶어요\",\"conversationId\":$CONVERSATION_ID}")
MESSAGE=$(echo $RESPONSE | grep -o '"message":"[^"]*"' | sed 's/"message":"//;s/"$//')
echo -e "${YELLOW}🤖 챗봇:${NC} $MESSAGE"
echo ""
sleep 2

# 시나리오 3: 배송 조회
echo -e "${GREEN}👤 사용자:${NC} 배송 언제 도착하나요?"
RESPONSE=$(curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"배송 언제 도착하나요?\",\"conversationId\":$CONVERSATION_ID}")
MESSAGE=$(echo $RESPONSE | grep -o '"message":"[^"]*"' | sed 's/"message":"//;s/"$//')
echo -e "${YELLOW}🤖 챗봇:${NC} $MESSAGE"
echo ""
sleep 2

# 시나리오 4: 스테이지 안내
echo -e "${GREEN}👤 사용자:${NC} 스테이지 어떻게 참여해?"
RESPONSE=$(curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"스테이지 어떻게 참여해?\",\"conversationId\":$CONVERSATION_ID}")
MESSAGE=$(echo $RESPONSE | grep -o '"message":"[^"]*"' | sed 's/"message":"//;s/"$//')
echo -e "${YELLOW}🤖 챗봇:${NC} $MESSAGE"
echo ""
sleep 2

# 시나리오 5: 환불 요청
echo -e "${GREEN}👤 사용자:${NC} 환불하고 싶어요"
RESPONSE=$(curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"환불하고 싶어요\",\"conversationId\":$CONVERSATION_ID}")
MESSAGE=$(echo $RESPONSE | grep -o '"message":"[^"]*"' | sed 's/"message":"//;s/"$//')
echo -e "${YELLOW}🤖 챗봇:${NC} $MESSAGE"
echo ""
sleep 2

# 시나리오 6: 도움말
echo -e "${GREEN}👤 사용자:${NC} 도움말"
RESPONSE=$(curl -s -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$USER_ID\",\"message\":\"도움말\",\"conversationId\":$CONVERSATION_ID}")
MESSAGE=$(echo $RESPONSE | grep -o '"message":"[^"]*"' | sed 's/"message":"//;s/"$//')
echo -e "${YELLOW}🤖 챗봇:${NC} $MESSAGE"
echo ""

echo "================================"
echo -e "${BLUE}✅ 데모 완료!${NC}"
echo ""
echo "📊 주요 기능:"
echo "  ✓ 규칙 기반 Intent 매칭 (9가지 의도)"
echo "  ✓ 대화 이력 관리"
echo "  ✓ Kafka 이벤트 발행 (EDA)"
echo "  ✓ DDD 패턴 적용"
echo "  ✓ 스테이지 이용 안내"
echo ""
echo "🌐 웹 UI: http://localhost:8080/chatbot"
echo "📖 API 문서: CHATBOT_README.md"
