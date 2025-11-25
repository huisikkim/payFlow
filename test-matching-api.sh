#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "유통업체 매칭 및 추천 API 테스트"
echo "=========================================="
echo ""

# 1. 매장 사장님 로그인
echo "1. 매장 사장님 로그인"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_store1",
    "password": "password123"
  }')

echo "$LOGIN_RESPONSE" | jq '.'
TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
echo ""

# 2. 맞춤 유통업체 추천 (상위 5개)
echo "2. 맞춤 유통업체 추천 (상위 5개)"
echo "GET $BASE_URL/api/matching/recommend?limit=5"
curl -s -X GET "$BASE_URL/api/matching/recommend?limit=5" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 3. 품목별 유통업체 검색 (쌀)
echo "3. 품목별 유통업체 검색 (쌀/곡물)"
echo "GET $BASE_URL/api/matching/search/product?keyword=쌀"
curl -s -X GET "$BASE_URL/api/matching/search/product?keyword=쌀" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 4. 지역별 유통업체 검색 (서울)
echo "4. 지역별 유통업체 검색 (서울)"
echo "GET $BASE_URL/api/matching/search/region?keyword=서울"
curl -s -X GET "$BASE_URL/api/matching/search/region?keyword=서울" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 5. 품목별 유통업체 검색 (채소)
echo "5. 품목별 유통업체 검색 (채소)"
echo "GET $BASE_URL/api/matching/search/product?keyword=채소"
curl -s -X GET "$BASE_URL/api/matching/search/product?keyword=채소" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

echo "=========================================="
echo "테스트 완료"
echo "=========================================="
