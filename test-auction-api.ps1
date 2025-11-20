# PickSwap 경매 API 테스트 스크립트
Write-Host "=== PickSwap 경매 API 테스트 ===" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"

# 1. 로그인 (JWT 토큰 획득)
Write-Host "1. 사용자 로그인..." -ForegroundColor Yellow
$loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body '{"username":"user","password":"password"}'

$token = $loginResponse.accessToken
Write-Host "✅ 로그인 성공: $($loginResponse.username)" -ForegroundColor Green
Write-Host ""

# 2. 내 상품 조회
Write-Host "2. 내 상품 조회..." -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $myProducts = Invoke-RestMethod -Uri "$baseUrl/api/products/my?page=0&size=10" `
        -Method Get `
        -Headers $headers
    
    if ($myProducts.content.Count -gt 0) {
        $productId = $myProducts.content[0].id
        Write-Host "✅ 상품 조회 성공: 상품 ID = $productId" -ForegroundColor Green
    } else {
        Write-Host "⚠️  등록된 상품이 없습니다. 먼저 상품을 등록하세요." -ForegroundColor Yellow
        exit
    }
} catch {
    Write-Host "❌ 상품 조회 실패" -ForegroundColor Red
    exit
}
Write-Host ""

# 3. 경매 생성
Write-Host "3. 경매 생성..." -ForegroundColor Yellow
$startTime = (Get-Date).AddMinutes(1).ToString("yyyy-MM-ddTHH:mm:ss")
$endTime = (Get-Date).AddDays(3).ToString("yyyy-MM-ddTHH:mm:ss")

$auctionData = @{
    productId = $productId
    startPrice = 50000
    buyNowPrice = 200000
    minBidIncrement = 1000
    startTime = $startTime
    endTime = $endTime
} | ConvertTo-Json

try {
    $auction = Invoke-RestMethod -Uri "$baseUrl/api/auctions" `
        -Method Post `
        -Headers $headers `
        -Body $auctionData
    
    $auctionId = $auction.id
    Write-Host "✅ 경매 생성 성공: 경매 ID = $auctionId" -ForegroundColor Green
    Write-Host "   시작가: $($auction.startPrice)원" -ForegroundColor Gray
    Write-Host "   즉시 구매가: $($auction.buyNowPrice)원" -ForegroundColor Gray
} catch {
    Write-Host "❌ 경매 생성 실패: $_" -ForegroundColor Red
    exit
}
Write-Host ""

# 4. 경매 조회
Write-Host "4. 경매 상세 조회..." -ForegroundColor Yellow
try {
    $auctionDetail = Invoke-RestMethod -Uri "$baseUrl/api/auctions/$auctionId" `
        -Method Get `
        -Headers $headers
    
    Write-Host "✅ 경매 조회 성공" -ForegroundColor Green
    Write-Host "   현재가: $($auctionDetail.currentPrice)원" -ForegroundColor Gray
    Write-Host "   입찰 횟수: $($auctionDetail.bidCount)회" -ForegroundColor Gray
    Write-Host "   상태: $($auctionDetail.status)" -ForegroundColor Gray
} catch {
    Write-Host "❌ 경매 조회 실패" -ForegroundColor Red
}
Write-Host ""

# 5. 다른 사용자로 로그인 (입찰용)
Write-Host "5. 관리자 로그인 (입찰용)..." -ForegroundColor Yellow
$adminLoginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body '{"username":"admin","password":"admin"}'

$adminToken = $adminLoginResponse.accessToken
$adminHeaders = @{
    "Authorization" = "Bearer $adminToken"
    "Content-Type" = "application/json"
}
Write-Host "✅ 관리자 로그인 성공" -ForegroundColor Green
Write-Host ""

# 6. 입찰하기
Write-Host "6. 입찰하기..." -ForegroundColor Yellow
$bidData = @{
    amount = 55000
} | ConvertTo-Json

try {
    $bid = Invoke-RestMethod -Uri "$baseUrl/api/auctions/$auctionId/bids" `
        -Method Post `
        -Headers $adminHeaders `
        -Body $bidData
    
    Write-Host "✅ 입찰 성공" -ForegroundColor Green
    Write-Host "   입찰 금액: $($bid.amount)원" -ForegroundColor Gray
    Write-Host "   입찰자: $($bid.bidderName)" -ForegroundColor Gray
} catch {
    Write-Host "⚠️  입찰 실패 (경매가 아직 시작되지 않았을 수 있습니다)" -ForegroundColor Yellow
}
Write-Host ""

# 7. 자동 입찰 설정
Write-Host "7. 자동 입찰 설정..." -ForegroundColor Yellow
$autoBidData = @{
    maxAmount = 100000
} | ConvertTo-Json

try {
    $autoBid = Invoke-RestMethod -Uri "$baseUrl/api/auctions/$auctionId/auto-bid" `
        -Method Post `
        -Headers $adminHeaders `
        -Body $autoBidData
    
    Write-Host "✅ 자동 입찰 설정 성공" -ForegroundColor Green
    Write-Host "   최대 금액: $($autoBid.maxAmount)원" -ForegroundColor Gray
} catch {
    Write-Host "⚠️  자동 입찰 설정 실패" -ForegroundColor Yellow
}
Write-Host ""

# 8. 입찰 내역 조회
Write-Host "8. 입찰 내역 조회..." -ForegroundColor Yellow
try {
    $bids = Invoke-RestMethod -Uri "$baseUrl/api/auctions/$auctionId/bids?page=0&size=10" `
        -Method Get `
        -Headers $headers
    
    Write-Host "✅ 입찰 내역 조회 성공" -ForegroundColor Green
    Write-Host "   총 입찰 수: $($bids.totalElements)개" -ForegroundColor Gray
} catch {
    Write-Host "❌ 입찰 내역 조회 실패" -ForegroundColor Red
}
Write-Host ""

# 9. 진행 중인 경매 목록
Write-Host "9. 진행 중인 경매 목록 조회..." -ForegroundColor Yellow
try {
    $activeAuctions = Invoke-RestMethod -Uri "$baseUrl/api/auctions/active?page=0&size=10" `
        -Method Get
    
    Write-Host "✅ 진행 중인 경매: $($activeAuctions.totalElements)개" -ForegroundColor Green
} catch {
    Write-Host "❌ 경매 목록 조회 실패" -ForegroundColor Red
}
Write-Host ""

# 10. 내 판매 경매 조회
Write-Host "10. 내 판매 경매 조회..." -ForegroundColor Yellow
try {
    $myAuctions = Invoke-RestMethod -Uri "$baseUrl/api/auctions/my-selling?page=0&size=10" `
        -Method Get `
        -Headers $headers
    
    Write-Host "✅ 내 판매 경매: $($myAuctions.totalElements)개" -ForegroundColor Green
} catch {
    Write-Host "❌ 내 판매 경매 조회 실패" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== 테스트 완료 ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "웹 UI 접속:" -ForegroundColor Yellow
Write-Host "  경매 목록: http://localhost:8080/pickswap/auctions" -ForegroundColor Gray
Write-Host "  경매 상세: http://localhost:8080/pickswap/auctions/$auctionId" -ForegroundColor Gray
Write-Host "  경매 등록: http://localhost:8080/pickswap/auctions/create" -ForegroundColor Gray
Write-Host "  내 경매: http://localhost:8080/pickswap/auctions/my" -ForegroundColor Gray
