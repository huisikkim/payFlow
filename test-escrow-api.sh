#!/bin/bash

# 에스크로 API 테스트 스크립트
# 사용법: ./test-escrow-api.sh

BASE_URL="http://localhost:8080"
TOKEN=""

echo "🚛 에스크로 API 테스트 시작"
echo "================================"

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 로그인 (JWT 토큰 획득)
echo -e "\n${YELLOW}1. 로그인 (JWT 토큰 획득)${NC}"
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

echo -e "${GREEN}✅ 로그인 성공${NC}"
echo "Token: ${TOKEN:0:20}..."

# 에스크로 거래 생성
echo -e "\n${YELLOW}2. 에스크로 거래 생성${NC}"
CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/escrow" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{
    "buyer": {
      "userId": "buyer1",
      "name": "홍길동",
      "email": "buyer@test.com",
      "phone": "010-1234-5678"
    },
    "seller": {
      "userId": "seller1",
      "name": "김판매",
      "email": "seller@test.com",
      "phone": "010-9876-5432"
    },
    "vehicle": {
      "vin": "VIN123456789",
      "manufacturer": "현대",
      "model": "그랜저",
      "year": 2023,
      "registrationNumber": "12가3456"
    },
    "amount": 50000000,
    "feeRate": 0.03
  }')

TRANSACTION_ID=$(echo $CREATE_RESPONSE | grep -o '"transactionId":"[^"]*' | cut -d'"' -f4)

if [ -z "$TRANSACTION_ID" ]; then
  echo -e "${RED}❌ 거래 생성 실패${NC}"
  echo "Response: $CREATE_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✅ 거래 생성 성공${NC}"
echo "Transaction ID: $TRANSACTION_ID"

# 거래 조회
echo -e "\n${YELLOW}3. 거래 조회${NC}"
GET_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/escrow/${TRANSACTION_ID}" \
  -H "Authorization: Bearer ${TOKEN}")

STATUS=$(echo $GET_RESPONSE | grep -o '"status":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}✅ 거래 조회 성공${NC}"
echo "Status: $STATUS"

# 입금 처리
echo -e "\n${YELLOW}4. 입금 처리${NC}"
DEPOSIT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/escrow/${TRANSACTION_ID}/deposit" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d "{
    \"transactionId\": \"${TRANSACTION_ID}\",
    \"amount\": 50000000,
    \"depositMethod\": \"BANK_TRANSFER\",
    \"depositReference\": \"DEP-001\"
  }")

echo -e "${GREEN}✅ 입금 처리 성공${NC}"

# 거래 상태 확인
echo -e "\n${YELLOW}5. 거래 상태 확인 (입금 후)${NC}"
GET_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/escrow/${TRANSACTION_ID}" \
  -H "Authorization: Bearer ${TOKEN}")

STATUS=$(echo $GET_RESPONSE | grep -o '"status":"[^"]*' | cut -d'"' -f4)
echo "Status: $STATUS"

if [ "$STATUS" = "DEPOSITED" ]; then
  echo -e "${GREEN}✅ 상태 변경 확인 (DEPOSITED)${NC}"
else
  echo -e "${RED}❌ 상태 변경 실패 (Expected: DEPOSITED, Got: $STATUS)${NC}"
fi

# 차량 인도 확인
echo -e "\n${YELLOW}6. 차량 인도 확인${NC}"
DELIVERY_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/escrow/${TRANSACTION_ID}/delivery" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d "{
    \"transactionId\": \"${TRANSACTION_ID}\",
    \"confirmedBy\": \"seller1\"
  }")

echo -e "${GREEN}✅ 차량 인도 확인 성공${NC}"

# 차량 검증
echo -e "\n${YELLOW}7. 차량 검증${NC}"
VERIFICATION_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/escrow/${TRANSACTION_ID}/verification" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d "{
    \"transactionId\": \"${TRANSACTION_ID}\",
    \"type\": \"VEHICLE_CONDITION\",
    \"result\": \"PASSED\",
    \"verifiedBy\": \"inspector1\",
    \"notes\": \"차량 상태 양호\",
    \"documentId\": \"DOC-001\"
  }")

echo -e "${GREEN}✅ 차량 검증 성공${NC}"

# 명의 이전 확인
echo -e "\n${YELLOW}8. 명의 이전 확인${NC}"
OWNERSHIP_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/escrow/${TRANSACTION_ID}/ownership-transfer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d "{
    \"transactionId\": \"${TRANSACTION_ID}\",
    \"verifiedBy\": \"inspector1\",
    \"documentId\": \"DOC-002\",
    \"notes\": \"명의 이전 완료\"
  }")

echo -e "${GREEN}✅ 명의 이전 확인 성공${NC}"

# 이벤트 히스토리 조회
echo -e "\n${YELLOW}9. 이벤트 히스토리 조회${NC}"
EVENTS_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/escrow/${TRANSACTION_ID}/events" \
  -H "Authorization: Bearer ${TOKEN}")

EVENT_COUNT=$(echo $EVENTS_RESPONSE | grep -o '"eventType"' | wc -l)
echo -e "${GREEN}✅ 이벤트 히스토리 조회 성공${NC}"
echo "Event Count: $EVENT_COUNT"

# 구매자별 거래 목록 조회
echo -e "\n${YELLOW}10. 구매자별 거래 목록 조회${NC}"
BUYER_ESCROWS=$(curl -s -X GET "${BASE_URL}/api/escrow/buyer/buyer1" \
  -H "Authorization: Bearer ${TOKEN}")

echo -e "${GREEN}✅ 구매자별 거래 목록 조회 성공${NC}"

# 판매자별 거래 목록 조회
echo -e "\n${YELLOW}11. 판매자별 거래 목록 조회${NC}"
SELLER_ESCROWS=$(curl -s -X GET "${BASE_URL}/api/escrow/seller/seller1" \
  -H "Authorization: Bearer ${TOKEN}")

echo -e "${GREEN}✅ 판매자별 거래 목록 조회 성공${NC}"

echo -e "\n${GREEN}================================${NC}"
echo -e "${GREEN}🎉 모든 테스트 완료!${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo "생성된 거래 ID: $TRANSACTION_ID"
echo "최종 상태: OWNERSHIP_TRANSFERRED"
echo ""
echo "다음 단계:"
echo "  - 관리자 권한으로 정산 시작: POST /api/escrow/${TRANSACTION_ID}/settlement/start"
echo "  - 정산 완료: POST /api/escrow/${TRANSACTION_ID}/settlement/complete"
echo "  - 웹 UI 확인: http://localhost:8080/escrow/${TRANSACTION_ID}"
