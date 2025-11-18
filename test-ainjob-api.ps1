# PowerShell script for testing AINJOB API

$BASE_URL = "http://localhost:8080/api/ainjob"

Write-Host "=== AINJOB API 테스트 ===" -ForegroundColor Green
Write-Host ""

# 1. 지원자 생성
Write-Host "1. 지원자 생성" -ForegroundColor Yellow
$applicantBody = @{
    name = "김철수"
    email = "kim.cs@email.com"
    phone = "010-1111-0001"
    birthDate = "1990-01-15"
    address = @{
        city = "서울"
        district = "강남구"
        detail = "테헤란로 123"
    }
    educations = @(
        @{
            level = "BACHELOR"
            majorName = "컴퓨터공학"
            schoolName = "서울대학교"
            startDate = "2008-03-01"
            endDate = "2012-02-28"
            status = "GRADUATED"
        }
    )
    careers = @(
        @{
            companyName = "네이버"
            position = "Backend Developer"
            description = "Java/Spring Boot 기반 서비스 개발"
            startDate = "2012-03-01"
            endDate = "2017-12-31"
            skills = @(
                @{
                    skillName = "Java"
                    proficiencyLevel = 5
                },
                @{
                    skillName = "Spring Boot"
                    proficiencyLevel = 4
                }
            )
        },
        @{
            companyName = "카카오"
            position = "Senior Backend Developer"
            description = "MSA 아키텍처 설계 및 구현"
            startDate = "2018-01-01"
            endDate = $null
            skills = @(
                @{
                    skillName = "Java"
                    proficiencyLevel = 5
                },
                @{
                    skillName = "Spring Boot"
                    proficiencyLevel = 5
                },
                @{
                    skillName = "AWS"
                    proficiencyLevel = 5
                }
            )
        }
    )
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Uri "$BASE_URL/applicants" -Method Post -Body $applicantBody -ContentType "application/json"
Write-Host ""

# 2. 채용 공고 생성
Write-Host "2. 채용 공고 생성" -ForegroundColor Yellow
$jobPostingBody = @{
    companyId = 1
    title = "백엔드 개발자 채용"
    description = "Java/Spring Boot 기반 백엔드 개발자를 모집합니다."
    position = "BACKEND"
    qualification = @{
        minEducationLevel = "BACHELOR"
        acceptedMajors = @("컴퓨터공학", "소프트웨어공학", "정보보안")
        minYearsOfExperience = 5
    }
    requiredSkills = @(
        @{
            skillName = "Java"
            isRequired = $true
            minProficiency = 4
        },
        @{
            skillName = "Spring Boot"
            isRequired = $true
            minProficiency = 4
        },
        @{
            skillName = "AWS"
            isRequired = $true
            minProficiency = 3
        }
    )
    openDate = "2024-01-01"
    closeDate = "2024-12-31"
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Uri "$BASE_URL/job-postings" -Method Post -Body $jobPostingBody -ContentType "application/json"
Write-Host ""

# 3. 공고 오픈
Write-Host "3. 채용 공고 오픈" -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BASE_URL/job-postings/1/open" -Method Patch
Write-Host ""

# 4. 지원하기
Write-Host "4. 지원하기" -ForegroundColor Yellow
$applicationBody = @{
    applicantId = 1
    jobPostingId = 1
    resumeId = $null
} | ConvertTo-Json

Invoke-RestMethod -Uri "$BASE_URL/applications" -Method Post -Body $applicationBody -ContentType "application/json"
Write-Host ""

# 5. 지원 상태 변경
Write-Host "5. 지원 상태 변경 (서류 합격)" -ForegroundColor Yellow
$statusChangeBody = @{
    fromStatus = "APPLIED"
    toStatus = "DOCUMENT_PASS"
    reason = "서류 검토 완료. 기술 스택 및 경력이 요구사항에 부합함."
} | ConvertTo-Json

Invoke-RestMethod -Uri "$BASE_URL/applications/1/status" -Method Patch -Body $statusChangeBody -ContentType "application/json"
Write-Host ""

# 6. 지원자 목록 조회
Write-Host "6. 지원자 목록 조회" -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BASE_URL/applicants" -Method Get
Write-Host ""

# 7. 채용 공고 목록 조회
Write-Host "7. 채용 공고 목록 조회" -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BASE_URL/job-postings" -Method Get
Write-Host ""

# 8. 공고별 지원 내역 조회
Write-Host "8. 공고별 지원 내역 조회" -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BASE_URL/applications/job-posting/1" -Method Get
Write-Host ""

# 9. 지원 상세 조회
Write-Host "9. 지원 상세 조회 (이력 포함)" -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BASE_URL/applications/1" -Method Get
Write-Host ""

Write-Host "=== 테스트 완료 ===" -ForegroundColor Green
