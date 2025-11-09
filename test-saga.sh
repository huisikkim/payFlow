#!/bin/bash

echo "======================================"
echo "🧪 Saga 패턴 테스트 스크립트"
echo "======================================"
echo ""

BASE_URL="http://localhost:8080"

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}1️⃣  Saga 성공 시나리오 테스트${NC}"
echo "   (주문 생성 → 결제 처리 → 재고 예약 → 완료)"
echo ""

SUCCESS_RESPONSE=$(curl -s -X POST "$BASE_URL/api/saga/test/success" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-TEST-001",
    "productName": "테스트 상품",
    "amount": 10000,
    "customerEmail": "test@example.com",
    "customerName": "홍길동"
  }')

echo "$SUCCESS_RESPONSE" | jq '.'
SAGA_ID=$(echo "$SUCCESS_RESPONSE" | jq -r '.sagaId')

if [ "$SAGA_ID" != "null" ]; then
    echo ""
    echo -e "${GREEN}✅ Saga 성공 시나리오 완료!${NC}"
    echo ""
    
    echo -e "${BLUE}📊 Saga 상태 조회${NC}"
    curl -s -X GET "$BASE_URL/api/saga/status/$SAGA_ID" | jq '.'
else
    echo -e "${RED}❌ Saga 성공 시나리오 실패${NC}"
fi

echo ""
echo "======================================"
echo ""

echo -e "${YELLOW}2️⃣  Saga 실패 시나리오 테스트 (보상 트랜잭션)${NC}"
echo "   (주문 생성 → 결제 처리 → 재고 예약 실패 → 보상 트랜잭션)"
echo ""

FAILURE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/saga/test/failure" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-TEST-003",
    "productName": "재고 부족 상품",
    "amount": 20000,
    "customerEmail": "test2@example.com",
    "customerName": "김철수"
  }')

echo "$FAILURE_RESPONSE" | jq '.'
SAGA_ID_FAIL=$(echo "$FAILURE_RESPONSE" | jq -r '.sagaId')

if [ "$SAGA_ID_FAIL" != "null" ]; then
    echo ""
    echo -e "${GREEN}✅ Saga 실패 시나리오 완료! (보상 트랜잭션 성공)${NC}"
    echo ""
    
    echo -e "${BLUE}📊 Saga 상태 조회${NC}"
    curl -s -X GET "$BASE_URL/api/saga/status/$SAGA_ID_FAIL" | jq '.'
else
    echo -e "${RED}❌ Saga 실패 시나리오 테스트 실패${NC}"
fi

echo ""
echo "======================================"
echo -e "${GREEN}🎉 모든 테스트 완료!${NC}"
echo "======================================"
