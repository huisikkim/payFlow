#!/bin/bash

echo "=========================================="
echo "유통업체 카탈로그 API 테스트 (권한 수정 후)"
echo "=========================================="
echo ""

# 유통업체 로그인
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

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
  echo "❌ 로그인 실패!"
  exit 1
fi

# 카탈로그 상품 등록
echo "2. 카탈로그 상품 등록 (POST /api/catalog/products)"
CREATE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "http://localhost:8080/api/catalog/products" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "백설 쌀 20kg",
    "category": "쌀/곡물",
    "unitPrice": 45000,
    "unit": "포",
    "stockQuantity": 100,
    "minOrderQuantity": 1,
    "description": "국내산 백미 20kg",
    "origin": "국내산",
    "storageMethod": "상온보관"
  }')

HTTP_STATUS=$(echo "$CREATE_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
RESPONSE_BODY=$(echo "$CREATE_RESPONSE" | sed '/HTTP_STATUS/d')

echo "HTTP Status: $HTTP_STATUS"
echo "$RESPONSE_BODY" | jq '.'
echo ""

if [ "$HTTP_STATUS" == "403" ]; then
  echo "❌ 403 Forbidden 에러 발생!"
  echo "유통업체가 카탈로그 상품을 등록할 권한이 없습니다."
  echo ""
  exit 1
elif [ "$HTTP_STATUS" == "200" ]; then
  echo "✅ 카탈로그 상품 등록 성공!"
  PRODUCT_ID=$(echo "$RESPONSE_BODY" | jq -r '.id')
  echo "등록된 상품 ID: $PRODUCT_ID"
  echo ""
  
  # 내 카탈로그 조회
  echo "3. 내 카탈로그 조회 (GET /api/catalog/my-products)"
  MY_PRODUCTS_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "http://localhost:8080/api/catalog/my-products" \
    -H "Authorization: Bearer $TOKEN")
  
  MY_HTTP_STATUS=$(echo "$MY_PRODUCTS_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
  MY_BODY=$(echo "$MY_PRODUCTS_RESPONSE" | sed '/HTTP_STATUS/d')
  
  echo "HTTP Status: $MY_HTTP_STATUS"
  echo "$MY_BODY" | jq '.'
  echo ""
  
  if [ "$MY_HTTP_STATUS" == "403" ]; then
    echo "❌ 403 Forbidden 에러 발생!"
  elif [ "$MY_HTTP_STATUS" == "200" ]; then
    echo "✅ 내 카탈로그 조회 성공!"
  fi
else
  echo "❌ 상품 등록 실패! HTTP Status: $HTTP_STATUS"
fi

echo ""
echo "=========================================="
echo "테스트 완료"
echo "=========================================="
