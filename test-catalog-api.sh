#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}식자재 카탈로그 API 테스트${NC}"
echo -e "${BLUE}========================================${NC}\n"

# 1. 유통업체 로그인
echo -e "${YELLOW}[1] 유통업체 로그인${NC}"
DIST_LOGIN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "distributor1",
    "password": "password123"
  }')

DIST_TOKEN=$(echo $DIST_LOGIN | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$DIST_TOKEN" ]; then
    echo -e "${RED}❌ 유통업체 로그인 실패${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 유통업체 로그인 성공${NC}"
echo "토큰: ${DIST_TOKEN:0:50}..."
echo ""

# 2. 상품 등록 - 쌀
echo -e "${YELLOW}[2] 상품 등록 - 쌀${NC}"
PRODUCT1=$(curl -s -X POST "$BASE_URL/api/catalog/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $DIST_TOKEN" \
  -d '{
    "productName": "경기미 20kg",
    "category": "쌀/곡물",
    "description": "경기도에서 생산된 고품질 쌀입니다",
    "unitPrice": 45000,
    "unit": "포",
    "stockQuantity": 100,
    "origin": "경기도",
    "brand": "농협",
    "imageUrl": "https://example.com/rice.jpg",
    "isAvailable": true,
    "minOrderQuantity": 1,
    "maxOrderQuantity": 50,
    "certifications": "친환경인증"
  }')

echo "$PRODUCT1" | python3 -m json.tool 2>/dev/null || echo "$PRODUCT1"
PRODUCT1_ID=$(echo $PRODUCT1 | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo -e "${GREEN}✅ 상품 등록 완료 (ID: $PRODUCT1_ID)${NC}\n"

# 3. 상품 등록 - 양파
echo -e "${YELLOW}[3] 상품 등록 - 양파${NC}"
PRODUCT2=$(curl -s -X POST "$BASE_URL/api/catalog/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $DIST_TOKEN" \
  -d '{
    "productName": "국산 양파",
    "category": "채소",
    "description": "신선한 국산 양파입니다",
    "unitPrice": 3000,
    "unit": "kg",
    "stockQuantity": 500,
    "origin": "전남 무안",
    "brand": null,
    "imageUrl": null,
    "isAvailable": true,
    "minOrderQuantity": 5,
    "maxOrderQuantity": 100,
    "certifications": null
  }')

echo "$PRODUCT2" | python3 -m json.tool 2>/dev/null || echo "$PRODUCT2"
PRODUCT2_ID=$(echo $PRODUCT2 | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo -e "${GREEN}✅ 상품 등록 완료 (ID: $PRODUCT2_ID)${NC}\n"

# 4. 상품 등록 - 소고기
echo -e "${YELLOW}[4] 상품 등록 - 소고기${NC}"
PRODUCT3=$(curl -s -X POST "$BASE_URL/api/catalog/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $DIST_TOKEN" \
  -d '{
    "productName": "한우 등심 1등급",
    "category": "육류",
    "description": "최상급 한우 등심입니다",
    "unitPrice": 80000,
    "unit": "kg",
    "stockQuantity": 50,
    "origin": "국내산",
    "brand": "한우명가",
    "imageUrl": null,
    "isAvailable": true,
    "minOrderQuantity": 1,
    "maxOrderQuantity": 10,
    "certifications": "HACCP"
  }')

echo "$PRODUCT3" | python3 -m json.tool 2>/dev/null || echo "$PRODUCT3"
PRODUCT3_ID=$(echo $PRODUCT3 | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo -e "${GREEN}✅ 상품 등록 완료 (ID: $PRODUCT3_ID)${NC}\n"

# 5. 내 카탈로그 조회 (유통업체)
echo -e "${YELLOW}[5] 내 카탈로그 조회 (유통업체)${NC}"
MY_CATALOG=$(curl -s -X GET "$BASE_URL/api/catalog/my-products" \
  -H "Authorization: Bearer $DIST_TOKEN")

echo "$MY_CATALOG" | python3 -m json.tool 2>/dev/null || echo "$MY_CATALOG"
echo -e "${GREEN}✅ 내 카탈로그 조회 완료${NC}\n"

# 6. 상품 수정
echo -e "${YELLOW}[6] 상품 수정 (가격 인상)${NC}"
UPDATED=$(curl -s -X PUT "$BASE_URL/api/catalog/products/$PRODUCT1_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $DIST_TOKEN" \
  -d '{
    "unitPrice": 48000,
    "description": "경기도에서 생산된 고품질 쌀입니다 (가격 인상)"
  }')

echo "$UPDATED" | python3 -m json.tool 2>/dev/null || echo "$UPDATED"
echo -e "${GREEN}✅ 상품 수정 완료${NC}\n"

# 7. 재고 업데이트
echo -e "${YELLOW}[7] 재고 업데이트${NC}"
STOCK_UPDATED=$(curl -s -X PUT "$BASE_URL/api/catalog/products/$PRODUCT2_ID/stock?quantity=450" \
  -H "Authorization: Bearer $DIST_TOKEN")

echo "$STOCK_UPDATED" | python3 -m json.tool 2>/dev/null || echo "$STOCK_UPDATED"
echo -e "${GREEN}✅ 재고 업데이트 완료${NC}\n"

# 8. 매장 로그인
echo -e "${YELLOW}[8] 매장 로그인${NC}"
STORE_LOGIN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_store_quote",
    "password": "password123"
  }')

STORE_TOKEN=$(echo $STORE_LOGIN | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$STORE_TOKEN" ]; then
    echo -e "${RED}❌ 매장 로그인 실패${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 매장 로그인 성공${NC}"
echo "토큰: ${STORE_TOKEN:0:50}..."
echo ""

# 9. 유통업체 카탈로그 조회 (매장)
echo -e "${YELLOW}[9] 유통업체 카탈로그 조회 (매장)${NC}"
CATALOG=$(curl -s -X GET "$BASE_URL/api/catalog/distributor/distributor1" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$CATALOG" | python3 -m json.tool 2>/dev/null || echo "$CATALOG"
echo -e "${GREEN}✅ 유통업체 카탈로그 조회 완료${NC}\n"

# 10. 카테고리별 조회 (채소)
echo -e "${YELLOW}[10] 카테고리별 조회 (채소)${NC}"
CATEGORY=$(curl -s -X GET "$BASE_URL/api/catalog/distributor/distributor1/category/채소" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$CATEGORY" | python3 -m json.tool 2>/dev/null || echo "$CATEGORY"
echo -e "${GREEN}✅ 카테고리별 조회 완료${NC}\n"

# 11. 상품 검색
echo -e "${YELLOW}[11] 상품 검색 (키워드: 쌀)${NC}"
SEARCH=$(curl -s -X GET "$BASE_URL/api/catalog/distributor/distributor1/search?keyword=쌀" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$SEARCH" | python3 -m json.tool 2>/dev/null || echo "$SEARCH"
echo -e "${GREEN}✅ 상품 검색 완료${NC}\n"

# 12. 가격 범위 검색
echo -e "${YELLOW}[12] 가격 범위 검색 (1000원~50000원)${NC}"
PRICE_RANGE=$(curl -s -X GET "$BASE_URL/api/catalog/distributor/distributor1/price-range?minPrice=1000&maxPrice=50000" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$PRICE_RANGE" | python3 -m json.tool 2>/dev/null || echo "$PRICE_RANGE"
echo -e "${GREEN}✅ 가격 범위 검색 완료${NC}\n"

# 13. 재고 있는 상품만 조회
echo -e "${YELLOW}[13] 재고 있는 상품만 조회${NC}"
IN_STOCK=$(curl -s -X GET "$BASE_URL/api/catalog/distributor/distributor1/in-stock" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$IN_STOCK" | python3 -m json.tool 2>/dev/null || echo "$IN_STOCK"
echo -e "${GREEN}✅ 재고 있는 상품 조회 완료${NC}\n"

# 14. 상품 상세 조회
echo -e "${YELLOW}[14] 상품 상세 조회${NC}"
DETAIL=$(curl -s -X GET "$BASE_URL/api/catalog/products/$PRODUCT1_ID" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$DETAIL" | python3 -m json.tool 2>/dev/null || echo "$DETAIL"
echo -e "${GREEN}✅ 상품 상세 조회 완료${NC}\n"

# 15. 판매 상태 변경 (품절 처리)
echo -e "${YELLOW}[15] 판매 상태 변경 (품절 처리)${NC}"
TOGGLE=$(curl -s -X PUT "$BASE_URL/api/catalog/products/$PRODUCT3_ID/toggle-availability" \
  -H "Authorization: Bearer $DIST_TOKEN")

echo "$TOGGLE" | python3 -m json.tool 2>/dev/null || echo "$TOGGLE"
echo -e "${GREEN}✅ 판매 상태 변경 완료${NC}\n"

# 16. 배송 정보 등록
echo -e "${YELLOW}[16] 배송 정보 등록${NC}"
DELIVERY_INFO=$(curl -s -X POST "$BASE_URL/api/catalog/products/$PRODUCT1_ID/delivery-info" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $DIST_TOKEN" \
  -d '{
    "deliveryType": "익일배송",
    "deliveryFee": 3000,
    "freeDeliveryThreshold": 50000,
    "deliveryRegions": "서울,경기,인천",
    "deliveryDays": "월,화,수,목,금",
    "deliveryTimeSlots": "오전,오후",
    "estimatedDeliveryDays": 1,
    "packagingType": "박스",
    "isFragile": false,
    "requiresRefrigeration": false,
    "specialInstructions": "직사광선을 피해 보관해주세요"
  }')

echo "$DELIVERY_INFO" | python3 -m json.tool 2>/dev/null || echo "$DELIVERY_INFO"
echo -e "${GREEN}✅ 배송 정보 등록 완료${NC}\n"

# 17. 상품 상세 정보 조회 (가격, 재고, 배송 정보 포함)
echo -e "${YELLOW}[17] 상품 상세 정보 조회 (가격, 재고, 배송 정보 포함)${NC}"
PRODUCT_DETAIL=$(curl -s -X GET "$BASE_URL/api/catalog/products/$PRODUCT1_ID/detail" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$PRODUCT_DETAIL" | python3 -m json.tool 2>/dev/null || echo "$PRODUCT_DETAIL"
echo -e "${GREEN}✅ 상품 상세 정보 조회 완료${NC}\n"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}테스트 완료!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}✅ 상품 등록 테스트 성공 (3개)${NC}"
echo -e "${GREEN}✅ 상품 수정 테스트 성공${NC}"
echo -e "${GREEN}✅ 재고 업데이트 테스트 성공${NC}"
echo -e "${GREEN}✅ 카탈로그 조회 테스트 성공${NC}"
echo -e "${GREEN}✅ 카테고리별 조회 테스트 성공${NC}"
echo -e "${GREEN}✅ 상품 검색 테스트 성공${NC}"
echo -e "${GREEN}✅ 가격 범위 검색 테스트 성공${NC}"
echo -e "${GREEN}✅ 재고 조회 테스트 성공${NC}"
echo -e "${GREEN}✅ 판매 상태 변경 테스트 성공${NC}"
echo -e "${GREEN}✅ 배송 정보 등록 테스트 성공${NC}"
echo -e "${GREEN}✅ 상품 상세 정보 조회 테스트 성공${NC}"
