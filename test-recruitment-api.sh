#!/bin/bash

# 온톨로지 기반 채용 시스템 API 테스트 스크립트

BASE_URL="http://localhost:8080"
echo "🎯 온톨로지 기반 채용 시스템 API 테스트 시작..."
echo "=========================================="

# 1. 로그인 (JWT 토큰 획득)
echo ""
echo "1️⃣ 사용자 로그인..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "✅ JWT 토큰 획득: ${TOKEN:0:50}..."

# 2. 기술 목록 조회
echo ""
echo "2️⃣ 기술 온톨로지 조회..."
curl -s -X GET "$BASE_URL/api/recruitment/skills" \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool | head -30
echo "✅ 기술 목록 조회 완료"

# 3. Java 기술의 유사 기술 조회
echo ""
echo "3️⃣ Java의 유사 기술 조회 (온톨로지 관계)..."
curl -s -X GET "$BASE_URL/api/recruitment/skills/1/similar" \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool
echo "✅ 유사 기술 조회 완료"

# 4. 활성 채용 공고 조회
echo ""
echo "4️⃣ 활성 채용 공고 조회..."
curl -s -X GET "$BASE_URL/api/recruitment/jobs/active" \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool
echo "✅ 채용 공고 조회 완료"

# 5. 지원자 목록 조회
echo ""
echo "5️⃣ 지원자 목록 조회..."
curl -s -X GET "$BASE_URL/api/recruitment/candidates" \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool | head -50
echo "✅ 지원자 목록 조회 완료"

# 6. 특정 공고의 지원자 조회 (매칭 스코어 순)
echo ""
echo "6️⃣ 백엔드 공고의 지원자 조회 (매칭 스코어 순)..."
curl -s -X GET "$BASE_URL/api/recruitment/applications/job/1" \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool
echo "✅ 지원자 매칭 조회 완료"

# 7. 공고별 추천 지원자 (온톨로지 기반 매칭)
echo ""
echo "7️⃣ 백엔드 공고에 적합한 Top 3 지원자 추천..."
curl -s -X GET "$BASE_URL/api/recruitment/recommendations/job/1/candidates?topN=3" \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool
echo "✅ 추천 지원자 조회 완료"

# 8. 지원자별 추천 공고
echo ""
echo "8️⃣ 김개발 지원자에게 적합한 공고 추천..."
curl -s -X GET "$BASE_URL/api/recruitment/recommendations/candidate/1/jobs?topN=5" \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool
echo "✅ 추천 공고 조회 완료"

# 9. 유사 지원자 찾기
echo ""
echo "9️⃣ 김개발과 유사한 지원자 찾기..."
curl -s -X GET "$BASE_URL/api/recruitment/recommendations/candidate/1/similar?topN=3" \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool
echo "✅ 유사 지원자 조회 완료"

# 10. 지원 상세 매칭 분석
echo ""
echo "🔟 지원 1번의 상세 매칭 분석..."
curl -s -X GET "$BASE_URL/api/recruitment/applications/1/matching-detail" \
  -H "Authorization: Bearer $TOKEN" | python -m json.tool
echo "✅ 매칭 분석 완료"

# 11. 새로운 지원자 생성
echo ""
echo "1️⃣1️⃣ 새로운 지원자 생성..."
NEW_CANDIDATE=$(curl -s -X POST "$BASE_URL/api/recruitment/candidates" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "최신입",
    "email": "choi.junior@example.com",
    "phone": "010-7777-8888",
    "education": "BACHELOR",
    "university": "고려대학교",
    "major": "컴퓨터학"
  }')

CANDIDATE_ID=$(echo $NEW_CANDIDATE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "✅ 새 지원자 생성 완료 (ID: $CANDIDATE_ID)"

# 12. 지원자에게 기술 추가
echo ""
echo "1️⃣2️⃣ 지원자에게 Java 기술 추가..."
curl -s -X POST "$BASE_URL/api/recruitment/candidates/$CANDIDATE_ID/skills?skillId=1&proficiencyLevel=INTERMEDIATE&yearsOfExperience=1&description=Java%20%EC%8B%A0%EC%9E%85" \
  -H "Authorization: Bearer $TOKEN"
echo ""
echo "✅ 기술 추가 완료"

# 13. 지원자에게 경력 추가
echo ""
echo "1️⃣3️⃣ 지원자에게 경력 추가..."
curl -s -X POST "$BASE_URL/api/recruitment/candidates/$CANDIDATE_ID/experiences?company=%EC%8A%A4%ED%83%80%ED%8A%B8%EC%97%85&position=%EC%A3%BC%EB%8B%88%EC%96%B4%20%EA%B0%9C%EB%B0%9C%EC%9E%90&startDate=2023-01-01&currentlyWorking=true&description=%EC%8A%A4%ED%83%80%ED%8A%B8%EC%97%85%20%EA%B0%9C%EB%B0%9C" \
  -H "Authorization: Bearer $TOKEN"
echo ""
echo "✅ 경력 추가 완료"

# 14. 새 지원자로 지원
echo ""
echo "1️⃣4️⃣ 새 지원자가 백엔드 공고에 지원..."
NEW_APPLICATION=$(curl -s -X POST "$BASE_URL/api/recruitment/applications" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"candidateId\": $CANDIDATE_ID,
    \"jobPostingId\": 1,
    \"coverLetter\": \"신입 개발자로 열정적으로 배우고 성장하겠습니다!\"
  }")

echo $NEW_APPLICATION | python -m json.tool
echo "✅ 지원 완료 (자동 매칭 스코어 계산됨)"

# 15. 관리자 로그인
echo ""
echo "1️⃣5️⃣ 관리자 로그인..."
ADMIN_LOGIN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }')

ADMIN_TOKEN=$(echo $ADMIN_LOGIN | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "✅ 관리자 토큰 획득"

# 16. 지원 상태 변경 (서류 합격)
echo ""
echo "1️⃣6️⃣ 지원 상태를 서류 합격으로 변경..."
curl -s -X PUT "$BASE_URL/api/recruitment/applications/1/status?status=SCREENING_PASSED&notes=%EC%84%9C%EB%A5%98%20%ED%95%A9%EA%B2%A9%20%EC%B6%95%ED%95%98%ED%95%A9%EB%8B%88%EB%8B%A4" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
echo ""
echo "✅ 상태 변경 완료"

# 17. 새 채용 공고 생성
echo ""
echo "1️⃣7️⃣ 새 채용 공고 생성 (DevOps 엔지니어)..."
NEW_JOB=$(curl -s -X POST "$BASE_URL/api/recruitment/jobs" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "DevOps 엔지니어",
    "description": "클라우드 인프라 구축 및 운영",
    "departmentId": 1,
    "position": "SENIOR",
    "headcount": 1,
    "startDate": "2025-11-16",
    "endDate": "2026-01-16"
  }')

JOB_ID=$(echo $NEW_JOB | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "✅ 새 공고 생성 완료 (ID: $JOB_ID)"

# 18. 공고에 요구사항 추가
echo ""
echo "1️⃣8️⃣ 공고에 Docker 필수 요구사항 추가..."
curl -s -X POST "$BASE_URL/api/recruitment/jobs/$JOB_ID/requirements?skillId=14&type=REQUIRED&minProficiency=ADVANCED&minYearsOfExperience=3&description=Docker%20%EC%8B%A4%EB%AC%B4%20%EA%B2%BD%ED%97%98" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
echo ""
echo "✅ 요구사항 추가 완료"

# 19. 공고 공개
echo ""
echo "1️⃣9️⃣ 공고 공개..."
curl -s -X POST "$BASE_URL/api/recruitment/jobs/$JOB_ID/publish" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
echo ""
echo "✅ 공고 공개 완료"

# 20. 최종 통계
echo ""
echo "2️⃣0️⃣ 최종 통계..."
echo "----------------------------------------"
echo "📊 전체 기술 수:"
curl -s -X GET "$BASE_URL/api/recruitment/skills" \
  -H "Authorization: Bearer $TOKEN" | grep -o '"id"' | wc -l

echo "📊 전체 지원자 수:"
curl -s -X GET "$BASE_URL/api/recruitment/candidates" \
  -H "Authorization: Bearer $TOKEN" | grep -o '"id"' | wc -l

echo "📊 활성 공고 수:"
curl -s -X GET "$BASE_URL/api/recruitment/jobs/active" \
  -H "Authorization: Bearer $TOKEN" | grep -o '"id"' | wc -l

echo ""
echo "=========================================="
echo "✅ 온톨로지 기반 채용 시스템 테스트 완료!"
echo ""
echo "🎯 주요 기능:"
echo "  - 기술 온톨로지 (유사 기술 관계)"
echo "  - 규칙 기반 매칭 엔진"
echo "  - 자동 매칭 스코어 계산"
echo "  - 지원자/공고 추천 시스템"
echo "  - 유사 지원자 찾기"
echo "  - 상세 매칭 분석"
echo ""
