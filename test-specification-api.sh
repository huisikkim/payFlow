#!/bin/bash

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}명세표 API 테스트${NC}"
echo -e "${BLUE}========================================${NC}\n"

# 1. 명세표 목록 조회
echo -e "${YELLOW}1. 명세표 목록 조회${NC}"
curl -s -X GET "$BASE_URL/api/specifications" \
  -H "Content-Type: application/json" | jq '.' || echo "Failed"
echo -e "\n"

# 2. 테스트 이미지 생성 (간단한 PNG)
echo -e "${YELLOW}2. 테스트 이미지 생성${NC}"
mkdir -p test_images

# Python으로 간단한 테스트 이미지 생성
python3 << 'EOF'
from PIL import Image, ImageDraw, ImageFont
import os

# 이미지 생성
img = Image.new('RGB', (400, 300), color='white')
draw = ImageDraw.Draw(img)

# 텍스트 추가
text = """상품명: 테스트 명세표
카테고리: 전자제품
가격: 50000
수량: 10

명세:
- 크기: 100x100mm
- 무게: 500g
- 색상: 검정색
- 재질: 플라스틱"""

draw.text((20, 20), text, fill='black')

# 이미지 저장
os.makedirs('test_images', exist_ok=True)
img.save('test_images/test_spec.png')
print("Test image created: test_images/test_spec.png")
EOF

echo -e "\n"

# 3. 명세표 업로드
echo -e "${YELLOW}3. 명세표 업로드 및 처리${NC}"
if [ -f "test_images/test_spec.png" ]; then
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/specifications/upload" \
      -F "file=@test_images/test_spec.png")
    
    echo "$RESPONSE" | jq '.'
    
    # ID 추출
    SPEC_ID=$(echo "$RESPONSE" | jq -r '.id // empty')
    
    if [ ! -z "$SPEC_ID" ] && [ "$SPEC_ID" != "null" ]; then
        echo -e "${GREEN}✓ 업로드 성공 (ID: $SPEC_ID)${NC}\n"
        
        # 4. 명세표 상세 조회
        echo -e "${YELLOW}4. 명세표 상세 조회${NC}"
        curl -s -X GET "$BASE_URL/api/specifications/$SPEC_ID" \
          -H "Content-Type: application/json" | jq '.'
        echo -e "\n"
        
        # 5. 상태별 조회
        echo -e "${YELLOW}5. 파싱 완료된 명세표 조회${NC}"
        curl -s -X GET "$BASE_URL/api/specifications/status/PARSED" \
          -H "Content-Type: application/json" | jq '.'
        echo -e "\n"
        
        # 6. 검색
        echo -e "${YELLOW}6. 상품명으로 검색${NC}"
        curl -s -X GET "$BASE_URL/api/specifications/search?productName=테스트" \
          -H "Content-Type: application/json" | jq '.'
        echo -e "\n"
    else
        echo -e "${RED}✗ 업로드 실패${NC}\n"
    fi
else
    echo -e "${RED}✗ 테스트 이미지 생성 실패${NC}\n"
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}테스트 완료${NC}"
echo -e "${BLUE}========================================${NC}"
