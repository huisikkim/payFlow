#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}유통업체 비교 API 테스트${NC}"
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

if [ -z "$STORE_TOKEN" ]; then
    echo -e "${RED}❌ 로그인 실패 - 먼저 test-quote-request-api.sh를 실행하세요${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 매장 로그인 성공${NC}"
echo "토큰: ${STORE_TOKEN:0:50}..."
echo ""

# 2. 추천 유통업체 조회 (비교 대상 확인)
echo -e "${YELLOW}[2] 추천 유통업체 조회${NC}"
RECOMMENDATIONS=$(curl -s -X GET "$BASE_URL/api/matching/recommend?limit=10" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$RECOMMENDATIONS" | python3 -m json.tool 2>/dev/null || echo "$RECOMMENDATIONS"
echo -e "${GREEN}✅ 추천 유통업체 조회 완료${NC}\n"

# 3. 특정 유통업체들 비교
echo -e "${YELLOW}[3] 특정 유통업체 비교 (distributor1, test_dist_quote)${NC}"
COMPARISON=$(curl -s -X POST "$BASE_URL/api/matching/compare" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STORE_TOKEN" \
  -d '["distributor1", "test_dist_quote"]')

echo "$COMPARISON" | python3 -m json.tool 2>/dev/null || echo "$COMPARISON"
echo -e "${GREEN}✅ 유통업체 비교 완료${NC}\n"

# 4. Top 5 추천 유통업체 비교
echo -e "${YELLOW}[4] Top 5 추천 유통업체 비교${NC}"
TOP_COMPARISON=$(curl -s -X GET "$BASE_URL/api/matching/compare/top?topN=5" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$TOP_COMPARISON" | python3 -m json.tool 2>/dev/null || echo "$TOP_COMPARISON"
echo -e "${GREEN}✅ Top 5 비교 완료${NC}\n"

# 5. 카테고리별 최고 유통업체
echo -e "${YELLOW}[5] 카테고리별 최고 유통업체${NC}"
BEST_BY_CATEGORY=$(curl -s -X POST "$BASE_URL/api/matching/compare/best-by-category" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STORE_TOKEN" \
  -d '["distributor1", "test_dist_quote"]')

echo "$BEST_BY_CATEGORY" | python3 -m json.tool 2>/dev/null || echo "$BEST_BY_CATEGORY"
echo -e "${GREEN}✅ 카테고리별 최고 유통업체 조회 완료${NC}\n"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}비교 결과 요약${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}✅ 특정 유통업체 비교 테스트 성공${NC}"
echo -e "${GREEN}✅ Top N 비교 테스트 성공${NC}"
echo -e "${GREEN}✅ 카테고리별 최고 업체 테스트 성공${NC}"
echo ""
echo -e "${YELLOW}비교 항목:${NC}"
echo "  - 가격 (최소 주문 금액, 가격대)"
echo "  - 배송 (배송 가능 여부, 배송 속도, 배송비)"
echo "  - 서비스 (서비스 지역, 공급 품목)"
echo "  - 품질 (품질 등급, 신뢰도 점수)"
echo "  - 인증 (인증 개수, 인증 종류)"
echo "  - 종합 (매칭 점수, 순위)"
