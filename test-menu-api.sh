#!/bin/bash

echo "🍽️ 메뉴 원가 계산 + 마진 시뮬레이터 API 테스트"
echo "================================================"
echo ""

BASE_URL="http://localhost:8080"

echo "1️⃣ 전체 메뉴 조회"
echo "GET $BASE_URL/api/menu"
curl -s "$BASE_URL/api/menu" | jq '.'
echo ""
echo ""

echo "2️⃣ 매장별 메뉴 조회 (STORE-001)"
echo "GET $BASE_URL/api/menu/store/STORE-001"
curl -s "$BASE_URL/api/menu/store/STORE-001" | jq '.'
echo ""
echo ""

echo "3️⃣ 메뉴 상세 조회 (김치찌개)"
echo "GET $BASE_URL/api/menu/1"
curl -s "$BASE_URL/api/menu/1" | jq '.'
echo ""
echo ""

echo "4️⃣ 메뉴 원가 계산 (김치찌개)"
echo "GET $BASE_URL/api/menu/1/cost"
curl -s "$BASE_URL/api/menu/1/cost" | jq '.'
echo ""
echo ""

echo "5️⃣ 매장 전체 메뉴 원가 분석"
echo "GET $BASE_URL/api/menu/store/STORE-001/costs"
curl -s "$BASE_URL/api/menu/store/STORE-001/costs" | jq '.'
echo ""
echo ""

echo "6️⃣ 가격 기반 마진 시뮬레이션 (김치찌개 → 10,000원)"
echo "GET $BASE_URL/api/menu/1/simulate/price?targetPrice=10000"
curl -s "$BASE_URL/api/menu/1/simulate/price?targetPrice=10000" | jq '.'
echo ""
echo ""

echo "7️⃣ 마진율 기반 시뮬레이션 (김치찌개 → 35% 마진)"
echo "GET $BASE_URL/api/menu/1/simulate/margin?targetMargin=35"
curl -s "$BASE_URL/api/menu/1/simulate/margin?targetMargin=35" | jq '.'
echo ""
echo ""

echo "8️⃣ 새 메뉴 생성 (제육볶음)"
echo "POST $BASE_URL/api/menu"
NEW_MENU=$(cat <<EOF
{
  "name": "제육볶음",
  "description": "매콤달콤한 돼지고기 볶음",
  "category": "한식",
  "storeId": "STORE-001",
  "sellingPrice": 9000,
  "recipeIngredients": [
    {
      "ingredientName": "돼지고기",
      "quantity": 0.2,
      "unit": "kg",
      "notes": "앞다리살"
    },
    {
      "ingredientName": "양파",
      "quantity": 0.1,
      "unit": "kg",
      "notes": ""
    },
    {
      "ingredientName": "대파",
      "quantity": 0.05,
      "unit": "kg",
      "notes": ""
    },
    {
      "ingredientName": "고춧가루",
      "quantity": 0.02,
      "unit": "kg",
      "notes": ""
    }
  ]
}
EOF
)

CREATED_MENU=$(curl -s -X POST "$BASE_URL/api/menu" \
  -H "Content-Type: application/json" \
  -d "$NEW_MENU")

echo "$CREATED_MENU" | jq '.'
MENU_ID=$(echo "$CREATED_MENU" | jq -r '.id')
echo ""
echo ""

echo "9️⃣ 생성된 메뉴 원가 계산 (제육볶음)"
echo "GET $BASE_URL/api/menu/$MENU_ID/cost"
curl -s "$BASE_URL/api/menu/$MENU_ID/cost" | jq '.'
echo ""
echo ""

echo "🎯 웹 UI 접속 URL:"
echo "  - 메뉴 목록: $BASE_URL/menu"
echo "  - 메뉴 상세: $BASE_URL/menu/1"
echo "  - 마진 시뮬레이터: $BASE_URL/menu/1/simulator"
echo "  - 메뉴 추가: $BASE_URL/menu/create"
echo ""
echo "✅ 테스트 완료!"
