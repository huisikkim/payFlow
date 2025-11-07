#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BASE_URL="http://localhost:8080"

echo -e "${YELLOW}=== 정산 데모 데이터 생성 ===${NC}\n"

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

# 2. 스테이지 ID 찾기
echo -e "${YELLOW}2. 스테이지 조회...${NC}"
STAGES=$(curl -s -X GET "${BASE_URL}/api/stages?status=ACTIVE" \
  -H "Authorization: Bearer ${TOKEN}")

STAGE_ID=$(echo $STAGES | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -z "$STAGE_ID" ]; then
    echo -e "${RED}❌ 진행 중인 스테이지가 없습니다.${NC}"
    echo -e "${YELLOW}먼저 ./test-stage-api.sh를 실행하세요.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 스테이지 ID: ${STAGE_ID}${NC}\n"

# 3. H2 Console SQL 생성
echo -e "${YELLOW}3. H2 Console에서 다음 SQL을 실행하세요:${NC}"
echo -e "${BLUE}http://localhost:8080/h2-console${NC}\n"

cat << EOF
${GREEN}-- 1. 모든 결제 완료 처리
UPDATE stage_payments 
SET is_paid = true, 
    paid_at = CURRENT_TIMESTAMP, 
    payment_key = CONCAT('TEST-PAY-', id)
WHERE stage_id = ${STAGE_ID};

-- 2. 약정금 생성 및 완료 처리
INSERT INTO stage_payouts (stage_id, username, turn_number, amount, is_completed, scheduled_at, completed_at, transaction_id)
SELECT 
    sp.stage_id,
    sp.username,
    sp.turn_number,
    s.monthly_payment * s.total_participants * (1 + s.interest_rate * (sp.turn_number - 1)) as amount,
    true as is_completed,
    CURRENT_TIMESTAMP as scheduled_at,
    CURRENT_TIMESTAMP as completed_at,
    CONCAT('TXN-TEST-', sp.id) as transaction_id
FROM stage_participants sp
JOIN stages s ON sp.stage_id = s.id
WHERE sp.stage_id = ${STAGE_ID}
AND NOT EXISTS (
    SELECT 1 FROM stage_payouts 
    WHERE stage_id = sp.stage_id 
    AND turn_number = sp.turn_number
);

-- 3. 참여자 수령 상태 업데이트
UPDATE stage_participants 
SET has_received_payout = true,
    payout_received_at = CURRENT_TIMESTAMP
WHERE stage_id = ${STAGE_ID};

-- 4. 스테이지 완료 처리
UPDATE stages 
SET status = 'COMPLETED',
    updated_at = CURRENT_TIMESTAMP
WHERE id = ${STAGE_ID};

-- 5. 확인
SELECT * FROM stages WHERE id = ${STAGE_ID};
SELECT * FROM stage_payouts WHERE stage_id = ${STAGE_ID};${NC}
EOF

echo ""
echo -e "${YELLOW}4. SQL 실행 후 다음 명령어를 실행하세요:${NC}"
echo -e "${GREEN}curl -X POST ${BASE_URL}/api/stages/${STAGE_ID}/settlement -H \"Authorization: Bearer ${TOKEN}\"${NC}"
echo ""
echo -e "${YELLOW}5. 그 다음 브라우저에서 확인:${NC}"
echo -e "${BLUE}http://localhost:8080/stages/${STAGE_ID}/settlement${NC}"
echo ""

# SQL 파일로 저장
cat > /tmp/complete-stage-${STAGE_ID}.sql << EOF
-- 스테이지 ${STAGE_ID} 완료 처리 SQL

-- 1. 모든 결제 완료 처리
UPDATE stage_payments 
SET is_paid = true, 
    paid_at = CURRENT_TIMESTAMP, 
    payment_key = CONCAT('TEST-PAY-', id)
WHERE stage_id = ${STAGE_ID};

-- 2. 약정금 생성 및 완료 처리
INSERT INTO stage_payouts (stage_id, username, turn_number, amount, is_completed, scheduled_at, completed_at, transaction_id)
SELECT 
    sp.stage_id,
    sp.username,
    sp.turn_number,
    s.monthly_payment * s.total_participants * (1 + s.interest_rate * (sp.turn_number - 1)) as amount,
    true as is_completed,
    CURRENT_TIMESTAMP as scheduled_at,
    CURRENT_TIMESTAMP as completed_at,
    CONCAT('TXN-TEST-', sp.id) as transaction_id
FROM stage_participants sp
JOIN stages s ON sp.stage_id = s.id
WHERE sp.stage_id = ${STAGE_ID}
AND NOT EXISTS (
    SELECT 1 FROM stage_payouts 
    WHERE stage_id = sp.stage_id 
    AND turn_number = sp.turn_number
);

-- 3. 참여자 수령 상태 업데이트
UPDATE stage_participants 
SET has_received_payout = true,
    payout_received_at = CURRENT_TIMESTAMP
WHERE stage_id = ${STAGE_ID};

-- 4. 스테이지 완료 처리
UPDATE stages 
SET status = 'COMPLETED',
    updated_at = CURRENT_TIMESTAMP
WHERE id = ${STAGE_ID};

-- 5. 확인
SELECT * FROM stages WHERE id = ${STAGE_ID};
SELECT * FROM stage_payouts WHERE stage_id = ${STAGE_ID};
EOF

echo -e "${GREEN}✅ SQL 파일 생성: /tmp/complete-stage-${STAGE_ID}.sql${NC}"
echo ""

# 토큰 저장
echo $TOKEN > /tmp/stage-token.txt
echo -e "${GREEN}✅ 토큰 저장: /tmp/stage-token.txt${NC}"
echo ""

echo -e "${YELLOW}=== 다음 단계 ===${NC}"
echo -e "${BLUE}1. H2 Console 열기: http://localhost:8080/h2-console${NC}"
echo -e "${BLUE}2. 위의 SQL 복사해서 실행${NC}"
echo -e "${BLUE}3. 정산 생성:${NC}"
echo -e "${GREEN}   curl -X POST ${BASE_URL}/api/stages/${STAGE_ID}/settlement -H \"Authorization: Bearer ${TOKEN}\"${NC}"
echo -e "${BLUE}4. 대시보드 확인: http://localhost:8080/stages/${STAGE_ID}/settlement${NC}"
