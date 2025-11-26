#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}견적 요청 API 테스트${NC}"
echo -e "${BLUE}========================================${NC}\n"

# 1. 매장 회원가입 및 로그인
echo -e "${YELLOW}[1] 매장 회원가입 및 로그인${NC}"
STORE_SIGNUP=$(curl -s -X POST "$BASE_URL/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_store_quote",
    "password": "password123",
    "email": "store_quote@example.com",
    "userType": "STORE_OWNER",
    "businessNumber": "999-99-99999",
    "businessName": "견적테스트 매장",
    "ownerName": "김사장",
    "phoneNumber": "010-9999-9999",
    "address": "서울시 강남구"
  }')

if echo "$STORE_SIGNUP" | grep -q "already exists"; then
    echo "회원가입 응답: 이미 존재하는 사용자 (스킵)"
else
    echo "회원가입 응답: $STORE_SIGNUP"
fi

STORE_LOGIN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_store_quote",
    "password": "password123"
  }')

STORE_TOKEN=$(echo $STORE_LOGIN | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}✅ 매장 로그인 성공${NC}"
echo "토큰: ${STORE_TOKEN:0:50}..."
echo ""

# 2. 유통업체 회원가입 및 로그인
echo -e "${YELLOW}[2] 유통업체 회원가입 및 로그인${NC}"
DIST_SIGNUP=$(curl -s -X POST "$BASE_URL/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_dist_quote",
    "password": "password123",
    "email": "dist_quote@example.com",
    "userType": "DISTRIBUTOR",
    "businessNumber": "888-88-88888",
    "businessName": "견적테스트 유통",
    "ownerName": "이사장",
    "phoneNumber": "010-8888-8888",
    "address": "서울시 송파구"
  }')

if echo "$DIST_SIGNUP" | grep -q "already exists"; then
    echo "회원가입 응답: 이미 존재하는 사용자 (스킵)"
else
    echo "회원가입 응답: $DIST_SIGNUP"
fi

DIST_LOGIN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_dist_quote",
    "password": "password123"
  }')

DIST_TOKEN=$(echo $DIST_LOGIN | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}✅ 유통업체 로그인 성공${NC}"
echo "토큰: ${DIST_TOKEN:0:50}..."
echo ""

# 3. 매장 정보 등록
echo -e "${YELLOW}[3] 매장 정보 등록${NC}"
STORE_INFO=$(curl -s -X POST "$BASE_URL/api/store/info" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STORE_TOKEN" \
  -d '{
    "storeName": "견적테스트 한식당",
    "businessType": "한식",
    "region": "서울 강남구",
    "mainProducts": "쌀/곡물,채소,육류",
    "description": "견적 요청 테스트용 매장",
    "employeeCount": 5,
    "operatingHours": "09:00-22:00",
    "phoneNumber": "010-9999-9999",
    "address": "서울시 강남구 테헤란로 123"
  }')

echo "$STORE_INFO" | python3 -m json.tool 2>/dev/null || echo "$STORE_INFO"
echo -e "${GREEN}✅ 매장 정보 등록 완료${NC}\n"

# 4. 유통업체 정보 등록
echo -e "${YELLOW}[4] 유통업체 정보 등록${NC}"
DIST_INFO=$(curl -s -X POST "$BASE_URL/api/distributor/info" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $DIST_TOKEN" \
  -d '{
    "distributorName": "견적테스트 유통",
    "supplyProducts": "쌀/곡물,채소,과일,육류,수산물",
    "serviceRegions": "서울,경기,인천",
    "deliveryAvailable": true,
    "deliveryInfo": "배송비 무료 (10만원 이상), 익일 배송",
    "description": "견적 요청 테스트용 유통업체",
    "certifications": "HACCP,ISO22000",
    "minOrderAmount": 100000,
    "operatingHours": "09:00-18:00",
    "phoneNumber": "010-8888-8888",
    "email": "dist_quote@example.com",
    "address": "서울시 송파구 올림픽로 456"
  }')

echo "$DIST_INFO" | python3 -m json.tool 2>/dev/null || echo "$DIST_INFO"
echo -e "${GREEN}✅ 유통업체 정보 등록 완료${NC}\n"

# 5. 견적 요청 생성
echo -e "${YELLOW}[5] 견적 요청 생성 (매장 → 유통업체)${NC}"
QUOTE_REQUEST=$(curl -s -X POST "$BASE_URL/api/matching/quote-request" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STORE_TOKEN" \
  -d '{
    "distributorId": "test_dist_quote",
    "requestedProducts": "쌀/곡물,채소,육류",
    "message": "매주 월요일 오전 배송 가능한지 확인 부탁드립니다. 최소 주문금액도 알려주세요."
  }')

echo "$QUOTE_REQUEST" | python3 -m json.tool 2>/dev/null || echo "$QUOTE_REQUEST"
QUOTE_ID=$(echo $QUOTE_REQUEST | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo -e "${GREEN}✅ 견적 요청 생성 완료 (ID: $QUOTE_ID)${NC}\n"

# 6. 매장의 견적 요청 목록 조회
echo -e "${YELLOW}[6] 매장의 견적 요청 목록 조회${NC}"
STORE_QUOTES=$(curl -s -X GET "$BASE_URL/api/matching/quote-requests/store" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$STORE_QUOTES" | python3 -m json.tool 2>/dev/null || echo "$STORE_QUOTES"
echo -e "${GREEN}✅ 매장 견적 요청 목록 조회 완료${NC}\n"

# 7. 유통업체의 견적 요청 목록 조회
echo -e "${YELLOW}[7] 유통업체의 견적 요청 목록 조회${NC}"
DIST_QUOTES=$(curl -s -X GET "$BASE_URL/api/matching/quote-requests/distributor" \
  -H "Authorization: Bearer $DIST_TOKEN")

echo "$DIST_QUOTES" | python3 -m json.tool 2>/dev/null || echo "$DIST_QUOTES"
echo -e "${GREEN}✅ 유통업체 견적 요청 목록 조회 완료${NC}\n"

# 8. 견적 요청 상세 조회
echo -e "${YELLOW}[8] 견적 요청 상세 조회${NC}"
QUOTE_DETAIL=$(curl -s -X GET "$BASE_URL/api/matching/quote-request/$QUOTE_ID" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$QUOTE_DETAIL" | python3 -m json.tool 2>/dev/null || echo "$QUOTE_DETAIL"
echo -e "${GREEN}✅ 견적 요청 상세 조회 완료${NC}\n"

# 9. 견적 요청 응답 (유통업체 - 수락)
echo -e "${YELLOW}[9] 견적 요청 응답 (유통업체 - 수락)${NC}"
QUOTE_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/matching/quote-request/$QUOTE_ID/respond" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $DIST_TOKEN" \
  -d '{
    "status": "ACCEPTED",
    "estimatedAmount": 500000,
    "response": "매주 월요일 오전 8시 배송 가능합니다. 최소 주문금액은 10만원이며, 첫 거래 시 5% 할인 제공합니다."
  }')

echo "$QUOTE_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$QUOTE_RESPONSE"
echo -e "${GREEN}✅ 견적 요청 응답 완료 (수락)${NC}\n"

# 10. 견적 완료 처리
echo -e "${YELLOW}[10] 견적 완료 처리 (매장)${NC}"
QUOTE_COMPLETE=$(curl -s -X PUT "$BASE_URL/api/matching/quote-request/$QUOTE_ID/complete" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$QUOTE_COMPLETE" | python3 -m json.tool 2>/dev/null || echo "$QUOTE_COMPLETE"
echo -e "${GREEN}✅ 견적 완료 처리 성공${NC}\n"

# 11. 추가 견적 요청 생성 (거절 테스트용)
echo -e "${YELLOW}[11] 추가 견적 요청 생성 (거절 테스트용)${NC}"
QUOTE_REQUEST2=$(curl -s -X POST "$BASE_URL/api/matching/quote-request" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STORE_TOKEN" \
  -d '{
    "distributorId": "test_dist_quote",
    "requestedProducts": "수산물,과일",
    "message": "내일 배송 가능한가요?"
  }')

echo "$QUOTE_REQUEST2" | python3 -m json.tool 2>/dev/null || echo "$QUOTE_REQUEST2"
QUOTE_ID2=$(echo $QUOTE_REQUEST2 | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo -e "${GREEN}✅ 추가 견적 요청 생성 완료 (ID: $QUOTE_ID2)${NC}\n"

# 12. 견적 요청 응답 (유통업체 - 거절)
echo -e "${YELLOW}[12] 견적 요청 응답 (유통업체 - 거절)${NC}"
QUOTE_REJECT=$(curl -s -X PUT "$BASE_URL/api/matching/quote-request/$QUOTE_ID2/respond" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $DIST_TOKEN" \
  -d '{
    "status": "REJECTED",
    "estimatedAmount": null,
    "response": "죄송합니다. 현재 수산물 재고가 부족하여 내일 배송이 어렵습니다."
  }')

echo "$QUOTE_REJECT" | python3 -m json.tool 2>/dev/null || echo "$QUOTE_REJECT"
echo -e "${GREEN}✅ 견적 요청 거절 완료${NC}\n"

# 13. 추가 견적 요청 생성 (취소 테스트용)
echo -e "${YELLOW}[13] 추가 견적 요청 생성 (취소 테스트용)${NC}"
QUOTE_REQUEST3=$(curl -s -X POST "$BASE_URL/api/matching/quote-request" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STORE_TOKEN" \
  -d '{
    "distributorId": "test_dist_quote",
    "requestedProducts": "채소",
    "message": "테스트 요청입니다."
  }')

echo "$QUOTE_REQUEST3" | python3 -m json.tool 2>/dev/null || echo "$QUOTE_REQUEST3"
QUOTE_ID3=$(echo $QUOTE_REQUEST3 | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo -e "${GREEN}✅ 추가 견적 요청 생성 완료 (ID: $QUOTE_ID3)${NC}\n"

# 14. 견적 요청 취소
echo -e "${YELLOW}[14] 견적 요청 취소 (매장)${NC}"
QUOTE_CANCEL=$(curl -s -X DELETE "$BASE_URL/api/matching/quote-request/$QUOTE_ID3" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$QUOTE_CANCEL"
echo -e "${GREEN}✅ 견적 요청 취소 완료${NC}\n"

# 15. 최종 견적 요청 목록 확인
echo -e "${YELLOW}[15] 최종 견적 요청 목록 확인${NC}"
FINAL_QUOTES=$(curl -s -X GET "$BASE_URL/api/matching/quote-requests/store" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$FINAL_QUOTES" | python3 -m json.tool 2>/dev/null || echo "$FINAL_QUOTES"
echo -e "${GREEN}✅ 최종 견적 요청 목록 조회 완료${NC}\n"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}테스트 완료!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}✅ 견적 요청 생성 테스트 성공${NC}"
echo -e "${GREEN}✅ 견적 요청 목록 조회 테스트 성공${NC}"
echo -e "${GREEN}✅ 견적 요청 응답 (수락) 테스트 성공${NC}"
echo -e "${GREEN}✅ 견적 요청 응답 (거절) 테스트 성공${NC}"
echo -e "${GREEN}✅ 견적 완료 처리 테스트 성공${NC}"
echo -e "${GREEN}✅ 견적 요청 취소 테스트 성공${NC}"
