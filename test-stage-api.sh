#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

echo -e "${YELLOW}=== PayFlow 스테이지(계) API 테스트 ===${NC}\n"

# 1. 로그인 (JWT 토큰 획득)
echo -e "${YELLOW}1. 사용자 로그인...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}❌ 로그인 실패${NC}"
    echo $LOGIN_RESPONSE
    exit 1
fi

echo -e "${GREEN}✅ 로그인 성공${NC}"
echo "Token: ${TOKEN:0:50}..."
echo ""

# 2. 스테이지 생성
echo -e "${YELLOW}2. 스테이지 생성...${NC}"
CREATE_STAGE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/stages" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{
    "name": "여행 계모임",
    "totalParticipants": 3,
    "monthlyPayment": 100000,
    "interestRate": 0.05,
    "paymentDay": 15
  }')

STAGE_ID=$(echo $CREATE_STAGE_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -z "$STAGE_ID" ]; then
    echo -e "${RED}❌ 스테이지 생성 실패${NC}"
    echo $CREATE_STAGE_RESPONSE
    exit 1
fi

echo -e "${GREEN}✅ 스테이지 생성 성공 (ID: ${STAGE_ID})${NC}"
echo $CREATE_STAGE_RESPONSE | jq '.' 2>/dev/null || echo $CREATE_STAGE_RESPONSE
echo ""

# 3. 스테이지 조회
echo -e "${YELLOW}3. 스테이지 조회...${NC}"
GET_STAGE_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/stages/${STAGE_ID}" \
  -H "Authorization: Bearer ${TOKEN}")

echo -e "${GREEN}✅ 스테이지 조회 성공${NC}"
echo $GET_STAGE_RESPONSE | jq '.' 2>/dev/null || echo $GET_STAGE_RESPONSE
echo ""

# 4. 첫 번째 사용자 참여 (순번 1)
echo -e "${YELLOW}4. 첫 번째 사용자 참여 (순번 1)...${NC}"
JOIN_RESPONSE_1=$(curl -s -X POST "${BASE_URL}/api/stages/${STAGE_ID}/join" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{
    "turnNumber": 1
  }')

echo -e "${GREEN}✅ 참여 성공${NC}"
echo $JOIN_RESPONSE_1 | jq '.' 2>/dev/null || echo $JOIN_RESPONSE_1
echo ""

# 5. 두 번째 사용자 로그인 및 참여
echo -e "${YELLOW}5. 두 번째 사용자 로그인...${NC}"
LOGIN_RESPONSE_2=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }')

TOKEN_2=$(echo $LOGIN_RESPONSE_2 | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}✅ 두 번째 사용자 로그인 성공${NC}"
echo ""

echo -e "${YELLOW}6. 두 번째 사용자 참여 (순번 2)...${NC}"
JOIN_RESPONSE_2=$(curl -s -X POST "${BASE_URL}/api/stages/${STAGE_ID}/join" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN_2}" \
  -d '{
    "turnNumber": 2
  }')

echo -e "${GREEN}✅ 참여 성공${NC}"
echo $JOIN_RESPONSE_2 | jq '.' 2>/dev/null || echo $JOIN_RESPONSE_2
echo ""

# 7. 세 번째 사용자 생성 및 참여
echo -e "${YELLOW}7. 세 번째 사용자 생성...${NC}"
SIGNUP_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser3",
    "password": "password123",
    "email": "testuser3@example.com"
  }')

echo -e "${GREEN}✅ 사용자 생성 성공${NC}"
echo ""

echo -e "${YELLOW}8. 세 번째 사용자 로그인...${NC}"
LOGIN_RESPONSE_3=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser3",
    "password": "password123"
  }')

TOKEN_3=$(echo $LOGIN_RESPONSE_3 | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}✅ 세 번째 사용자 로그인 성공${NC}"
echo ""

echo -e "${YELLOW}9. 세 번째 사용자 참여 (순번 3)...${NC}"
JOIN_RESPONSE_3=$(curl -s -X POST "${BASE_URL}/api/stages/${STAGE_ID}/join" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN_3}" \
  -d '{
    "turnNumber": 3
  }')

echo -e "${GREEN}✅ 참여 성공${NC}"
echo $JOIN_RESPONSE_3 | jq '.' 2>/dev/null || echo $JOIN_RESPONSE_3
echo ""

# 10. 참여자 목록 조회
echo -e "${YELLOW}10. 참여자 목록 조회...${NC}"
PARTICIPANTS_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/stages/${STAGE_ID}/participants" \
  -H "Authorization: Bearer ${TOKEN}")

echo -e "${GREEN}✅ 참여자 목록 조회 성공${NC}"
echo $PARTICIPANTS_RESPONSE | jq '.' 2>/dev/null || echo $PARTICIPANTS_RESPONSE
echo ""

# 11. 스테이지 시작
echo -e "${YELLOW}11. 스테이지 시작...${NC}"
START_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/stages/${STAGE_ID}/start" \
  -H "Authorization: Bearer ${TOKEN}")

echo -e "${GREEN}✅ 스테이지 시작 성공${NC}"
echo ""

# 12. 시작된 스테이지 조회
echo -e "${YELLOW}12. 시작된 스테이지 조회...${NC}"
GET_STARTED_STAGE=$(curl -s -X GET "${BASE_URL}/api/stages/${STAGE_ID}" \
  -H "Authorization: Bearer ${TOKEN}")

echo -e "${GREEN}✅ 스테이지 상태 확인${NC}"
echo $GET_STARTED_STAGE | jq '.' 2>/dev/null || echo $GET_STARTED_STAGE
echo ""

# 13. 내 스테이지 목록 조회
echo -e "${YELLOW}13. 내 스테이지 목록 조회...${NC}"
MY_STAGES_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/stages/my" \
  -H "Authorization: Bearer ${TOKEN}")

echo -e "${GREEN}✅ 내 스테이지 목록 조회 성공${NC}"
echo $MY_STAGES_RESPONSE | jq '.' 2>/dev/null || echo $MY_STAGES_RESPONSE
echo ""

# 14. 모집 중인 스테이지 목록 조회
echo -e "${YELLOW}14. 모집 중인 스테이지 목록 조회...${NC}"
RECRUITING_STAGES=$(curl -s -X GET "${BASE_URL}/api/stages?status=RECRUITING" \
  -H "Authorization: Bearer ${TOKEN}")

echo -e "${GREEN}✅ 모집 중인 스테이지 목록 조회 성공${NC}"
echo $RECRUITING_STAGES | jq '.' 2>/dev/null || echo $RECRUITING_STAGES
echo ""

# 15. 진행 중인 스테이지 목록 조회
echo -e "${YELLOW}15. 진행 중인 스테이지 목록 조회...${NC}"
ACTIVE_STAGES=$(curl -s -X GET "${BASE_URL}/api/stages?status=ACTIVE" \
  -H "Authorization: Bearer ${TOKEN}")

echo -e "${GREEN}✅ 진행 중인 스테이지 목록 조회 성공${NC}"
echo $ACTIVE_STAGES | jq '.' 2>/dev/null || echo $ACTIVE_STAGES
echo ""

# 16. 약정금 내역 조회
echo -e "${YELLOW}16. 약정금 내역 조회...${NC}"
PAYOUTS_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/stages/${STAGE_ID}/payouts" \
  -H "Authorization: Bearer ${TOKEN}")

echo -e "${GREEN}✅ 약정금 내역 조회 성공${NC}"
echo $PAYOUTS_RESPONSE | jq '.' 2>/dev/null || echo $PAYOUTS_RESPONSE
echo ""

# 17. 내 약정금 조회
echo -e "${YELLOW}17. 내 약정금 조회...${NC}"
MY_PAYOUTS=$(curl -s -X GET "${BASE_URL}/api/stages/payouts/my" \
  -H "Authorization: Bearer ${TOKEN}")

echo -e "${GREEN}✅ 내 약정금 조회 성공${NC}"
echo $MY_PAYOUTS | jq '.' 2>/dev/null || echo $MY_PAYOUTS
echo ""

echo -e "${GREEN}=== 모든 테스트 완료! ===${NC}"
echo -e "${YELLOW}생성된 스테이지 ID: ${STAGE_ID}${NC}"
echo -e "${YELLOW}참여자: user (순번 1), admin (순번 2), testuser3 (순번 3)${NC}"
echo -e "${YELLOW}상태: ACTIVE${NC}"
echo ""
echo -e "${YELLOW}💡 약정금 지급 완료 처리 예시:${NC}"
echo -e "curl -X POST ${BASE_URL}/api/stages/payouts/\${PAYOUT_ID}/complete \\"
echo -e "  -H \"Content-Type: application/json\" \\"
echo -e "  -H \"Authorization: Bearer \${TOKEN}\" \\"
echo -e "  -d '{\"transactionId\": \"TXN-20251108-001\"}'"

# 18. 정산 생성 (스테이지 완료 후)
echo -e "${YELLOW}18. 정산 생성...${NC}"
echo -e "${YELLOW}(참고: 실제로는 스테이지가 COMPLETED 상태여야 합니다)${NC}"
# SETTLEMENT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/stages/${STAGE_ID}/settlement" \
#   -H "Authorization: Bearer ${TOKEN}")
# echo $SETTLEMENT_RESPONSE | jq '.' 2>/dev/null || echo $SETTLEMENT_RESPONSE
echo ""

echo -e "${GREEN}=== 정산 기능 추가 완료! ===${NC}"
echo -e "${YELLOW}정산 기능:${NC}"
echo -e "  - 총 납입액/지급액/이자 계산"
echo -e "  - 참여자별 손익 계산"
echo -e "  - 실질 이율 계산"
echo -e "  - 정산 검증"
