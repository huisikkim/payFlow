#!/bin/bash

# 간단한 식자재 발주 테스트
BASE_URL="http://localhost:8080"

echo "🚀 간단 발주 테스트"

# 발주 생성
echo "📦 발주 생성..."
curl -X POST "$BASE_URL/api/ingredient-orders" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": "STORE_TEST",
    "distributorId": "DIST_TEST",
    "items": [
      {
        "itemName": "테스트 품목",
        "quantity": 5,
        "unitPrice": 10000,
        "unit": "개"
      }
    ]
  }' | jq '.'
