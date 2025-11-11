#!/bin/bash

echo "======================================"
echo "토탈소싱기 API 테스트"
echo "======================================"
echo ""

BASE_URL="http://localhost:8080"

# 1. 키워드 분석 테스트 (전체 플랫폼)
echo "1. 키워드 분석 테스트 - 무선이어폰 (전체 플랫폼)"
curl -X POST "${BASE_URL}/api/sourcing/analyze" \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "무선이어폰",
    "platform": "ALL"
  }' | jq '.'

echo ""
echo "======================================"
echo ""

# 2. 키워드 분석 테스트 (네이버만)
echo "2. 키워드 분석 테스트 - 텀블러 (네이버)"
curl -X POST "${BASE_URL}/api/sourcing/analyze" \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "텀블러",
    "platform": "NAVER"
  }' | jq '.'

echo ""
echo "======================================"
echo ""

# 3. 키워드 분석 테스트 (쿠팡만)
echo "3. 키워드 분석 테스트 - 노트북 거치대 (쿠팡)"
curl -X POST "${BASE_URL}/api/sourcing/analyze" \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "노트북 거치대",
    "platform": "COUPANG"
  }' | jq '.'

echo ""
echo "======================================"
echo ""

# 4. 블루오션 키워드 조회
echo "4. 블루오션 키워드 조회"
curl -X GET "${BASE_URL}/api/sourcing/blue-ocean" | jq '.'

echo ""
echo "======================================"
echo ""

# 5. 최근 분석 결과 조회
echo "5. 최근 7일 분석 결과 조회"
curl -X GET "${BASE_URL}/api/sourcing/recent?days=7" | jq '.'

echo ""
echo "======================================"
echo "테스트 완료!"
echo "웹 UI: ${BASE_URL}/sourcing"
echo "======================================"
