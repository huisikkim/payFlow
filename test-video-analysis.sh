#!/bin/bash

echo "=========================================="
echo "YouTube 영상 분석 API 테스트"
echo "=========================================="
echo ""

# 테스트할 YouTube 영상 URL (실제 존재하는 영상)
VIDEO_URL="https://www.youtube.com/watch?v=dQw4w9WgXcQ"
VIDEO_ID="dQw4w9WgXcQ"

echo "1️⃣ VideoId 추출 테스트"
echo "URL: $VIDEO_URL"
echo ""
curl -s "http://localhost:8080/api/youtube/analysis/extract?url=$VIDEO_URL" | jq '.'
echo ""
echo ""

echo "2️⃣ 영상 분석 테스트 (VideoId 직접 입력)"
echo "VideoId: $VIDEO_ID"
echo ""
curl -s "http://localhost:8080/api/youtube/analysis/$VIDEO_ID" | jq '.'
echo ""
echo ""

echo "3️⃣ 영상 분석 테스트 (URL로 분석)"
echo "URL: $VIDEO_URL"
echo ""
curl -s -X POST "http://localhost:8080/api/youtube/analysis/url" \
  -H "Content-Type: application/json" \
  -d "{\"url\": \"$VIDEO_URL\"}" | jq '.'
echo ""
echo ""

echo "=========================================="
echo "✅ 테스트 완료!"
echo "=========================================="
echo ""
echo "브라우저에서 확인:"
echo "  http://localhost:8080/youtube/analysis"
echo ""
