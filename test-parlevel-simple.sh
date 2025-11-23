#!/bin/bash

BASE_URL="http://localhost:8080"
STORE_ID="STORE_001"

echo "=========================================="
echo "🎯 Dynamic Par Level 간단 테스트"
echo "=========================================="
echo ""

# 1. Par Level 생성
echo "1️⃣ Par Level 생성 (양파)"
curl -s -X POST "$BASE_URL/api/parlevel/settings" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": "STORE_001",
    "itemName": "양파",
    "unit": "kg",
    "minLevel": 50,
    "maxLevel": 150,
    "safetyStock": 30,
    "leadTimeDays": 2,
    "autoOrderEnabled": true
  }' | jq '.'
echo ""
echo ""

# 2. Par Level 조회
echo "2️⃣ Par Level 조회"
curl -s -X GET "$BASE_URL/api/parlevel/settings/$STORE_ID" | jq '.'
echo ""
echo ""

# 3. 발주 예측 생성
echo "3️⃣ 발주 예측 생성"
curl -s -X POST "$BASE_URL/api/parlevel/predictions/$STORE_ID/generate" | jq '.'
echo ""
echo ""

# 4. 대기 중인 예측 조회
echo "4️⃣ 대기 중인 예측 조회"
curl -s -X GET "$BASE_URL/api/parlevel/predictions/$STORE_ID/pending" | jq '.'
echo ""
echo ""

echo "=========================================="
echo "✅ 테스트 완료!"
echo "=========================================="
echo ""
echo "📊 대시보드: $BASE_URL/parlevel/dashboard"
echo ""
