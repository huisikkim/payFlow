#!/bin/bash

# 식자재 발주 E2E 테스트 스크립트
BASE_URL="http://localhost:8080"

echo "🚀 식자재 발주 플랫폼 E2E 테스트 시작"
echo "=========================================="

# 1. 매장 발주 생성
echo ""
echo "📦 Step 1: 매장 발주 생성"
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/ingredient-orders" \
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

ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.orderId')
echo "✅ 발주 생성 완료: $ORDER_ID"
echo "   총액: $(echo $ORDER_RESPONSE | jq -r '.totalAmount')원"

sleep 2

# 2. 발주 조회
echo ""
echo "🔍 Step 2: 발주 조회"
curl -s -X GET "$BASE_URL/api/ingredient-orders/$ORDER_ID" | jq '.'

sleep 2

# 3. 유통사 대기 중인 발주 목록 조회
echo ""
echo "📋 Step 3: 유통사 대기 중인 발주 목록"
curl -s -X GET "$BASE_URL/api/distributor/orders/pending?distributorId=DIST_001" | jq '.'

sleep 2

# 4. 유통사 단가 수정 (선택사항)
echo ""
echo "💰 Step 4: 품목 단가 수정 (양파 가격 변경)"
ITEM_ID=$(curl -s -X GET "$BASE_URL/api/ingredient-orders/$ORDER_ID" | jq -r '.items[0].itemId // 1')
curl -s -X PUT "$BASE_URL/api/distributor/orders/$ORDER_ID/items/$ITEM_ID/price" \
  -H "Content-Type: application/json" \
  -d '{
    "newPrice": 5500
  }'
echo "✅ 단가 수정 완료"

sleep 2

# 5. 유통사 발주 확인
echo ""
echo "✅ Step 5: 유통사 발주 확인"
curl -s -X POST "$BASE_URL/api/distributor/orders/$ORDER_ID/confirm"
echo "✅ 발주 확인 완료"

sleep 3

# 6. 명세서 업로드 (CSV 파일 생성 및 업로드)
echo ""
echo "📄 Step 6: 명세서 업로드"

# 샘플 CSV 파일 생성
cat > /tmp/invoice_sample.csv << EOF
품목명,수량,단가,단위
양파,10,5500,kg
감자,20,3000,kg
당근,15,4000,kg
EOF

INVOICE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/invoices/upload" \
  -F "orderId=$ORDER_ID" \
  -F "file=@/tmp/invoice_sample.csv")

INVOICE_ID=$(echo $INVOICE_RESPONSE | jq -r '.invoiceId')
echo "✅ 명세서 업로드 완료: $INVOICE_ID"

sleep 5

# 7. 명세서 파싱 결과 확인
echo ""
echo "🔍 Step 7: 명세서 파싱 결과 확인"
curl -s -X GET "$BASE_URL/api/invoices/$INVOICE_ID" | jq '.'

sleep 2

# 8. 정산 내역 조회
echo ""
echo "💰 Step 8: 매장 정산 내역 조회"
SETTLEMENT_RESPONSE=$(curl -s -X GET "$BASE_URL/api/settlements/store/STORE_001")
echo $SETTLEMENT_RESPONSE | jq '.'

SETTLEMENT_ID=$(echo $SETTLEMENT_RESPONSE | jq -r '.[0].settlementId')

sleep 2

# 9. 정산 완료 (일부 금액 지불)
echo ""
echo "💳 Step 9: 정산 완료 처리"
curl -s -X POST "$BASE_URL/api/settlements/$SETTLEMENT_ID/complete" \
  -H "Content-Type: application/json" \
  -d '{
    "paidAmount": 100000
  }'
echo "✅ 정산 완료"

sleep 2

# 10. 미수금 확인
echo ""
echo "📊 Step 10: 매장 총 미수금 확인"
curl -s -X GET "$BASE_URL/api/settlements/store/STORE_001/outstanding" | jq '.'

echo ""
echo "=========================================="
echo "🎉 E2E 테스트 완료!"
echo ""
echo "📝 테스트 요약:"
echo "  - 발주 ID: $ORDER_ID"
echo "  - 명세서 ID: $INVOICE_ID"
echo "  - 정산 ID: $SETTLEMENT_ID"
echo ""
echo "🔍 Kafka 이벤트 로그를 확인하세요!"
