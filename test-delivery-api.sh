#!/bin/bash

echo "=========================================="
echo "배송 관리 API 테스트"
echo "=========================================="
echo ""

BASE_URL="http://localhost:8080"

# 색상 코드
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}1. 매장 사용자 로그인${NC}"
STORE_LOGIN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "store001",
    "password": "password"
  }')

STORE_TOKEN=$(echo $STORE_LOGIN | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$STORE_TOKEN" ]; then
    echo -e "${RED}매장 로그인 실패${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 매장 로그인 성공${NC}"
echo ""

echo -e "${BLUE}2. 유통업자 로그인${NC}"
DIST_LOGIN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "dist001",
    "password": "password"
  }')

DIST_TOKEN=$(echo $DIST_LOGIN | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$DIST_TOKEN" ]; then
    echo -e "${RED}유통업자 로그인 실패${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 유통업자 로그인 성공${NC}"
echo ""

echo -e "${BLUE}3. 주문 생성 (매장)${NC}"
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/catalog-orders/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $STORE_TOKEN" \
  -d '{
    "distributorId": "dist001",
    "deliveryAddress": "서울시 강남구 테헤란로 123",
    "deliveryPhone": "010-1234-5678",
    "deliveryRequest": "문 앞에 놓아주세요",
    "desiredDeliveryDate": "2025-11-30T10:00:00"
  }')

ORDER_ID=$(echo $ORDER_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
ORDER_NUMBER=$(echo $ORDER_RESPONSE | grep -o '"orderNumber":"[^"]*' | cut -d'"' -f4)

echo "주문 ID: $ORDER_ID"
echo "주문 번호: $ORDER_NUMBER"
echo -e "${GREEN}✓ 주문 생성 완료${NC}"
echo ""

echo -e "${BLUE}4. 주문 확정 (매장)${NC}"
curl -s -X POST "$BASE_URL/api/catalog-orders/$ORDER_ID/confirm" \
  -H "Authorization: Bearer $STORE_TOKEN" > /dev/null

echo -e "${GREEN}✓ 주문 확정 완료 (상태: CONFIRMED)${NC}"
echo ""

echo -e "${BLUE}5. 배송 정보 생성 (유통업자)${NC}"
DELIVERY_RESPONSE=$(curl -s -X POST "$BASE_URL/api/deliveries/order/$ORDER_ID" \
  -H "Authorization: Bearer $DIST_TOKEN")

echo "$DELIVERY_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$DELIVERY_RESPONSE"
echo -e "${GREEN}✓ 배송 정보 생성 완료 (상태: PREPARING)${NC}"
echo ""

echo -e "${BLUE}6. 배송 시작 (유통업자)${NC}"
SHIPPING_RESPONSE=$(curl -s -X POST "$BASE_URL/api/deliveries/order/$ORDER_ID/ship" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $DIST_TOKEN" \
  -d '{
    "trackingNumber": "1234567890",
    "courierCompany": "CJ대한통운",
    "courierPhone": "1588-1255",
    "estimatedDeliveryDate": "2025-11-30T18:00:00",
    "deliveryNotes": "신선식품 주의"
  }')

echo "$SHIPPING_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$SHIPPING_RESPONSE"
echo -e "${GREEN}✓ 배송 시작 완료 (상태: SHIPPED)${NC}"
echo ""

echo -e "${BLUE}7. 배송 정보 조회 (매장)${NC}"
DELIVERY_INFO=$(curl -s -X GET "$BASE_URL/api/deliveries/order/$ORDER_ID" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$DELIVERY_INFO" | python3 -m json.tool 2>/dev/null || echo "$DELIVERY_INFO"
echo -e "${GREEN}✓ 배송 정보 조회 완료${NC}"
echo ""

echo -e "${BLUE}8. 유통업자 배송 목록 조회${NC}"
DIST_DELIVERIES=$(curl -s -X GET "$BASE_URL/api/deliveries/distributor" \
  -H "Authorization: Bearer $DIST_TOKEN")

echo "$DIST_DELIVERIES" | python3 -m json.tool 2>/dev/null || echo "$DIST_DELIVERIES"
echo -e "${GREEN}✓ 유통업자 배송 목록 조회 완료${NC}"
echo ""

echo -e "${BLUE}9. 매장 배송 목록 조회${NC}"
STORE_DELIVERIES=$(curl -s -X GET "$BASE_URL/api/deliveries/store" \
  -H "Authorization: Bearer $STORE_TOKEN")

echo "$STORE_DELIVERIES" | python3 -m json.tool 2>/dev/null || echo "$STORE_DELIVERIES"
echo -e "${GREEN}✓ 매장 배송 목록 조회 완료${NC}"
echo ""

echo -e "${BLUE}10. 배송 완료 (유통업자)${NC}"
COMPLETE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/deliveries/order/$ORDER_ID/complete" \
  -H "Authorization: Bearer $DIST_TOKEN")

echo "$COMPLETE_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$COMPLETE_RESPONSE"
echo -e "${GREEN}✓ 배송 완료 처리 (상태: DELIVERED)${NC}"
echo ""

echo "=========================================="
echo -e "${GREEN}배송 관리 API 테스트 완료!${NC}"
echo "=========================================="
echo ""
echo "웹 UI 접속:"
echo "- 유통업자 배송 관리: $BASE_URL/deliveries/distributor"
echo "- 매장 배송 조회: $BASE_URL/deliveries/store"
