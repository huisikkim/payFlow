#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=== 채팅 API 테스트 시작 ==="
echo ""

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 1. 매장 로그인
echo -e "${BLUE}1. 매장 로그인${NC}"
STORE_LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "store1",
    "password": "password123"
  }')

STORE_TOKEN=$(echo $STORE_LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "매장 토큰: ${STORE_TOKEN:0:50}..."
echo ""

# 2. 유통업체 로그인
echo -e "${BLUE}2. 유통업체 로그인${NC}"
DIST_LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "dist1",
    "password": "password123"
  }')

DIST_TOKEN=$(echo $DIST_LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "유통업체 토큰: ${DIST_TOKEN:0:50}..."
echo ""

# 3. 채팅방 생성 (매장에서)
echo -e "${BLUE}3. 채팅방 생성 (매장 → 유통업체)${NC}"
CHATROOM_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/chat/rooms" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${STORE_TOKEN}" \
  -d '{
    "storeId": "store1",
    "distributorId": "dist1"
  }')

echo $CHATROOM_RESPONSE | jq '.'
ROOM_ID=$(echo $CHATROOM_RESPONSE | grep -o '"roomId":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}채팅방 ID: ${ROOM_ID}${NC}"
echo ""

# 4. 내 채팅방 목록 조회 (매장)
echo -e "${BLUE}4. 내 채팅방 목록 조회 (매장)${NC}"
curl -s -X GET "${BASE_URL}/api/chat/rooms" \
  -H "Authorization: Bearer ${STORE_TOKEN}" | jq '.'
echo ""

# 5. 내 채팅방 목록 조회 (유통업체)
echo -e "${BLUE}5. 내 채팅방 목록 조회 (유통업체)${NC}"
curl -s -X GET "${BASE_URL}/api/chat/rooms" \
  -H "Authorization: Bearer ${DIST_TOKEN}" | jq '.'
echo ""

# 6. 메시지 조회 (초기 - 비어있음)
echo -e "${BLUE}6. 메시지 조회 (초기)${NC}"
curl -s -X GET "${BASE_URL}/api/chat/rooms/${ROOM_ID}/messages?page=0&size=20" \
  -H "Authorization: Bearer ${STORE_TOKEN}" | jq '.'
echo ""

# 7. 읽지 않은 메시지 수 조회
echo -e "${BLUE}7. 읽지 않은 메시지 수 조회${NC}"
curl -s -X GET "${BASE_URL}/api/chat/rooms/${ROOM_ID}/unread-count" \
  -H "Authorization: Bearer ${STORE_TOKEN}"
echo ""
echo ""

echo -e "${GREEN}=== REST API 테스트 완료 ===${NC}"
echo ""
echo -e "${BLUE}WebSocket 테스트는 브라우저나 WebSocket 클라이언트를 사용하세요:${NC}"
echo "- 연결: ws://localhost:8080/ws/chat"
echo "- 구독: /topic/chat/${ROOM_ID}"
echo "- 발행: /app/chat/${ROOM_ID}"
echo ""
