#!/bin/bash

# 공동구매 시스템 테스트 스크립트

BASE_URL="http://localhost:8080/api/group-buying"

echo "========================================="
echo "공동구매 시스템 테스트"
echo "========================================="
echo ""

# 1. 공동구매 방 생성 (유통업자)
echo "1. 공동구매 방 생성..."
CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/rooms" \
  -H "Content-Type: application/json" \
  -d '{
    "roomTitle": "🔥 김치 대박 세일! 20% 할인",
    "distributorId": "DIST001",
    "distributorName": "신선식품 유통",
    "productId": 1,
    "discountRate": 20.00,
    "availableStock": 500,
    "targetQuantity": 300,
    "minOrderPerStore": 10,
    "maxOrderPerStore": 100,
    "minParticipants": 5,
    "maxParticipants": 20,
    "region": "서울 강남구,서초구",
    "deliveryFee": 50000,
    "deliveryFeeType": "SHARED",
    "durationHours": 24,
    "description": "신선한 김치를 특가로 제공합니다!",
    "specialNote": "당일 배송 보장",
    "featured": true
  }')

ROOM_ID=$(echo "$CREATE_RESPONSE" | jq -r '.roomId')
echo "✅ 방 생성 완료: $ROOM_ID"
echo "$CREATE_RESPONSE" | jq '{roomId, roomTitle, status, discountRate, targetQuantity}'
echo ""

# 2. 방 오픈
echo "2. 방 오픈..."
curl -s -X POST "${BASE_URL}/rooms/${ROOM_ID}/open?distributorId=DIST001" | jq '.'
echo "✅ 방 오픈 완료"
echo ""

# 3. 오픈 중인 방 목록 조회
echo "3. 오픈 중인 방 목록 조회..."
curl -s -X GET "${BASE_URL}/rooms/open" | jq '.'
echo ""

# 4. 방 상세 조회
echo "4. 방 상세 조회..."
curl -s -X GET "${BASE_URL}/rooms/${ROOM_ID}" | jq '.'
echo ""

# 5. 공동구매 참여 (가게 1)
echo "5. 가게 1 참여..."
curl -s -X POST "${BASE_URL}/participants/join" \
  -H "Content-Type: application/json" \
  -d "{
    \"roomId\": \"${ROOM_ID}\",
    \"storeId\": \"STORE001\",
    \"quantity\": 30,
    \"deliveryAddress\": \"서울시 강남구 테헤란로 123\",
    \"deliveryPhone\": \"010-1234-5678\",
    \"deliveryRequest\": \"문 앞에 놓아주세요\"
  }" | jq '.'
echo "✅ 가게 1 참여 완료"
echo ""

# 6. 공동구매 참여 (가게 2)
echo "6. 가게 2 참여..."
curl -s -X POST "${BASE_URL}/participants/join" \
  -H "Content-Type: application/json" \
  -d "{
    \"roomId\": \"${ROOM_ID}\",
    \"storeId\": \"STORE002\",
    \"quantity\": 50,
    \"deliveryAddress\": \"서울시 서초구 강남대로 456\",
    \"deliveryPhone\": \"010-2345-6789\",
    \"deliveryRequest\": \"배송 전 연락주세요\"
  }" | jq '.'
echo "✅ 가게 2 참여 완료"
echo ""

# 7. 방 참여자 목록 조회
echo "7. 방 참여자 목록 조회..."
curl -s -X GET "${BASE_URL}/participants/room/${ROOM_ID}" | jq '.'
echo ""

# 8. 방 상세 조회 (업데이트된 정보)
echo "8. 방 상세 조회 (업데이트된 정보)..."
curl -s -X GET "${BASE_URL}/rooms/${ROOM_ID}" | jq '{
  roomId,
  roomTitle,
  productName,
  originalPrice,
  discountedPrice,
  targetQuantity,
  currentQuantity,
  achievementRate,
  currentParticipants,
  deliveryFeePerStore,
  status
}'
echo ""

# 9. 가게의 참여 내역 조회
echo "9. 가게 1의 참여 내역 조회..."
curl -s -X GET "${BASE_URL}/participants/store/STORE001" | jq '.'
echo ""

# 10. 추천 방 목록 조회
echo "10. 추천 방 목록 조회..."
curl -s -X GET "${BASE_URL}/rooms/featured" | jq '.'
echo ""

echo "========================================="
echo "테스트 완료!"
echo "========================================="
echo ""
echo "생성된 방 ID: $ROOM_ID"
echo ""
echo "다음 단계:"
echo "- 더 많은 가게 참여시키기"
echo "- 목표 달성 후 자동 주문 생성 확인"
echo "- 스케줄러 동작 확인 (5분 후)"
