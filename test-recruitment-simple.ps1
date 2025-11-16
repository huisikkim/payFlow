# Ontology-based Recruitment System Test

$BASE_URL = "http://localhost:8080"

Write-Host "Ontology-based Recruitment System Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Login
Write-Host "`n1. User Login..." -ForegroundColor Yellow
$loginBody = @{
    username = "user"
    password = "password"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.accessToken
Write-Host "Token acquired" -ForegroundColor Green

# 2. Get Skills
Write-Host "`n2. Get Skills (Ontology)..." -ForegroundColor Yellow
$headers = @{
    Authorization = "Bearer $token"
}
$skills = Invoke-RestMethod -Uri "$BASE_URL/api/recruitment/skills" -Headers $headers
Write-Host "Found $($skills.Count) skills" -ForegroundColor Green
$skills | Select-Object -First 5 | Format-Table id, name, category

# 3. Get Active Job Postings
Write-Host "`n3. Get Active Job Postings..." -ForegroundColor Yellow
$jobs = Invoke-RestMethod -Uri "$BASE_URL/api/recruitment/jobs/active" -Headers $headers
Write-Host "Found $($jobs.Count) job postings" -ForegroundColor Green
$jobs | Format-Table id, title, position, headcount

# 4. Get Candidates
Write-Host "`n4. Get Candidates..." -ForegroundColor Yellow
$candidates = Invoke-RestMethod -Uri "$BASE_URL/api/recruitment/candidates" -Headers $headers
Write-Host "Found $($candidates.Count) candidates" -ForegroundColor Green
$candidates | Format-Table id, name, education, totalYearsOfExperience

# 5. Get Applications by Job (sorted by matching score)
if ($jobs.Count -gt 0) {
    Write-Host "`n5. Get Applications for Backend Job (sorted by matching score)..." -ForegroundColor Yellow
    $jobId = $jobs[0].id
    $applications = Invoke-RestMethod -Uri "$BASE_URL/api/recruitment/applications/job/$jobId" -Headers $headers
    Write-Host "Found $($applications.Count) applications" -ForegroundColor Green
    $applications | Format-Table candidateName, matchingScore, status
}

# 6. Recommend Candidates for Job
if ($jobs.Count -gt 0) {
    Write-Host "`n6. Recommend Top 3 Candidates for Backend Job..." -ForegroundColor Yellow
    $jobId = $jobs[0].id
    $recommendations = Invoke-RestMethod -Uri "$BASE_URL/api/recruitment/recommendations/job/$jobId/candidates?topN=3" -Headers $headers
    Write-Host "Recommended $($recommendations.Count) candidates" -ForegroundColor Green
    foreach ($rec in $recommendations) {
        $score = [math]::Round($rec.matchingScore, 1)
        Write-Host "  - $($rec.candidate.name): $score points" -ForegroundColor White
    }
}

# 7. Recommend Jobs for Candidate
if ($candidates.Count -gt 0) {
    Write-Host "`n7. Recommend Jobs for First Candidate..." -ForegroundColor Yellow
    $candidateId = $candidates[0].id
    $jobRecs = Invoke-RestMethod -Uri "$BASE_URL/api/recruitment/recommendations/candidate/$candidateId/jobs?topN=5" -Headers $headers
    Write-Host "Recommended $($jobRecs.Count) jobs" -ForegroundColor Green
    foreach ($rec in $jobRecs) {
        $score = [math]::Round($rec.matchingScore, 1)
        Write-Host "  - $($rec.jobPosting.title): $score points" -ForegroundColor White
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Test Completed Successfully!" -ForegroundColor Green
Write-Host "`nKey Features:" -ForegroundColor Cyan
Write-Host "  - Skill Ontology (similar skill relationships)" -ForegroundColor White
Write-Host "  - Rule-based Matching Engine" -ForegroundColor White
Write-Host "  - Automatic Matching Score Calculation" -ForegroundColor White
Write-Host "  - Candidate/Job Recommendation System" -ForegroundColor White
Write-Host "`nWeb Dashboard: http://localhost:8080/recruitment/dashboard" -ForegroundColor Yellow
