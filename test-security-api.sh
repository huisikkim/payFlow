#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "PayFlow Security API Test"
echo "=========================================="
echo ""

# 1. 회원가입 테스트
echo "1. 회원가입 테스트"
echo "POST /api/auth/signup"
curl -X POST "$BASE_URL/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test1234",
    "email": "testuser@example.com"
  }'
echo -e "\n"

# 2. 로그인 테스트 (일반 사용자)
echo "2. 로그인 테스트 (일반 사용자)"
echo "POST /api/auth/login"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }')
echo $LOGIN_RESPONSE | jq '.'

# JWT 토큰 추출
USER_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
echo "User Token: $USER_TOKEN"
echo ""

# 3. 로그인 테스트 (관리자)
echo "3. 로그인 테스트 (관리자)"
echo "POST /api/auth/login"
ADMIN_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }')
echo $ADMIN_LOGIN_RESPONSE | jq '.'

# JWT 토큰 추출
ADMIN_TOKEN=$(echo $ADMIN_LOGIN_RESPONSE | jq -r '.accessToken')
echo "Admin Token: $ADMIN_TOKEN"
echo ""

# 4. 사용자 프로필 조회 (인증 필요)
echo "4. 사용자 프로필 조회 (인증 필요)"
echo "GET /api/user/profile"
curl -s -X GET "$BASE_URL/api/user/profile" \
  -H "Authorization: Bearer $USER_TOKEN" | jq '.'
echo ""

# 5. 주문 생성 (인증 필요)
echo "5. 주문 생성 (인증 필요)"
echo "POST /api/orders"
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "productName": "테스트 상품",
    "amount": 10000
  }')
echo $ORDER_RESPONSE | jq '.'

ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.orderId')
echo "Order ID: $ORDER_ID"
echo ""

# 6. 주문 조회 (인증 필요)
echo "6. 주문 조회 (인증 필요)"
echo "GET /api/orders/$ORDER_ID"
curl -s -X GET "$BASE_URL/api/orders/$ORDER_ID" \
  -H "Authorization: Bearer $USER_TOKEN" | jq '.'
echo ""

# 7. 관리자 대시보드 접근 (관리자 권한 필요)
echo "7. 관리자 대시보드 접근 (관리자 권한 필요)"
echo "GET /api/admin/dashboard"
curl -s -X GET "$BASE_URL/api/admin/dashboard" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.'
echo ""

# 8. 관리자 대시보드 접근 실패 테스트 (일반 사용자)
echo "8. 관리자 대시보드 접근 실패 테스트 (일반 사용자 토큰)"
echo "GET /api/admin/dashboard"
curl -s -X GET "$BASE_URL/api/admin/dashboard" \
  -H "Authorization: Bearer $USER_TOKEN"
echo -e "\n"

# 9. 인증 없이 접근 시도 (실패 예상)
echo "9. 인증 없이 주문 조회 시도 (실패 예상)"
echo "GET /api/orders/$ORDER_ID"
curl -s -X GET "$BASE_URL/api/orders/$ORDER_ID"
echo -e "\n"

echo "=========================================="
echo "테스트 완료"
echo "=========================================="
