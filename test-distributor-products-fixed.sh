#!/bin/bash

# 유통업체로 로그인
echo "=========================================="
echo "유통업체 상품 관리 API 테스트"
echo "=========================================="
echo ""

echo "1. 유통업체 로그인"
LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "distributor1",
    "password": "password123"
  }')

echo "$LOGIN_RESPONSE" | jq '.'
TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
USER_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.userId')
USERNAME=$(echo "$LOGIN_RESPONSE" | jq -r '.username')
echo ""

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo "로그인 실패! 토큰을 받지 못했습니다."
  exit 1
fi

echo "로그인 성공! USER_ID=$USER_ID, USERNAME=$USERNAME"
echo ""

# 상품 등록 (모든 필수 필드 포함)
echo "2. 상품 등록 (POST /api/products) - 모든 필수 필드 포함"
CREATE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "http://localhost:8080/api/products" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"신선한 사과 10kg\",
    \"description\": \"맛있는 사과입니다. 유통업체 직거래\",
    \"price\": 50000,
    \"category\": \"FOOD\",
    \"productCondition\": \"NEW\",
    \"sellerId\": $USER_ID,
    \"sellerName\": \"$USERNAME\",
    \"location\": \"서울시 송파구\",
    \"imageUrls\": [\"https://picsum.photos/400/400?random=100\"]
  }")

HTTP_STATUS=$(echo "$CREATE_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
RESPONSE_BODY=$(echo "$CREATE_RESPONSE" | sed '/HTTP_STATUS/d')

echo "HTTP Status: $HTTP_STATUS"
echo "$RESPONSE_BODY" | jq '.'
echo ""

if [ "$HTTP_STATUS" == "403" ]; then
  echo "❌ 403 Forbidden 에러 발생!"
  echo "유통업체 사장님이 상품을 등록할 권한이 없습니다."
  echo ""
elif [ "$HTTP_STATUS" == "201" ] || [ "$HTTP_STATUS" == "200" ]; then
  echo "✅ 상품 등록 성공!"
  PRODUCT_ID=$(echo "$RESPONSE_BODY" | jq -r '.id')
  echo "등록된 상품 ID: $PRODUCT_ID"
  echo ""
  
  # 내 상품 조회
  echo "3. 내 상품 조회 (GET /api/products/my)"
  curl -s -X GET "http://localhost:8080/api/products/my" \
    -H "Authorization: Bearer $TOKEN" | jq '.content[] | {id, title, sellerName, price}'
  echo ""
  
  # 상품 수정
  echo "4. 상품 수정 (PUT /api/products/$PRODUCT_ID)"
  UPDATE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X PUT "http://localhost:8080/api/products/$PRODUCT_ID" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"title\": \"신선한 사과 10kg (가격 인하)\",
      \"description\": \"맛있는 사과입니다. 유통업체 직거래. 가격 인하!\",
      \"price\": 45000,
      \"category\": \"FOOD\",
      \"productCondition\": \"NEW\",
      \"location\": \"서울시 송파구\",
      \"imageUrls\": [\"https://picsum.photos/400/400?random=100\"]
    }")
  
  UPDATE_HTTP_STATUS=$(echo "$UPDATE_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
  UPDATE_BODY=$(echo "$UPDATE_RESPONSE" | sed '/HTTP_STATUS/d')
  
  echo "HTTP Status: $UPDATE_HTTP_STATUS"
  echo "$UPDATE_BODY" | jq '.'
  echo ""
  
  if [ "$UPDATE_HTTP_STATUS" == "403" ]; then
    echo "❌ 403 Forbidden 에러 발생!"
    echo "유통업체 사장님이 상품을 수정할 권한이 없습니다."
  elif [ "$UPDATE_HTTP_STATUS" == "200" ]; then
    echo "✅ 상품 수정 성공!"
  fi
else
  echo "❌ 상품 등록 실패! HTTP Status: $HTTP_STATUS"
fi

echo ""
echo "=========================================="
echo "테스트 완료"
echo "=========================================="
