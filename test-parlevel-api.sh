#!/bin/bash

BASE_URL="http://localhost:8080"
STORE_ID="STORE_001"
DISTRIBUTOR_ID="DIST_001"

echo "=========================================="
echo "🎯 Dynamic Par Level API 테스트"
echo "=========================================="
echo ""

# 1. Par Level 설정 조회
echo "1️⃣ Par Level 설정 조회"
curl -s -X GET "$BASE_URL/api/parlevel/settings/$STORE_ID" | jq '.'
echo ""
echo ""

# 2. 소비 패턴 통계
echo "2️⃣ 소비 패턴 통계 (양파)"
curl -s -X GET "$BASE_URL/api/parlevel/consumption/$STORE_ID/양파/statistics?days=30" | jq '.'
echo ""
echo ""

# 3. 소비 이력 조회
echo "3️⃣ 소비 이력 조회 (양파, 최근 7일)"
curl -s -X GET "$BASE_URL/api/parlevel/consumption/$STORE_ID/양파?days=7" | jq '.'
echo ""
echo ""

# 4. 발주 예측 생성
echo "4️⃣ 발주 예측 생성"
curl -s -X POST "$BASE_URL/api/parlevel/predictions/$STORE_ID/generate" | jq '.'
echo ""
echo ""

# 5. 대기 중인 예측 조회
echo "5️⃣ 대기 중인 예측 조회"
PREDICTIONS=$(curl -s -X GET "$BASE_URL/api/parlevel/predictions/$STORE_ID/pending")
echo "$PREDICTIONS" | jq '.'
echo ""
echo ""

# 6. 자동 발주 실행 (첫 번째 예측)
PREDICTION_ID=$(echo "$PREDICTIONS" | jq -r '.[0].id // empty')

if [ -n "$PREDICTION_ID" ]; then
    echo "6️⃣ 자동 발주 실행 (예측 ID: $PREDICTION_ID)"
    curl -s -X POST "$BASE_URL/api/parlevel/auto-order/$PREDICTION_ID?distributorId=$DISTRIBUTOR_ID" | jq '.'
    echo ""
    echo ""
else
    echo "6️⃣ 자동 발주할 예측이 없습니다."
    echo ""
fi

# 7. Par Level 자동 계산
echo "7️⃣ Par Level 자동 계산 (새 품목: 토마토)"
curl -s -X POST "$BASE_URL/api/parlevel/settings/auto-calculate?storeId=$STORE_ID&itemName=토마토&unit=kg&leadTimeDays=2" | jq '.'
echo ""
echo ""

# 8. 전체 예측 조회
echo "8️⃣ 전체 예측 조회 (최근 이력)"
curl -s -X GET "$BASE_URL/api/parlevel/predictions/$STORE_ID" | jq '.'
echo ""
echo ""

echo "=========================================="
echo "✅ 테스트 완료!"
echo "=========================================="
echo ""
echo "📊 대시보드: $BASE_URL/parlevel/dashboard"
echo ""
