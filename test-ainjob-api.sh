#!/bin/bash

BASE_URL="http://localhost:8080/api/ainjob"

echo "=== AINJOB API 테스트 ==="
echo ""

# 1. 지원자 생성
echo "1. 지원자 생성"
curl -X POST "$BASE_URL/applicants" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "김철수",
    "email": "kim.cs@email.com",
    "phone": "010-1111-0001",
    "birthDate": "1990-01-15",
    "address": {
      "city": "서울",
      "district": "강남구",
      "detail": "테헤란로 123"
    },
    "educations": [
      {
        "level": "BACHELOR",
        "majorName": "컴퓨터공학",
        "schoolName": "서울대학교",
        "startDate": "2008-03-01",
        "endDate": "2012-02-28",
        "status": "GRADUATED"
      }
    ],
    "careers": [
      {
        "companyName": "네이버",
        "position": "Backend Developer",
        "description": "Java/Spring Boot 기반 서비스 개발",
        "startDate": "2012-03-01",
        "endDate": "2017-12-31",
        "skills": [
          {
            "skillName": "Java",
            "proficiencyLevel": 5
          },
          {
            "skillName": "Spring Boot",
            "proficiencyLevel": 4
          }
        ]
      },
      {
        "companyName": "카카오",
        "position": "Senior Backend Developer",
        "description": "MSA 아키텍처 설계 및 구현",
        "startDate": "2018-01-01",
        "endDate": null,
        "skills": [
          {
            "skillName": "Java",
            "proficiencyLevel": 5
          },
          {
            "skillName": "Spring Boot",
            "proficiencyLevel": 5
          },
          {
            "skillName": "AWS",
            "proficiencyLevel": 5
          }
        ]
      }
    ]
  }'
echo -e "\n"

# 2. 채용 공고 생성
echo "2. 채용 공고 생성"
curl -X POST "$BASE_URL/job-postings" \
  -H "Content-Type: application/json" \
  -d '{
    "companyId": 1,
    "title": "백엔드 개발자 채용",
    "description": "Java/Spring Boot 기반 백엔드 개발자를 모집합니다.",
    "position": "BACKEND",
    "qualification": {
      "minEducationLevel": "BACHELOR",
      "acceptedMajors": ["컴퓨터공학", "소프트웨어공학", "정보보안"],
      "minYearsOfExperience": 5
    },
    "requiredSkills": [
      {
        "skillName": "Java",
        "isRequired": true,
        "minProficiency": 4
      },
      {
        "skillName": "Spring Boot",
        "isRequired": true,
        "minProficiency": 4
      },
      {
        "skillName": "AWS",
        "isRequired": true,
        "minProficiency": 3
      }
    ],
    "openDate": "2024-01-01",
    "closeDate": "2024-12-31"
  }'
echo -e "\n"

# 3. 공고 오픈
echo "3. 채용 공고 오픈"
curl -X PATCH "$BASE_URL/job-postings/1/open"
echo -e "\n"

# 4. 지원하기
echo "4. 지원하기"
curl -X POST "$BASE_URL/applications" \
  -H "Content-Type: application/json" \
  -d '{
    "applicantId": 1,
    "jobPostingId": 1,
    "resumeId": null
  }'
echo -e "\n"

# 5. 지원 상태 변경 (서류 합격)
echo "5. 지원 상태 변경 (서류 합격)"
curl -X PATCH "$BASE_URL/applications/1/status" \
  -H "Content-Type: application/json" \
  -d '{
    "fromStatus": "APPLIED",
    "toStatus": "DOCUMENT_PASS",
    "reason": "서류 검토 완료. 기술 스택 및 경력이 요구사항에 부합함."
  }'
echo -e "\n"

# 6. 지원자 목록 조회
echo "6. 지원자 목록 조회"
curl -X GET "$BASE_URL/applicants"
echo -e "\n"

# 7. 채용 공고 목록 조회
echo "7. 채용 공고 목록 조회"
curl -X GET "$BASE_URL/job-postings"
echo -e "\n"

# 8. 공고별 지원 내역 조회
echo "8. 공고별 지원 내역 조회"
curl -X GET "$BASE_URL/applications/job-posting/1"
echo -e "\n"

# 9. 지원 상세 조회
echo "9. 지원 상세 조회 (이력 포함)"
curl -X GET "$BASE_URL/applications/1"
echo -e "\n"

echo "=== 테스트 완료 ==="
