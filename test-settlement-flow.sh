#!/bin/bash

# 정산 기능 테스트 스크립트

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "정산 기능 통합 테스트"
echo "=========================================="
echo ""

# 색상 코드
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. 카탈로그 주문 생성 및 결제
echo -e "${BLUE}[1단계] 카탈로그 주문 생성${NC}"
echo "장바구니에 상품 추가..."

# 장바구니에 상품 추가 (예시)
curl -X POST "${BASE_URL}/api/catalog/cart/store1/add" \
  -H "Content-Type: application/json" \
  -d '{
    "distributorId": "dist1",
    "productId": 1,
    "quantity": 10
  }' \
  -s | jq '.'

echo ""
echo "주문 생성..."

# 주문 생성
ORDER_RESPONSE=$(curl -X POST "${BASE_URL}/api/catalog/orders/store1" \
  -H "Content-Type: application/json" \
  -d '{
    "distributorId": "dist1",
    "deliveryAddress": "서울시 강남구",
    "deliveryPhone": "010-1234-5678",
    "deliveryRequest": "문 앞에 놓아주세요"
  }' \
  -s)

echo "$ORDER_RESPONSE" | jq '.'

ORDER_NUMBER=$(echo "$ORDER_RESPONSE" | jq -r '.orderNumber')
ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.id')
TOTAL_AMOUNT=$(echo "$ORDER_RESPONSE" | jq -r '.totalAmount')

echo ""
echo -e "${GREEN}✓ 주문 생성 완료: ${ORDER_NUMBER}${NC}"
echo "  - 주문 ID: ${ORDER_ID}"
echo "  - 금액: ${TOTAL_AMOUNT}원"
echo ""

# 2. 결제 승인 (주문 확정)
echo -e "${BLUE}[2단계] 결제 승인 (주문 확정)${NC}"
sleep 2

curl -X POST "${BASE_URL}/api/catalog/orders/${ORDER_ID}/confirm?storeId=store1" \
  -s | jq '.'

echo ""
echo -e "${GREEN}✓ 결제 승인 완료 (정산 자동 생성됨)${NC}"
echo ""

# 3. 정산 조회
echo -e "${BLUE}[3단계] 정산 조회${NC}"
sleep 2

echo "가게별 정산 목록:"
curl -X GET "${BASE_URL}/api/settlements/store/store1" \
  -s | jq '.'

echo ""
echo "유통업자별 정산 목록:"
curl -X GET "${BASE_URL}/api/settlements/distributor/dist1" \
  -s | jq '.'

echo ""

# 4. 일일 정산 조회
echo -e "${BLUE}[4단계] 일일 정산 조회${NC}"
sleep 1

TODAY=$(date +%Y-%m-%d)
THIRTY_DAYS_AGO=$(date -v-30d +%Y-%m-%d 2>/dev/null || date -d "30 days ago" +%Y-%m-%d)

echo "가게별 일일 정산 (최근 30일):"
curl -X GET "${BASE_URL}/api/daily-settlements/store/store1?startDate=${THIRTY_DAYS_AGO}&endDate=${TODAY}" \
  -s | jq '.'

echo ""
echo "유통업자별 일일 정산 (최근 30일):"
curl -X GET "${BASE_URL}/api/daily-settlements/distributor/dist1?startDate=${THIRTY_DAYS_AGO}&endDate=${TODAY}" \
  -s | jq '.'

echo ""

# 5. 정산 통계 조회
echo -e "${BLUE}[5단계] 정산 통계 조회${NC}"
sleep 1

FIRST_DAY_OF_MONTH=$(date +%Y-%m-01)

echo "가게별 정산 통계 (이번 달):"
curl -X GET "${BASE_URL}/api/daily-settlements/store/store1/statistics?startDate=${FIRST_DAY_OF_MONTH}&endDate=${TODAY}" \
  -s | jq '.'

echo ""
echo "유통업자별 정산 통계 (이번 달):"
curl -X GET "${BASE_URL}/api/daily-settlements/distributor/dist1/statistics?startDate=${FIRST_DAY_OF_MONTH}&endDate=${TODAY}" \
  -s | jq '.'

echo ""

# 6. 정산 완료 처리
echo -e "${BLUE}[6단계] 정산 완료 처리 (선택)${NC}"
echo "정산을 완료 처리하시겠습니까? (y/n)"
read -r CONFIRM

if [ "$CONFIRM" = "y" ]; then
  echo "정산 ID를 입력하세요:"
  read -r SETTLEMENT_ID
  
  echo "지불 금액을 입력하세요:"
  read -r PAID_AMOUNT
  
  curl -X POST "${BASE_URL}/api/settlements/${SETTLEMENT_ID}/complete" \
    -H "Content-Type: application/json" \
    -d "{\"paidAmount\": ${PAID_AMOUNT}}" \
    -s | jq '.'
  
  echo ""
  echo -e "${GREEN}✓ 정산 완료 처리됨${NC}"
fi

echo ""
echo "=========================================="
echo -e "${GREEN}테스트 완료!${NC}"
echo "=========================================="
echo ""
echo "추가 확인 사항:"
echo "1. Kafka 토픽 확인: CatalogOrderPaymentCompleted"
echo "2. 일일 정산 자동 집계 확인"
echo "3. 스케줄러 로그 확인 (매일 새벽 1시)"
echo ""
