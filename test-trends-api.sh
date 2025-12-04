#!/bin/bash

echo "=== Google Trends API 테스트 ==="
echo ""
echo "1. 트렌드 조회 테스트"
curl -s http://localhost:8080/api/youtube/trends | python3 -m json.tool | head -80
echo ""
echo "✅ 테스트 완료!"
echo ""
echo "브라우저에서 확인: http://localhost:8080/youtube/popular"
echo "트렌드 탭을 클릭하세요!"
