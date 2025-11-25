#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "매장 정보 API 테스트"
echo "=========================================="
echo ""

# 1. 매장 사장님 로그인
echo "1. 매장 사장님 로그인"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_store1",
    "password": "password123"
  }')

echo "$LOGIN_RESPONSE" | jq '.'
TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
echo ""

# 2. 매장 정보 등록
echo "2. 매장 정보 등록"
echo "POST $BASE_URL/api/store/info"
curl -s -X POST "$BASE_URL/api/store/info" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "storeName": "맛있는 한식당",
    "businessType": "한식",
    "region": "서울 강남구",
    "mainProducts": "쌀/곡물,채소,육류",
    "description": "정성을 다하는 한식당입니다",
    "employeeCount": 5,
    "operatingHours": "09:00-22:00",
    "phoneNumber": "010-1111-1111",
    "address": "서울시 강남구 테헤란로 123"
  }' | jq '.'
echo ""

# 3. 내 매장 정보 조회
echo "3. 내 매장 정보 조회"
echo "GET $BASE_URL/api/store/info"
curl -s -X GET "$BASE_URL/api/store/info" \
  -H "Authorization: Bearer $TOKEN" | jq '.'
echo ""

# 4. 매장 정보 수정
echo "4. 매장 정보 수정 (직원 수 변경)"
echo "POST $BASE_URL/api/store/info"
curl -s -X POST "$BASE_URL/api/store/info" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "storeName": "맛있는 한식당",
    "businessType": "한식",
    "region": "서울 강남구",
    "mainProducts": "쌀/곡물,채소,육류,수산물",
    "description": "정성을 다하는 한식당입니다. 신선한 재료만 사용합니다!",
    "employeeCount": 7,
    "operatingHours": "09:00-23:00",
    "phoneNumber": "010-1111-1111",
    "address": "서울시 강남구 테헤란로 123"
  }' | jq '.'
echo ""

# 5. 매장 비활성화
echo "5. 매장 비활성화"
echo "PUT $BASE_URL/api/store/status?activate=false"
curl -s -X PUT "$BASE_URL/api/store/status?activate=false" \
  -H "Authorization: Bearer $TOKEN"
echo ""
echo ""

# 6. 매장 활성화
echo "6. 매장 활성화"
echo "PUT $BASE_URL/api/store/status?activate=true"
curl -s -X PUT "$BASE_URL/api/store/status?activate=true" \
  -H "Authorization: Bearer $TOKEN"
echo ""
echo ""

# 7. 유통업체 로그인 후 매장 정보 조회
echo "7. 유통업체가 매장 정보 조회"
DISTRIBUTOR_LOGIN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "distributor1",
    "password": "password123"
  }')

DISTRIBUTOR_TOKEN=$(echo "$DISTRIBUTOR_LOGIN" | jq -r '.accessToken')

echo "GET $BASE_URL/api/store/info/test_store1"
curl -s -X GET "$BASE_URL/api/store/info/test_store1" \
  -H "Authorization: Bearer $DISTRIBUTOR_TOKEN" | jq '.'
echo ""

echo "=========================================="
echo "테스트 완료"
echo "=========================================="
