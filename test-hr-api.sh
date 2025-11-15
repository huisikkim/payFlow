#!/bin/bash

BASE_URL="http://localhost:8080"
TOKEN=""

echo "=========================================="
echo "근태관리 시스템 API 테스트"
echo "=========================================="
echo ""

# 1. 로그인
echo "1. 로그인 (user/password)"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ 로그인 실패"
    exit 1
fi

echo "✅ 로그인 성공"
echo "Token: ${TOKEN:0:50}..."
echo ""

# 2. 출근
echo "2. 출근 처리"
CHECK_IN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/hr/attendance/check-in" \
  -H "Authorization: Bearer $TOKEN")

echo "$CHECK_IN_RESPONSE" | python -m json.tool 2>/dev/null || echo "$CHECK_IN_RESPONSE"
echo ""

sleep 2

# 3. 오늘 근태 조회
echo "3. 오늘 근태 조회"
TODAY_RESPONSE=$(curl -s -X GET "$BASE_URL/api/hr/attendance/today" \
  -H "Authorization: Bearer $TOKEN")

echo "$TODAY_RESPONSE" | python -m json.tool 2>/dev/null || echo "$TODAY_RESPONSE"
echo ""

# 4. 퇴근
echo "4. 퇴근 처리"
CHECK_OUT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/hr/attendance/check-out" \
  -H "Authorization: Bearer $TOKEN")

echo "$CHECK_OUT_RESPONSE" | python -m json.tool 2>/dev/null || echo "$CHECK_OUT_RESPONSE"
echo ""

# 5. 월별 근태 조회
YEAR=$(date +%Y)
MONTH=$(date +%-m)

echo "5. 월별 근태 조회 ($YEAR년 $MONTH월)"
MONTHLY_RESPONSE=$(curl -s -X GET "$BASE_URL/api/hr/attendance/monthly?year=$YEAR&month=$MONTH" \
  -H "Authorization: Bearer $TOKEN")

echo "$MONTHLY_RESPONSE" | python -m json.tool 2>/dev/null || echo "$MONTHLY_RESPONSE"
echo ""

# 6. 잔여 연차 조회
echo "6. 잔여 연차 조회"
REMAINING_RESPONSE=$(curl -s -X GET "$BASE_URL/api/hr/leaves/remaining-days" \
  -H "Authorization: Bearer $TOKEN")

echo "$REMAINING_RESPONSE" | python -m json.tool 2>/dev/null || echo "$REMAINING_RESPONSE"
echo ""

# 7. 휴가 신청
START_DATE=$(date -d "+7 days" +%Y-%m-%d 2>/dev/null || date -v+7d +%Y-%m-%d)
END_DATE=$(date -d "+9 days" +%Y-%m-%d 2>/dev/null || date -v+9d +%Y-%m-%d)

echo "7. 휴가 신청 ($START_DATE ~ $END_DATE)"
LEAVE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/hr/leaves" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"type\": \"ANNUAL\",
    \"startDate\": \"$START_DATE\",
    \"endDate\": \"$END_DATE\",
    \"days\": 3,
    \"reason\": \"개인 사유로 인한 연차 사용\"
  }")

echo "$LEAVE_RESPONSE" | python -m json.tool 2>/dev/null || echo "$LEAVE_RESPONSE"
LEAVE_ID=$(echo $LEAVE_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
echo ""

# 8. 내 휴가 목록 조회
echo "8. 내 휴가 목록 조회"
MY_LEAVES_RESPONSE=$(curl -s -X GET "$BASE_URL/api/hr/leaves/my" \
  -H "Authorization: Bearer $TOKEN")

echo "$MY_LEAVES_RESPONSE" | python -m json.tool 2>/dev/null || echo "$MY_LEAVES_RESPONSE"
echo ""

# 9. 관리자 로그인
echo "9. 관리자 로그인 (admin/admin)"
ADMIN_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }')

ADMIN_TOKEN=$(echo $ADMIN_LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
    echo "❌ 관리자 로그인 실패"
else
    echo "✅ 관리자 로그인 성공"
    echo ""
    
    # 10. 대기 중인 휴가 목록 조회
    echo "10. 대기 중인 휴가 목록 조회 (관리자)"
    PENDING_RESPONSE=$(curl -s -X GET "$BASE_URL/api/hr/leaves/pending" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    
    echo "$PENDING_RESPONSE" | python -m json.tool 2>/dev/null || echo "$PENDING_RESPONSE"
    echo ""
    
    # 11. 휴가 승인
    if [ ! -z "$LEAVE_ID" ]; then
        echo "11. 휴가 승인 (ID: $LEAVE_ID)"
        APPROVE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/hr/leaves/$LEAVE_ID/approve" \
          -H "Authorization: Bearer $ADMIN_TOKEN")
        
        echo "$APPROVE_RESPONSE" | python -m json.tool 2>/dev/null || echo "$APPROVE_RESPONSE"
        echo ""
    fi
fi

echo "=========================================="
echo "테스트 완료!"
echo "=========================================="
echo ""
echo "웹 UI 접속: http://localhost:8080/hr/attendance"
