#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

echo -e "${YELLOW}=== 스테이지 완료 및 정산 대시보드 테스트 ===${NC}\n"

# 스테이지 ID 입력 받기
if [ -z "$1" ]; then
    echo -e "${YELLOW}사용법: ./complete-stage-and-view-settlement.sh <STAGE_ID>${NC}"
    echo -e "${YELLOW}예시: ./complete-stage-and-view-settlement.sh 1${NC}"
    exit 1
fi

STAGE_ID=$1

# 1. 로그인
echo -e "${YELLOW}1. 로그인...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}❌ 로그인 실패${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 로그인 성공${NC}\n"

# 2. 스테이지 정보 조회
echo -e "${YELLOW}2. 스테이지 정보 조회...${NC}"
STAGE_INFO=$(curl -s -X GET "${BASE_URL}/api/stages/${STAGE_ID}" \
  -H "Authorization: Bearer ${TOKEN}")

echo $STAGE_INFO | jq '.' 2>/dev/null || echo $STAGE_INFO
echo ""

TOTAL_PARTICIPANTS=$(echo $STAGE_INFO | grep -o '"totalParticipants":[0-9]*' | cut -d':' -f2)
echo -e "${BLUE}총 참여자: ${TOTAL_PARTICIPANTS}명${NC}\n"

# 3. 모든 참여자의 모든 월 결제를 완료 처리 (시뮬레이션)
echo -e "${YELLOW}3. 모든 결제 완료 처리 (시뮬레이션)...${NC}"
echo -e "${BLUE}실제로는 스케줄러가 자동으로 처리하지만, 테스트를 위해 수동으로 진행합니다.${NC}\n"

# 참여자 목록 조회
PARTICIPANTS=$(curl -s -X GET "${BASE_URL}/api/stages/${STAGE_ID}/participants" \
  -H "Authorization: Bearer ${TOKEN}")

echo -e "${GREEN}✅ 참여자 목록 조회 완료${NC}"
echo $PARTICIPANTS | jq '.' 2>/dev/null || echo $PARTICIPANTS
echo ""

# 4. 모든 약정금 지급 완료 처리 (시뮬레이션)
echo -e "${YELLOW}4. 모든 약정금 지급 완료 처리 (시뮬레이션)...${NC}"
echo -e "${BLUE}실제로는 각 순번마다 약정금을 지급해야 하지만, 테스트를 위해 스킵합니다.${NC}\n"

# 5. 스테이지 강제 완료 (개발/테스트용)
echo -e "${YELLOW}5. 스테이지 완료 처리...${NC}"
echo -e "${BLUE}참고: 실제로는 모든 약정금이 지급되어야 자동 완료됩니다.${NC}"
echo -e "${BLUE}테스트를 위해 데이터베이스를 직접 수정하거나 관리자 API가 필요합니다.${NC}\n"

# 6. 정산 생성
echo -e "${YELLOW}6. 정산 생성 시도...${NC}"
SETTLEMENT_CREATE=$(curl -s -X POST "${BASE_URL}/api/stages/${STAGE_ID}/settlement" \
  -H "Authorization: Bearer ${TOKEN}")

if echo $SETTLEMENT_CREATE | grep -q "error\|완료된 스테이지만"; then
    echo -e "${RED}❌ 정산 생성 실패: 스테이지가 아직 완료되지 않았습니다.${NC}"
    echo $SETTLEMENT_CREATE | jq '.' 2>/dev/null || echo $SETTLEMENT_CREATE
    echo ""
    echo -e "${YELLOW}=== 스테이지를 완료하는 방법 ===${NC}"
    echo -e "${BLUE}1. H2 Console 접속: http://localhost:8080/h2-console${NC}"
    echo -e "${BLUE}2. 다음 SQL 실행:${NC}"
    echo -e "${GREEN}   UPDATE stages SET status = 'COMPLETED' WHERE id = ${STAGE_ID};${NC}"
    echo ""
    echo -e "${BLUE}3. 또는 모든 약정금을 지급 완료 처리:${NC}"
    echo -e "${GREEN}   -- 모든 결제 완료 처리${NC}"
    echo -e "${GREEN}   UPDATE stage_payments SET is_paid = true WHERE stage_id = ${STAGE_ID};${NC}"
    echo ""
    echo -e "${GREEN}   -- 모든 약정금 생성 (수동)${NC}"
    echo -e "${GREEN}   INSERT INTO stage_payouts (stage_id, username, turn_number, amount, is_completed, scheduled_at)${NC}"
    echo -e "${GREEN}   SELECT ${STAGE_ID}, username, turn_number, ${NC}"
    echo -e "${GREEN}          (SELECT monthly_payment * total_participants * (1 + interest_rate * (turn_number - 1)) FROM stages WHERE id = ${STAGE_ID}),${NC}"
    echo -e "${GREEN}          true, NOW()${NC}"
    echo -e "${GREEN}   FROM stage_participants WHERE stage_id = ${STAGE_ID};${NC}"
    echo ""
    echo -e "${BLUE}4. 그 다음 이 스크립트를 다시 실행하세요.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 정산 생성 성공!${NC}"
echo $SETTLEMENT_CREATE | jq '.' 2>/dev/null || echo $SETTLEMENT_CREATE
echo ""

# 7. 정산 조회
echo -e "${YELLOW}7. 정산 조회...${NC}"
SETTLEMENT=$(curl -s -X GET "${BASE_URL}/api/stages/${STAGE_ID}/settlement" \
  -H "Authorization: Bearer ${TOKEN}")

echo -e "${GREEN}✅ 정산 조회 성공!${NC}"
echo $SETTLEMENT | jq '.' 2>/dev/null || echo $SETTLEMENT
echo ""

# 8. 브라우저에서 대시보드 열기
echo -e "${GREEN}=== 완료! ===${NC}"
echo -e "${YELLOW}정산 대시보드 URL:${NC}"
echo -e "${BLUE}http://localhost:8080/stages/${STAGE_ID}/settlement${NC}"
echo ""
echo -e "${YELLOW}스테이지 목록 URL:${NC}"
echo -e "${BLUE}http://localhost:8080/stages${NC}"
echo ""

# macOS에서 자동으로 브라우저 열기
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo -e "${YELLOW}브라우저를 자동으로 엽니다...${NC}"
    open "http://localhost:8080/stages/${STAGE_ID}/settlement"
fi
