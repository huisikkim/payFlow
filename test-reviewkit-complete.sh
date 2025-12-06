#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "  ReviewKit 전체 플로우 테스트"
echo "=========================================="
echo ""

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: 비즈니스 생성
echo -e "${BLUE}[1/6] 비즈니스 생성 중...${NC}"
BUSINESS_RESPONSE=$(curl -s -X POST "$BASE_URL/api/reviewkit/businesses" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍대 지민 카페",
    "description": "최고의 커피를 제공하는 카페",
    "websiteUrl": "https://jimin-cafe.com"
  }')
echo "✓ 비즈니스 생성 완료"
echo ""

# Step 2: 리뷰 제출 (여러 개)
echo -e "${BLUE}[2/6] 리뷰 제출 중...${NC}"

# 리뷰 1
curl -s -X POST "$BASE_URL/api/reviewkit/businesses/hongdae-jimin-cafe/reviews" \
  -H "Content-Type: application/json" \
  -d '{
    "reviewerName": "김철수",
    "reviewerEmail": "chulsoo@gmail.com",
    "rating": 5,
    "content": "커피 정말 맛있었어요! 특히 아인슈페너 추천합니다 ☕"
  }' > /dev/null

# 리뷰 2
curl -s -X POST "$BASE_URL/api/reviewkit/businesses/hongdae-jimin-cafe/reviews" \
  -H "Content-Type: application/json" \
  -d '{
    "reviewerName": "이영희",
    "reviewerEmail": "younghee@naver.com",
    "rating": 5,
    "content": "분위기도 좋고 직원분들도 친절하세요. 자주 올게요!"
  }' > /dev/null

# 리뷰 3
curl -s -X POST "$BASE_URL/api/reviewkit/businesses/hongdae-jimin-cafe/reviews" \
  -H "Content-Type: application/json" \
  -d '{
    "reviewerName": "박민수",
    "reviewerEmail": "minsu@kakao.com",
    "rating": 4,
    "content": "케이크도 맛있고 커피도 좋아요. 다만 주말엔 사람이 많아서 조금 시끄러워요."
  }' > /dev/null

# 리뷰 4
curl -s -X POST "$BASE_URL/api/reviewkit/businesses/hongdae-jimin-cafe/reviews" \
  -H "Content-Type: application/json" \
  -d '{
    "reviewerName": "최수진",
    "reviewerEmail": "sujin@gmail.com",
    "reviewerCompany": "ABC 스타트업",
    "rating": 5,
    "content": "회의하기 좋은 공간이에요. 와이파이도 빠르고 콘센트도 많아요!"
  }' > /dev/null

# 리뷰 5
curl -s -X POST "$BASE_URL/api/reviewkit/businesses/hongdae-jimin-cafe/reviews" \
  -H "Content-Type: application/json" \
  -d '{
    "reviewerName": "정대현",
    "reviewerEmail": "daehyun@company.com",
    "rating": 5,
    "content": "홍대에서 제일 좋아하는 카페입니다. 사장님도 친절하시고 커피 맛도 일품!"
  }' > /dev/null

# 리뷰 6
curl -s -X POST "$BASE_URL/api/reviewkit/businesses/hongdae-jimin-cafe/reviews" \
  -H "Content-Type: application/json" \
  -d '{
    "reviewerName": "강지은",
    "reviewerEmail": "jieun@email.com",
    "rating": 4,
    "content": "디저트가 정말 맛있어요. 특히 티라미수 강추!"
  }' > /dev/null

echo "✓ 6개 리뷰 제출 완료"
echo ""

# Step 3: 리뷰 승인
echo -e "${BLUE}[3/6] 리뷰 승인 중...${NC}"
for i in {1..6}; do
  curl -s -X POST "$BASE_URL/api/reviewkit/reviews/$i/approve" > /dev/null
done
echo "✓ 6개 리뷰 승인 완료"
echo ""

# Step 4: 위젯 생성
echo -e "${BLUE}[4/6] 위젯 생성 중...${NC}"
# Note: 위젯은 웹 UI를 통해 생성해야 합니다
echo "⚠ 위젯은 대시보드에서 수동으로 생성해주세요:"
echo "   → $BASE_URL/reviewkit/businesses/1"
echo ""

# Step 5: 승인된 리뷰 조회
echo -e "${BLUE}[5/6] 승인된 리뷰 조회...${NC}"
REVIEWS=$(curl -s "$BASE_URL/api/reviewkit/businesses/1/reviews")
echo "$REVIEWS" | python3 -m json.tool 2>/dev/null || echo "$REVIEWS"
echo ""

# Step 6: 접속 URL 안내
echo -e "${GREEN}=========================================="
echo "  ✅ 테스트 완료!"
echo "==========================================${NC}"
echo ""
echo -e "${YELLOW}📍 접속 URL:${NC}"
echo ""
echo "1. 랜딩 페이지:"
echo "   → $BASE_URL/reviewkit"
echo ""
echo "2. 대시보드:"
echo "   → $BASE_URL/reviewkit/dashboard"
echo ""
echo "3. 비즈니스 상세 (리뷰 관리):"
echo "   → $BASE_URL/reviewkit/businesses/1"
echo ""
echo "4. 리뷰 작성 페이지 (공개):"
echo "   → $BASE_URL/reviewkit/r/hongdae-jimin-cafe"
echo ""
echo "5. 위젯 데모:"
echo "   → $BASE_URL/reviewkit/widget-demo"
echo ""
echo -e "${YELLOW}📝 다음 단계:${NC}"
echo "1. 대시보드에서 위젯 생성"
echo "2. 생성된 위젯 ID로 위젯 데모 페이지에서 테스트"
echo "3. 임베드 코드를 복사해서 실제 웹사이트에 붙여넣기"
echo ""
