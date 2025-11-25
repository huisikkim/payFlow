#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "유통업체 정보 API 테스트"
echo "=========================================="
echo ""

# 1. 유통업체 로그인
echo "1. 유통업체 로그인"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "distributor1",
    "password": "password123"
  }')

echo "$LOGIN_RESPONSE" | jq '.'
TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
echo ""

# 2. 유통업체 정보 등록
echo "2. 유통업체 정보 등록"
echo "POST $BASE_URL/api/distributor/info"
curl -s -X POST "$BASE_URL/api/distributor/info" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "distributorName": "신선식자재 유통",
    "supplyProducts": "쌀/곡물,채소,과일,육류,수산물",
    "serviceRegions": "서울,경기,인천",
    "deliveryAvailable": true,
    "deliveryInfo": "배송비 무료 (10만원 이상), 익일 배송",
    "description": "신선한 식자재를 공급하는 전문 유통업체입니다",
    "certifications": "HACCP,ISO22000",
    "minOrderAmount": 100000,
    "operatingHours": "09:00-18:00",
    "phoneNumber": "010-9876-5432",
    "email": "distributor1@example.com",
    "address": "서울시 송파구 올림픽로 456"
  }' | jq '.'
echo ""

# 3. 내 유통업체 정보 조회
echo "3. 내 유통업체 정보 조회"
echo "GET $BASE_URL/api/distributor/info"
curl -s -X GET "$BASE_URL/api/distributor/info" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 4. 유통업체 정보 수정
echo "4. 유통업체 정보 수정 (서비스 지역 추가)"
echo "POST $BASE_URL/api/distributor/info"
curl -s -X POST "$BASE_URL/api/distributor/info" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "distributorName": "신선식자재 유통",
    "supplyProducts": "쌀/곡물,채소,과일,육류,수산물,유제품",
    "serviceRegions": "서울,경기,인천,충남,충북",
    "deliveryAvailable": true,
    "deliveryInfo": "배송비 무료 (10만원 이상), 당일/익일 배송 가능",
    "description": "신선한 식자재를 공급하는 전문 유통업체입니다. 20년 경력!",
    "certifications": "HACCP,ISO22000,유기농인증",
    "minOrderAmount": 100000,
    "operatingHours": "08:00-19:00",
    "phoneNumber": "010-9876-5432",
    "email": "distributor1@example.com",
    "address": "서울시 송파구 올림픽로 456"
  }' | jq '.'
echo ""

# 5. 유통업체 비활성화
echo "5. 유통업체 비활성화"
echo "PUT $BASE_URL/api/distributor/status?activate=false"
curl -s -X PUT "$BASE_URL/api/distributor/status?activate=false" \
  -H "Authorization: Bearer $TOKEN"
echo ""
echo ""

# 6. 유통업체 활성화
echo "6. 유통업체 활성화"
echo "PUT $BASE_URL/api/distributor/status?activate=true"
curl -s -X PUT "$BASE_URL/api/distributor/status?activate=true" \
  -H "Authorization: Bearer $TOKEN"
echo ""
echo ""

# 7. 매장 사장님이 유통업체 정보 조회
echo "7. 매장 사장님이 유통업체 정보 조회"
STORE_LOGIN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_store1",
    "password": "password123"
  }')

STORE_TOKEN=$(echo "$STORE_LOGIN" | jq -r '.accessToken')

echo "GET $BASE_URL/api/distributor/info/distributor1"
curl -s -X GET "$BASE_URL/api/distributor/info/distributor1" \
  -H "Authorization: Bearer $STORE_TOKEN" | jq '.'
echo ""

echo "=========================================="
echo "테스트 완료"
echo "=========================================="
