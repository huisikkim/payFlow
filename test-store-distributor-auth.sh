#!/bin/bash

BASE_URL="http://localhost:8080/api/auth"

echo "=========================================="
echo "매장/유통업체 회원가입 및 로그인 테스트"
echo "=========================================="
echo ""

# 1. 매장 사장님 회원가입
echo "1. 매장 사장님 회원가입 테스트"
echo "POST $BASE_URL/signup"
curl -X POST "$BASE_URL/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "store_owner1",
    "password": "password123",
    "email": "store1@example.com",
    "userType": "STORE_OWNER",
    "businessNumber": "123-45-67890",
    "businessName": "맛있는 식당",
    "ownerName": "김사장",
    "phoneNumber": "010-1234-5678",
    "address": "서울시 강남구 테헤란로 123"
  }'
echo -e "\n"

# 2. 유통업체 회원가입
echo "2. 유통업체 회원가입 테스트"
echo "POST $BASE_URL/signup"
curl -X POST "$BASE_URL/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "distributor1",
    "password": "password123",
    "email": "distributor1@example.com",
    "userType": "DISTRIBUTOR",
    "businessNumber": "987-65-43210",
    "businessName": "신선식자재 유통",
    "ownerName": "이대표",
    "phoneNumber": "010-9876-5432",
    "address": "서울시 송파구 올림픽로 456"
  }'
echo -e "\n"

# 3. 매장 사장님 로그인
echo "3. 매장 사장님 로그인 테스트"
echo "POST $BASE_URL/login"
STORE_RESPONSE=$(curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "store_owner1",
    "password": "password123"
  }')
echo "$STORE_RESPONSE" | jq '.'
STORE_TOKEN=$(echo "$STORE_RESPONSE" | jq -r '.accessToken')
echo -e "\n"

# 4. 유통업체 로그인
echo "4. 유통업체 로그인 테스트"
echo "POST $BASE_URL/login"
DISTRIBUTOR_RESPONSE=$(curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "distributor1",
    "password": "password123"
  }')
echo "$DISTRIBUTOR_RESPONSE" | jq '.'
DISTRIBUTOR_TOKEN=$(echo "$DISTRIBUTOR_RESPONSE" | jq -r '.accessToken')
echo -e "\n"

# 5. 매장 사장님 프로필 조회
echo "5. 매장 사장님 프로필 조회"
echo "GET http://localhost:8080/api/user/profile"
curl -s -X GET "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer $STORE_TOKEN" | jq '.'
echo -e "\n"

# 6. 유통업체 프로필 조회
echo "6. 유통업체 프로필 조회"
echo "GET http://localhost:8080/api/user/profile"
curl -s -X GET "http://localhost:8080/api/user/profile" \
  -H "Authorization: Bearer $DISTRIBUTOR_TOKEN" | jq '.'
echo -e "\n"

echo "=========================================="
echo "테스트 완료"
echo "=========================================="
