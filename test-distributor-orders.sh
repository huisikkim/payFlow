#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}유통업체 발주 목록 조회 테스트${NC}"
echo -e "${BLUE}========================================${NC}\n"

# 1. 유통업체 로그인
echo -e "${YELLOW}[1] 유통업체 로그인${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "distributor1",
    "password": "password123"
  }')

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}❌ 로그인 실패${NC}"
    echo "응답: $LOGIN_RESPONSE"
    echo ""
    echo -e "${YELLOW}유통업체 계정이 없습니다. 먼저 회원가입을 진행합니다...${NC}"
    
    # 유통업체 회원가입
    SIGNUP_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/signup" \
      -H "Content-Type: application/json" \
      -d '{
        "username": "distributor1",
        "password": "password123",
        "email": "dist1@example.com",
        "userType": "DISTRIBUTOR",
        "businessNumber": "111-11-11111",
        "businessName": "신선식자재 유통",
        "ownerName": "김유통",
        "phoneNumber": "010-1111-1111",
        "address": "서울시 송파구"
      }')
    
    echo "회원가입 응답: $SIGNUP_RESPONSE"
    
    # 다시 로그인
    LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
      -H "Content-Type: application/json" \
      -d '{
        "username": "distributor1",
        "password": "password123"
      }')
    
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
fi

echo -e "${GREEN}✅ 유통업체 로그인 성공${NC}"
echo "토큰: ${TOKEN:0:50}..."
echo ""

# 2. 대기 중인 발주 조회
echo -e "${YELLOW}[2] 대기 중인 발주 조회${NC}"
echo "GET $BASE_URL/api/distributor/orders/pending?distributorId=distributor1"
PENDING_ORDERS=$(curl -s -X GET "$BASE_URL/api/distributor/orders/pending?distributorId=distributor1" \
  -H "Authorization: Bearer $TOKEN")

echo "$PENDING_ORDERS" | python3 -m json.tool 2>/dev/null || echo "$PENDING_ORDERS"
echo -e "${GREEN}✅ 대기 중인 발주 조회 완료${NC}\n"

# 3. 전체 발주 조회
echo -e "${YELLOW}[3] 전체 발주 조회${NC}"
echo "GET $BASE_URL/api/distributor/orders?distributorId=distributor1"
ALL_ORDERS=$(curl -s -X GET "$BASE_URL/api/distributor/orders?distributorId=distributor1" \
  -H "Authorization: Bearer $TOKEN")

echo "$ALL_ORDERS" | python3 -m json.tool 2>/dev/null || echo "$ALL_ORDERS"
echo -e "${GREEN}✅ 전체 발주 조회 완료${NC}\n"

# 4. 발주 개수 확인
PENDING_COUNT=$(echo "$PENDING_ORDERS" | python3 -c "import sys, json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
ALL_COUNT=$(echo "$ALL_ORDERS" | python3 -c "import sys, json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}조회 결과 요약${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "대기 중인 발주: ${YELLOW}${PENDING_COUNT}건${NC}"
echo -e "전체 발주: ${YELLOW}${ALL_COUNT}건${NC}"
echo ""

if [ "$ALL_COUNT" = "0" ]; then
    echo -e "${YELLOW}⚠️  발주가 없습니다.${NC}"
    echo -e "${YELLOW}매장에서 발주를 생성해주세요.${NC}"
    echo ""
    echo -e "${BLUE}발주 생성 방법:${NC}"
    echo "1. 매장 계정으로 로그인"
    echo "2. POST /api/ingredient-orders 호출"
    echo "3. distributorId를 'distributor1'로 설정"
else
    echo -e "${GREEN}✅ 발주 목록 조회 성공!${NC}"
fi
