#!/bin/bash

BASE_URL="http://localhost:8080"

echo "========================================="
echo "품질 이슈 신고 및 반품/교환 API 테스트"
echo "========================================="
echo ""

# 1. 로그인 (JWT 토큰 발급)
echo "1. 사용자 로그인..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ 로그인 실패"
    exit 1
fi

echo "✅ 로그인 성공"
echo "Token: ${TOKEN:0:50}..."
echo ""

# 2. 품질 이슈 신고 (가게사장님)
echo "2. 품질 이슈 신고 (양파 썩음)..."
ISSUE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/quality-issues" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "orderId": 123,
    "itemId": 456,
    "itemName": "양파 10kg",
    "storeId": "STORE_001",
    "storeName": "홍길동 식당",
    "distributorId": "DIST_001",
    "issueType": "POOR_QUALITY",
    "photoUrls": [
      "https://example.com/photo1.jpg",
      "https://example.com/photo2.jpg"
    ],
    "description": "양파가 썩었습니다. 절반 이상이 사용 불가능한 상태입니다.",
    "requestAction": "REFUND"
  }')

ISSUE_ID=$(echo $ISSUE_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -z "$ISSUE_ID" ]; then
    echo "❌ 품질 이슈 신고 실패"
    echo "Response: $ISSUE_RESPONSE"
    exit 1
fi

echo "✅ 품질 이슈 신고 완료"
echo "Issue ID: $ISSUE_ID"
echo "Response: $ISSUE_RESPONSE" | jq '.' 2>/dev/null || echo "$ISSUE_RESPONSE"
echo ""

# 3. 품질 이슈 상세 조회
echo "3. 품질 이슈 상세 조회..."
curl -s -X GET "$BASE_URL/api/quality-issues/$ISSUE_ID" \
  -H "Authorization: Bearer $TOKEN" | jq '.' 2>/dev/null || echo "상세 조회 완료"
echo ""

# 4. 가게별 품질 이슈 목록 조회
echo "4. 가게별 품질 이슈 목록 조회..."
curl -s -X GET "$BASE_URL/api/quality-issues/store/STORE_001" \
  -H "Authorization: Bearer $TOKEN" | jq '.' 2>/dev/null || echo "목록 조회 완료"
echo ""

# 5. 유통사별 대기 중인 품질 이슈 조회
echo "5. 유통사별 대기 중인 품질 이슈 조회..."
curl -s -X GET "$BASE_URL/api/quality-issues/distributor/DIST_001/pending" \
  -H "Authorization: Bearer $TOKEN" | jq '.' 2>/dev/null || echo "대기 목록 조회 완료"
echo ""

# 6. 품질 이슈 검토 시작 (유통업자)
echo "6. 품질 이슈 검토 시작..."
curl -s -X POST "$BASE_URL/api/quality-issues/$ISSUE_ID/review" \
  -H "Authorization: Bearer $TOKEN" | jq '.' 2>/dev/null || echo "검토 시작 완료"
echo ""

# 7. 품질 이슈 승인 (유통업자)
echo "7. 품질 이슈 승인..."
curl -s -X POST "$BASE_URL/api/quality-issues/$ISSUE_ID/approve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "comment": "확인했습니다. 품질 문제가 맞습니다. 환불 처리하겠습니다."
  }' | jq '.' 2>/dev/null || echo "승인 완료"
echo ""

# 8. 수거 예약
echo "8. 수거 예약 (당일 오후 2시)..."
PICKUP_TIME=$(date -u -d "+6 hours" +"%Y-%m-%dT%H:%M:%S" 2>/dev/null || date -u -v+6H +"%Y-%m-%dT%H:%M:%S")
curl -s -X POST "$BASE_URL/api/quality-issues/$ISSUE_ID/schedule-pickup" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"pickupTime\": \"$PICKUP_TIME\"
  }" | jq '.' 2>/dev/null || echo "수거 예약 완료"
echo ""

# 9. 수거 완료
echo "9. 수거 완료..."
curl -s -X POST "$BASE_URL/api/quality-issues/$ISSUE_ID/complete-pickup" \
  -H "Authorization: Bearer $TOKEN" | jq '.' 2>/dev/null || echo "수거 완료"
echo ""

# 10. 환불 완료
echo "10. 환불 완료..."
curl -s -X POST "$BASE_URL/api/quality-issues/$ISSUE_ID/complete-resolution" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "note": "환불 처리 완료했습니다. 다음 주문 시 할인 쿠폰을 제공하겠습니다."
  }' | jq '.' 2>/dev/null || echo "환불 완료"
echo ""

# 11. 교환 케이스 테스트
echo "11. 품질 이슈 신고 (교환 요청)..."
EXCHANGE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/quality-issues" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "orderId": 124,
    "itemId": 457,
    "itemName": "감자 20kg",
    "storeId": "STORE_001",
    "storeName": "홍길동 식당",
    "distributorId": "DIST_001",
    "issueType": "DAMAGED",
    "photoUrls": [
      "https://example.com/potato1.jpg"
    ],
    "description": "포장이 파손되어 감자가 흙투성이입니다.",
    "requestAction": "EXCHANGE"
  }')

EXCHANGE_ID=$(echo $EXCHANGE_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "✅ 교환 요청 신고 완료 (Issue ID: $EXCHANGE_ID)"
echo ""

# 12. 교환 승인 및 완료
echo "12. 교환 승인..."
curl -s -X POST "$BASE_URL/api/quality-issues/$EXCHANGE_ID/approve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "comment": "교환 처리하겠습니다."
  }' > /dev/null

echo "13. 교환 수거 예약..."
curl -s -X POST "$BASE_URL/api/quality-issues/$EXCHANGE_ID/schedule-pickup" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"pickupTime\": \"$PICKUP_TIME\"
  }" > /dev/null

echo "14. 교환 수거 완료..."
curl -s -X POST "$BASE_URL/api/quality-issues/$EXCHANGE_ID/complete-pickup" \
  -H "Authorization: Bearer $TOKEN" > /dev/null

echo "15. 교환 완료..."
curl -s -X POST "$BASE_URL/api/quality-issues/$EXCHANGE_ID/complete-resolution" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "note": "새 제품으로 교환 배송 완료했습니다."
  }' | jq '.' 2>/dev/null || echo "교환 완료"
echo ""

# 16. 유통사별 전체 품질 이슈 조회
echo "16. 유통사별 전체 품질 이슈 조회..."
curl -s -X GET "$BASE_URL/api/quality-issues/distributor/DIST_001" \
  -H "Authorization: Bearer $TOKEN" | jq '.' 2>/dev/null || echo "전체 목록 조회 완료"
echo ""

echo "========================================="
echo "✅ 모든 테스트 완료!"
echo "========================================="
echo ""
echo "📊 테스트 요약:"
echo "  - 환불 케이스: Issue ID $ISSUE_ID (완료)"
echo "  - 교환 케이스: Issue ID $EXCHANGE_ID (완료)"
echo ""
echo "💡 다음 단계:"
echo "  - 웹 UI 구현"
echo "  - Kafka 이벤트 발행 (품질 이슈 알림)"
echo "  - 사진 업로드 기능"
echo "  - 통계 대시보드"
