#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=== ReviewKit API Test ==="
echo ""

# 1. Create Business
echo "1. Creating business..."
BUSINESS_RESPONSE=$(curl -s -X POST "$BASE_URL/api/reviewkit/businesses" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍대 지민 카페",
    "description": "최고의 커피를 제공하는 카페",
    "websiteUrl": "https://jimin-cafe.com"
  }')
echo "Response: $BUSINESS_RESPONSE"
echo ""

# 2. Submit Review (Public API)
echo "2. Submitting review..."
REVIEW_RESPONSE=$(curl -s -X POST "$BASE_URL/api/reviewkit/businesses/jimin-cafe/reviews" \
  -H "Content-Type: application/json" \
  -d '{
    "reviewerName": "김철수",
    "reviewerEmail": "chulsoo@gmail.com",
    "rating": 5,
    "content": "커피 정말 맛있었어요! 특히 아인슈페너 추천합니다"
  }')
echo "Response: $REVIEW_RESPONSE"
echo ""

# 3. Get Reviews
echo "3. Getting reviews..."
curl -s "$BASE_URL/api/reviewkit/businesses/1/reviews" | jq '.'
echo ""

# 4. Approve Review
echo "4. Approving review..."
curl -s -X POST "$BASE_URL/api/reviewkit/reviews/1/approve" | jq '.'
echo ""

# 5. Get Widget Reviews (Public API)
echo "5. Getting widget reviews..."
curl -s "$BASE_URL/api/widgets/test-widget-id/reviews" | jq '.'
echo ""

echo "=== Test Complete ==="
