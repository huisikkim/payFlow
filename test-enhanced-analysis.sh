#!/bin/bash

# YouTube 영상 분석 API 강화 버전 테스트 스크립트

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "YouTube 영상 분석 API - 강화 버전 테스트"
echo "=========================================="
echo ""

# 테스트할 YouTube Video ID (예시)
VIDEO_ID="dQw4w9WgXcQ"

echo "1. Video ID로 분석 테스트"
echo "GET ${BASE_URL}/api/youtube/analysis/${VIDEO_ID}"
echo ""

curl -X GET "${BASE_URL}/api/youtube/analysis/${VIDEO_ID}" \
  -H "Content-Type: application/json" \
  | jq '.'

echo ""
echo "=========================================="
echo ""

echo "2. URL로 분석 테스트"
echo "POST ${BASE_URL}/api/youtube/analysis/url"
echo ""

curl -X POST "${BASE_URL}/api/youtube/analysis/url" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://www.youtube.com/watch?v='${VIDEO_ID}'"
  }' \
  | jq '.'

echo ""
echo "=========================================="
echo ""

echo "3. 제목 분석 결과 확인"
echo ""

curl -X GET "${BASE_URL}/api/youtube/analysis/${VIDEO_ID}" \
  -H "Content-Type: application/json" \
  | jq '.report.titleAnalysis'

echo ""
echo "=========================================="
echo ""

echo "4. SEO 분석 결과 확인"
echo ""

curl -X GET "${BASE_URL}/api/youtube/analysis/${VIDEO_ID}" \
  -H "Content-Type: application/json" \
  | jq '.report.seoAnalysis'

echo ""
echo "=========================================="
echo ""

echo "5. 경쟁 영상 분석 결과 확인"
echo ""

curl -X GET "${BASE_URL}/api/youtube/analysis/${VIDEO_ID}" \
  -H "Content-Type: application/json" \
  | jq '.report.competitorAnalysis'

echo ""
echo "=========================================="
echo ""

echo "6. CTR 추정 결과 확인"
echo ""

curl -X GET "${BASE_URL}/api/youtube/analysis/${VIDEO_ID}" \
  -H "Content-Type: application/json" \
  | jq '.report.ctrEstimate'

echo ""
echo "=========================================="
echo ""

echo "7. 품질 점수 결과 확인"
echo ""

curl -X GET "${BASE_URL}/api/youtube/analysis/${VIDEO_ID}" \
  -H "Content-Type: application/json" \
  | jq '.report.qualityScore'

echo ""
echo "=========================================="
echo "테스트 완료"
echo "=========================================="
