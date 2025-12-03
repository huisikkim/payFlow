#!/bin/bash

# YouTube API 테스트 스크립트

BASE_URL="http://localhost:8080/api/youtube"

echo "=========================================="
echo "YouTube Data API v3 테스트"
echo "=========================================="

# 1. 한국 인기 급상승 영상 조회
echo ""
echo "1. 한국 인기 급상승 영상 조회"
echo "GET $BASE_URL/popular"
echo "------------------------------------------"
curl -s "$BASE_URL/popular" | jq '.videos[:3]'

# 2. 특정 국가 인기 영상 조회 (미국, 5개)
echo ""
echo "2. 미국 인기 영상 조회 (5개)"
echo "GET $BASE_URL/popular/US?maxResults=5"
echo "------------------------------------------"
curl -s "$BASE_URL/popular/US?maxResults=5" | jq '.videos[:3]'

# 3. 조회수 기준 상위 영상
echo ""
echo "3. 조회수 기준 상위 5개 영상"
echo "GET $BASE_URL/top-viewed?regionCode=KR&topN=5"
echo "------------------------------------------"
curl -s "$BASE_URL/top-viewed?regionCode=KR&topN=5" | jq '.videos[] | {title, viewCount, channelTitle}'

# 4. 좋아요 기준 상위 영상
echo ""
echo "4. 좋아요 기준 상위 5개 영상"
echo "GET $BASE_URL/top-liked?regionCode=KR&topN=5"
echo "------------------------------------------"
curl -s "$BASE_URL/top-liked?regionCode=KR&topN=5" | jq '.videos[] | {title, likeCount, channelTitle}'

echo ""
echo "=========================================="
echo "테스트 완료"
echo "=========================================="
