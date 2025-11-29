#!/bin/bash

BASE_URL="http://localhost:8080/api/group-buying"

echo "========================================="
echo "공동구매 시스템 간단 테스트"
echo "========================================="
echo ""

# 1. 방 생성
echo "1. 공동구매 방 생성..."
ROOM_ID=$(curl -s -X POST "${BASE_URL}/rooms" \
  -H "Content-Type: application/json" \
  -d '{"roomTitle":"🔥 쌀 공동구매 20% 할인","distributorId":"DIST001","distributorName":"신선식품","productId":1,"discountRate":20,"availableStock":500,"targetQuantity":300,"minOrderPerStore":10,"minParticipants":5,"region":"서울","deliveryFee":50000,"deliveryFeeType":"SHARED","durationHours":24}' | jq -r '.roomId')
echo "✅ 방 생성: $ROOM_ID"
echo ""

# 2. 방 오픈
echo "2. 방 오픈..."
curl -s -X POST "${BASE_URL}/rooms/${ROOM_ID}/open?distributorId=DIST001" | jq '{roomId, status, targetQuantity, currentQuantity}'
echo ""

# 3. 오픈 중인 방 목록
echo "3. 오픈 중인 방 목록..."
curl -s "${BASE_URL}/rooms/open" | jq 'length'
echo ""

# 4. 방 상세 조회
echo "4. 방 상세 조회..."
curl -s "${BASE_URL}/rooms/${ROOM_ID}" | jq '{roomId, roomTitle, status, originalPrice, discountedPrice, savingsPerUnit, achievementRate, currentParticipants, deliveryFeePerStore}'
echo ""

echo "========================================="
echo "테스트 완료!"
echo "방 ID: $ROOM_ID"
echo "========================================="
