#!/bin/bash

# 유통업체로 로그인
echo "1. 유통업체 로그인"
LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "distributor1",
    "password": "password123"
  }')

echo "$LOGIN_RESPONSE" | jq '.'
TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
echo ""

# 상품 목록 조회 (GET)
echo "2. 상품 목록 조회 (GET /api/products/feed)"
curl -s -X GET "http://localhost:8080/api/products/feed" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 내 상품 조회 (GET)
echo "3. 내 상품 조회 (GET /api/products/my)"
curl -s -X GET "http://localhost:8080/api/products/my" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 상품 등록 (POST)
echo "4. 상품 등록 (POST /api/products)"
curl -s -X POST "http://localhost:8080/api/products" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "신선한 사과",
    "description": "맛있는 사과입니다",
    "price": 10000,
    "category": "FOOD",
    "location": "서울",
    "sellerName": "distributor1"
  }' | jq '.'
echo ""

echo "테스트 완료"
