#!/bin/bash

# 단가 학습 & 급등 경고 테스트 스크립트
BASE_URL="http://localhost:8080"

echo "🎯 단가 자동 학습 & 급등 경고 시스템 테스트"
echo "================================================"

# 1. 정상 단가로 첫 번째 발주 생성
echo ""
echo "📦 Step 1: 정상 단가로 발주 생성 (기준 데이터)"
ORDER1_RESPONSE=$(curl -s -X POST "$BASE_URL/api/ingredient-orders" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": "STORE_001",
    "distributorId": "DIST_001",
    "items": [
      {
        "itemName": "양파",
        "quantity": 10,
        "unitPrice": 5000,
        "unit": "kg"
      },
      {
        "itemName": "감자",
        "quantity": 20,
        "unitPrice": 3000,
        "unit": "kg"
      },
      {
        "itemName": "당근",
        "quantity": 15,
        "unitPrice": 4000,
        "unit": "kg"
      }
    ]
  }')

ORDER1_ID=$(echo $ORDER1_RESPONSE | jq -r '.orderId')
echo "✅ 발주 1 생성: $ORDER1_ID"
echo "   양파: 5,000원/kg, 감자: 3,000원/kg, 당근: 4,000원/kg"

sleep 3

# 2. 비슷한 단가로 두 번째 발주 (학습 데이터 축적)
echo ""
echo "📦 Step 2: 비슷한 단가로 발주 생성 (학습 데이터)"
ORDER2_RESPONSE=$(curl -s -X POST "$BASE_URL/api/ingredient-orders" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": "STORE_002",
    "distributorId": "DIST_001",
    "items": [
      {
        "itemName": "양파",
        "quantity": 15,
        "unitPrice": 5200,
        "unit": "kg"
      },
      {
        "itemName": "감자",
        "quantity": 25,
        "unitPrice": 2900,
        "unit": "kg"
      },
      {
        "itemName": "당근",
        "quantity": 20,
        "unitPrice": 4100,
        "unit": "kg"
      }
    ]
  }')

ORDER2_ID=$(echo $ORDER2_RESPONSE | jq -r '.orderId')
echo "✅ 발주 2 생성: $ORDER2_ID"
echo "   양파: 5,200원/kg, 감자: 2,900원/kg, 당근: 4,100원/kg"

sleep 3

# 3. 급등 단가로 세 번째 발주 (급등 경고 발생!)
echo ""
echo "🚨 Step 3: 급등 단가로 발주 생성 (경고 발생 예상)"
ORDER3_RESPONSE=$(curl -s -X POST "$BASE_URL/api/ingredient-orders" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": "STORE_003",
    "distributorId": "DIST_002",
    "items": [
      {
        "itemName": "양파",
        "quantity": 10,
        "unitPrice": 7500,
        "unit": "kg"
      },
      {
        "itemName": "감자",
        "quantity": 20,
        "unitPrice": 4500,
        "unit": "kg"
      },
      {
        "itemName": "당근",
        "quantity": 15,
        "unitPrice": 6000,
        "unit": "kg"
      }
    ]
  }')

ORDER3_ID=$(echo $ORDER3_RESPONSE | jq -r '.orderId')
echo "✅ 발주 3 생성: $ORDER3_ID"
echo "   양파: 7,500원/kg (+45% 급등!)"
echo "   감자: 4,500원/kg (+52% 급등!)"
echo "   당근: 6,000원/kg (+47% 급등!)"

sleep 5

# 4. 활성 경고 조회
echo ""
echo "🔍 Step 4: 활성 경고 조회"
ACTIVE_ALERTS=$(curl -s -X GET "$BASE_URL/api/price-learning/alerts/active")
echo "$ACTIVE_ALERTS" | jq '.'

ALERT_COUNT=$(echo "$ACTIVE_ALERTS" | jq 'length')
echo ""
echo "⚠️  활성 경고 수: $ALERT_COUNT개"

sleep 2

# 5. 양파 단가 통계 조회
echo ""
echo "📊 Step 5: 양파 단가 통계 조회"
curl -s -X GET "$BASE_URL/api/price-learning/items/%EC%96%91%ED%8C%8C/statistics?days=30" | jq '.'

sleep 2

# 6. 양파 추천 단가 조회
echo ""
echo "💡 Step 6: 양파 추천 단가 조회"
RECOMMENDED_PRICE=$(curl -s -X GET "$BASE_URL/api/price-learning/items/%EC%96%91%ED%8C%8C/recommended-price")
echo "추천 단가: ${RECOMMENDED_PRICE}원/kg"

sleep 2

# 7. 양파 단가 이력 조회
echo ""
echo "📋 Step 7: 양파 단가 이력 조회"
curl -s -X GET "$BASE_URL/api/price-learning/items/%EC%96%91%ED%8C%8C/history?days=30" | jq '.'

sleep 2

# 8. 최근 경고 목록 조회
echo ""
echo "📜 Step 8: 최근 경고 목록 조회"
curl -s -X GET "$BASE_URL/api/price-learning/alerts/recent" | jq '.'

sleep 2

# 9. 첫 번째 경고 확인 처리
if [ "$ALERT_COUNT" -gt 0 ]; then
    echo ""
    echo "✅ Step 9: 첫 번째 경고 확인 처리"
    FIRST_ALERT_ID=$(echo "$ACTIVE_ALERTS" | jq -r '.[0].alertId')
    curl -s -X POST "$BASE_URL/api/price-learning/alerts/$FIRST_ALERT_ID/acknowledge"
    echo ""
    echo "경고 확인 완료: $FIRST_ALERT_ID"
fi

sleep 2

# 10. 모든 품목 목록 조회
echo ""
echo "📦 Step 10: 모든 품목 목록 조회"
curl -s -X GET "$BASE_URL/api/price-learning/items" | jq '.'

echo ""
echo "================================================"
echo "🎉 테스트 완료!"
echo ""
echo "📝 테스트 요약:"
echo "  - 발주 1: $ORDER1_ID (정상 단가)"
echo "  - 발주 2: $ORDER2_ID (정상 단가)"
echo "  - 발주 3: $ORDER3_ID (급등 단가)"
echo "  - 활성 경고: $ALERT_COUNT개"
echo ""
echo "🌐 웹 대시보드:"
echo "  http://localhost:8080/ingredient/price-learning"
echo ""
echo "💡 Tip: 웹 대시보드에서 차트와 통계를 확인하세요!"
