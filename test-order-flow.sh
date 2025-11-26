#!/bin/bash

BASE_URL="http://localhost:8080"
STORE_ID="test_store"
DISTRIBUTOR_ID="distributor1"

echo "=========================================="
echo "식자재 주문 전체 흐름 테스트"
echo "=========================================="
echo ""

# 1. 카탈로그 조회
echo "1️⃣  카탈로그 조회"
echo "GET $BASE_URL/api/catalog/distributor/$DISTRIBUTOR_ID"
curl -s -X GET "$BASE_URL/api/catalog/distributor/$DISTRIBUTOR_ID" \
  -H "X-Store-Id: $STORE_ID" | jq '.[0:2]'
echo ""
echo ""

# 2. 상품 상세 조회
echo "2️⃣  상품 상세 조회 (상품 ID: 1)"
echo "GET $BASE_URL/api/catalog/products/1/detail"
curl -s -X GET "$BASE_URL/api/catalog/products/1/detail" \
  -H "X-Store-Id: $STORE_ID" | jq '.'
echo ""
echo ""

# 3. 장바구니에 상품 추가
echo "3️⃣  장바구니에 상품 추가"
echo "POST $BASE_URL/api/cart/add"
curl -s -X POST "$BASE_URL/api/cart/add" \
  -H "X-Store-Id: $STORE_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 10
  }' | jq '.'
echo ""
echo ""

# 4. 장바구니에 다른 상품 추가
echo "4️⃣  장바구니에 다른 상품 추가"
curl -s -X POST "$BASE_URL/api/cart/add" \
  -H "X-Store-Id: $STORE_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 2,
    "quantity": 5
  }' | jq '.'
echo ""
echo ""

# 5. 장바구니 조회
echo "5️⃣  장바구니 조회"
echo "GET $BASE_URL/api/cart/$DISTRIBUTOR_ID"
curl -s -X GET "$BASE_URL/api/cart/$DISTRIBUTOR_ID" \
  -H "X-Store-Id: $STORE_ID" | jq '.'
echo ""
echo ""

# 6. 주문 생성
echo "6️⃣  주문 생성"
echo "POST $BASE_URL/api/catalog-orders/create"
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/catalog-orders/create" \
  -H "X-Store-Id: $STORE_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "distributorId": "'"$DISTRIBUTOR_ID"'",
    "deliveryAddress": "서울시 강남구 테헤란로 123",
    "deliveryPhone": "010-1234-5678",
    "deliveryRequest": "문 앞에 놓아주세요"
  }')

echo "$ORDER_RESPONSE" | jq '.'
ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.id')
echo ""
echo "✅ 주문 생성 완료! 주문 ID: $ORDER_ID"
echo ""
echo ""

# 7. 주문 상세 조회
echo "7️⃣  주문 상세 조회"
echo "GET $BASE_URL/api/catalog-orders/$ORDER_ID"
curl -s -X GET "$BASE_URL/api/catalog-orders/$ORDER_ID" \
  -H "X-Store-Id: $STORE_ID" | jq '.'
echo ""
echo ""

# 8. 내 주문 목록 조회
echo "8️⃣  내 주문 목록 조회"
echo "GET $BASE_URL/api/catalog-orders/my"
curl -s -X GET "$BASE_URL/api/catalog-orders/my" \
  -H "X-Store-Id: $STORE_ID" | jq '.'
echo ""
echo ""

# 9. 장바구니 확인 (비어있어야 함)
echo "9️⃣  장바구니 확인 (주문 후 비어있어야 함)"
echo "GET $BASE_URL/api/cart/$DISTRIBUTOR_ID"
curl -s -X GET "$BASE_URL/api/cart/$DISTRIBUTOR_ID" \
  -H "X-Store-Id: $STORE_ID" | jq '.'
echo ""
echo ""

echo "=========================================="
echo "✅ 전체 흐름 테스트 완료!"
echo "=========================================="
