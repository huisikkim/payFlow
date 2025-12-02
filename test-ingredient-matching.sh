#!/bin/bash

# 재료 매칭 API 테스트 스크립트

BASE_URL="http://localhost:8080"
API_URL="${BASE_URL}/api/ingredients"

echo "=========================================="
echo "재료 매칭 API 테스트"
echo "=========================================="
echo ""

# 색상 코드
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. 단일 재료 매칭 테스트
echo -e "${BLUE}[1] 단일 재료 매칭 테스트${NC}"
echo "-------------------------------------------"

echo -e "${YELLOW}테스트 1-1: 정확한 매칭 (양파)${NC}"
curl -X POST "${API_URL}/match" \
  -H "Content-Type: application/json" \
  -d '{"ocrText": "양파"}' \
  -s | jq '.'
echo ""

echo -e "${YELLOW}테스트 1-2: 공백 포함 (양 파)${NC}"
curl -X POST "${API_URL}/match" \
  -H "Content-Type: application/json" \
  -d '{"ocrText": "양 파"}' \
  -s | jq '.'
echo ""

echo -e "${YELLOW}테스트 1-3: 괄호 포함 (파 (국산))${NC}"
curl -X POST "${API_URL}/match" \
  -H "Content-Type: application/json" \
  -d '{"ocrText": "파 (국산)"}' \
  -s | jq '.'
echo ""

echo -e "${YELLOW}테스트 1-4: 숫자+단위 포함 (양파3kg)${NC}"
curl -X POST "${API_URL}/match" \
  -H "Content-Type: application/json" \
  -d '{"ocrText": "양파3kg"}' \
  -s | jq '.'
echo ""

echo -e "${YELLOW}테스트 1-5: 복잡한 케이스 (닭 가슴살 2kg 냉장)${NC}"
curl -X POST "${API_URL}/match" \
  -H "Content-Type: application/json" \
  -d '{"ocrText": "닭 가슴살 2kg 냉장"}' \
  -s | jq '.'
echo ""

echo -e "${YELLOW}테스트 1-6: 유사도 매칭 (양과)${NC}"
curl -X POST "${API_URL}/match" \
  -H "Content-Type: application/json" \
  -d '{"ocrText": "양과"}' \
  -s | jq '.'
echo ""

# 2. 일괄 매칭 테스트
echo ""
echo -e "${BLUE}[2] 일괄 매칭 테스트${NC}"
echo "-------------------------------------------"

curl -X POST "${API_URL}/match/batch" \
  -H "Content-Type: application/json" \
  -d '{
    "ocrTexts": [
      "양파 10kg",
      "감 자 20kg",
      "당근(국산) 15kg",
      "대파 8단",
      "마늘 5kg",
      "닭가슴살 2kg",
      "돼지고기 3kg"
    ]
  }' \
  -s | jq '.'
echo ""

# 3. 표준 재료명 목록 조회
echo ""
echo -e "${BLUE}[3] 표준 재료명 목록 조회${NC}"
echo "-------------------------------------------"

curl -X GET "${API_URL}/standard-names" \
  -s | jq '.'
echo ""

# 4. 동의어 추가 테스트
echo ""
echo -e "${BLUE}[4] 동의어 추가 테스트${NC}"
echo "-------------------------------------------"

echo -e "${YELLOW}새 동의어 추가: 양파 -> 노란양파${NC}"
curl -X POST "${API_URL}/synonyms" \
  -H "Content-Type: application/json" \
  -d '{
    "standardName": "양파",
    "synonym": "노란양파",
    "similarityScore": 0.95
  }' \
  -s | jq '.'
echo ""

# 5. 특정 재료의 동의어 목록 조회
echo ""
echo -e "${BLUE}[5] 양파의 동의어 목록 조회${NC}"
echo "-------------------------------------------"

curl -X GET "${API_URL}/synonyms/양파" \
  -s | jq '.'
echo ""

# 6. 추가된 동의어로 매칭 테스트
echo ""
echo -e "${BLUE}[6] 추가된 동의어로 매칭 테스트${NC}"
echo "-------------------------------------------"

echo -e "${YELLOW}노란양파로 매칭 시도${NC}"
curl -X POST "${API_URL}/match" \
  -H "Content-Type: application/json" \
  -d '{"ocrText": "노란양파"}' \
  -s | jq '.'
echo ""

echo ""
echo -e "${GREEN}=========================================="
echo "테스트 완료!"
echo "==========================================${NC}"
