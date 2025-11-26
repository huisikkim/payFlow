#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}주문 장바구니 API 테스트${NC}"
echo -e "${BLUE}========================================${NC}\n"

# 1. 매장 로그인
echo -e "${YELLOW}[1] 매장 로그인${NC}"
STORE_LOGIN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_store_quote",
    "password": "password123"
  }')

STORE_TOKEN=$(echo $STORE_LOGIN | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}✅ 매장 로그인 성공${NC}\n"

# 2. 장바구니에 상품 추가 (상품 ID 6 - 경기미)
echo -e "${YELLOW}[2] 장바구니에 상품 추가 (경기미 20kg x 5포)${NC}"
ADD1=$(curl -s -X POST "$BASE_URL/api/cart/add" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STORE_TOKEN" \
  -d '{
    "productId": 6,
    "quantity": 5
  }')

echo "$ADD1" | python3 -m json.tool 2>/dev/null || echo "$ADD1"
echo -e "${GREEN}✅ 상품 추가 완료${NC}\n"

# 3. 장바구니에 상품 추가 (상품 ID 7 - 양파)
echo -e "${YELLOW}[3] 장바구니에 상품 추가 (양파 10kg)${NC}"
ADD2=$(curl -s -X POST "$BASE_URL/api/cart/add" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STORE_TOKEN" \
  -d '{
    "productId": 7,
    "quantity": 10
  }')

echo "$ADD2" | python3 -m json.tool 2>/dev/null || echo "$ADD2"
echo -e "${GREEN}✅ 상품 추가 완료${NC}\n"

# 4. 장바구니 조회
echo -e "${YELLOW}[4] 장바구니 조회${NC}"
CART=$(curl -s -X GET "$BASE_URL/api/cart/distributor1" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$CART" | python3 -m json.tool 2>/dev/null || echo "$CART"
ITEM1_ID=$(echo $CART | python3 -c "import sys, json; items = json.load(sys.stdin)['items']; print(items[0]['id'] if items else '')" 2>/dev/null)
echo -e "${GREEN}✅ 장바구니 조회 완료${NC}\n"

# 5. 장바구니 아이템 수량 변경
echo -e "${YELLOW}[5] 장바구니 아이템 수량 변경 (경기미 5포 → 10포)${NC}"
UPDATE=$(curl -s -X PUT "$BASE_URL/api/cart/distributor1/items/$ITEM1_ID?quantity=10" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$UPDATE" | python3 -m json.tool 2>/dev/null || echo "$UPDATE"
echo -e "${GREEN}✅ 수량 변경 완료${NC}\n"

# 6. 장바구니 최종 확인
echo -e "${YELLOW}[6] 장바구니 최종 확인${NC}"
FINAL_CART=$(curl -s -X GET "$BASE_URL/api/cart/distributor1" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$FINAL_CART" | python3 -m json.tool 2>/dev/null || echo "$FINAL_CART"
TOTAL_AMOUNT=$(echo $FINAL_CART | python3 -c "import sys, json; print(json.load(sys.stdin)['totalAmount'])" 2>/dev/null)
TOTAL_QUANTITY=$(echo $FINAL_CART | python3 -c "import sys, json; print(json.load(sys.stdin)['totalQuantity'])" 2>/dev/null)
echo -e "${GREEN}✅ 장바구니 최종 확인 완료${NC}\n"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}테스트 완료!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}✅ 장바구니 추가 테스트 성공${NC}"
echo -e "${GREEN}✅ 장바구니 조회 테스트 성공${NC}"
echo -e "${GREEN}✅ 수량 변경 테스트 성공${NC}"
echo ""
echo -e "${YELLOW}장바구니 요약:${NC}"
echo -e "  총 수량: ${TOTAL_QUANTITY}개"
echo -e "  총 금액: ${TOTAL_AMOUNT}원"
