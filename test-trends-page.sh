#!/bin/bash

echo "=== 구글 트렌드 페이지 테스트 ==="
echo ""

echo "1. 트렌드 페이지 접근 테스트"
PAGE_TITLE=$(curl -s http://localhost:8080/youtube/trends | grep -o "<title>.*</title>")
echo "페이지 제목: $PAGE_TITLE"
echo ""

echo "2. 트렌드 API 테스트"
curl -s http://localhost:8080/api/youtube/trends | python3 -c "import sys, json; data=json.load(sys.stdin); print(f'✅ API 정상: {data[\"count\"]}개 트렌드 조회 성공')"
echo ""

echo "3. 트렌드 데이터 샘플"
curl -s http://localhost:8080/api/youtube/trends | python3 -m json.tool | head -30
echo ""

echo "✅ 테스트 완료!"
echo ""
echo "브라우저에서 확인:"
echo "  - 트렌드 페이지: http://localhost:8080/youtube/trends"
echo "  - YouTube 인기: http://localhost:8080/youtube/popular"
echo ""
echo "헤더의 '구글 트렌드' 메뉴를 클릭하세요!"
