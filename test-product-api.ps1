# Pick Swap Product API 테스트 스크립트
$baseUrl = "http://localhost:8080/api/products"

Write-Host "=== Pick Swap Product API 테스트 ===" -ForegroundColor Cyan
Write-Host ""

# 1. Home Feed 조회 (최신 상품 리스트)
Write-Host "1. Home Feed 조회 (최신 상품 20개)" -ForegroundColor Yellow
$response = Invoke-RestMethod -Uri "$baseUrl/feed?page=0&size=20" -Method Get
Write-Host "총 상품 수: $($response.totalElements)" -ForegroundColor Green
Write-Host "현재 페이지: $($response.number), 총 페이지: $($response.totalPages)" -ForegroundColor Green
Write-Host "상품 목록 (처음 3개):" -ForegroundColor Green
$response.content | Select-Object -First 3 | ForEach-Object {
    Write-Host "  - [$($_.id)] $($_.title) - $($_.price)원 (좋아요: $($_.likeCount), 조회: $($_.viewCount))" -ForegroundColor White
}
Write-Host ""

# 2. 인기 상품 조회
Write-Host "2. 인기 상품 조회 (좋아요 많은 순)" -ForegroundColor Yellow
$response = Invoke-RestMethod -Uri "$baseUrl/popular?page=0&size=10" -Method Get
Write-Host "인기 상품 목록 (처음 5개):" -ForegroundColor Green
$response.content | Select-Object -First 5 | ForEach-Object {
    Write-Host "  - [$($_.id)] $($_.title) - 좋아요: $($_.likeCount), 조회: $($_.viewCount)" -ForegroundColor White
}
Write-Host ""

# 3. 카테고리별 조회 (전자기기)
Write-Host "3. 카테고리별 조회 (ELECTRONICS)" -ForegroundColor Yellow
$response = Invoke-RestMethod -Uri "$baseUrl/category/ELECTRONICS?page=0&size=10" -Method Get
Write-Host "전자기기 상품 수: $($response.totalElements)" -ForegroundColor Green
$response.content | ForEach-Object {
    Write-Host "  - [$($_.id)] $($_.title) - $($_.price)원" -ForegroundColor White
}
Write-Host ""

# 4. 키워드 검색
Write-Host "4. 키워드 검색 (아이폰)" -ForegroundColor Yellow
$response = Invoke-RestMethod -Uri "$baseUrl/search?keyword=아이폰&page=0&size=10" -Method Get
Write-Host "검색 결과: $($response.totalElements)개" -ForegroundColor Green
$response.content | ForEach-Object {
    Write-Host "  - [$($_.id)] $($_.title) - $($_.price)원" -ForegroundColor White
}
Write-Host ""

# 5. 상품 상세 조회
Write-Host "5. 상품 상세 조회 (ID: 1)" -ForegroundColor Yellow
$product = Invoke-RestMethod -Uri "$baseUrl/1" -Method Get
Write-Host "제목: $($product.title)" -ForegroundColor Green
Write-Host "가격: $($product.price)원" -ForegroundColor Green
Write-Host "카테고리: $($product.category)" -ForegroundColor Green
Write-Host "상태: $($product.productCondition)" -ForegroundColor Green
Write-Host "판매자: $($product.sellerName)" -ForegroundColor Green
Write-Host "위치: $($product.location)" -ForegroundColor Green
Write-Host "조회수: $($product.viewCount), 좋아요: $($product.likeCount)" -ForegroundColor Green
Write-Host ""

# 6. 상품 등록
Write-Host "6. 새 상품 등록" -ForegroundColor Yellow
$newProduct = @{
    title = "테스트 상품 - 갤럭시 S23"
    description = "API 테스트용 상품입니다."
    price = 750000
    category = "ELECTRONICS"
    productCondition = "LIKE_NEW"
    sellerId = 1
    sellerName = "테스트 판매자"
    location = "서울 강남구"
    imageUrls = @("https://picsum.photos/400/400?random=100")
} | ConvertTo-Json

$created = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $newProduct -ContentType "application/json"
Write-Host "상품 등록 완료! ID: $($created.id)" -ForegroundColor Green
Write-Host "제목: $($created.title)" -ForegroundColor Green
$createdId = $created.id
Write-Host ""

# 7. 상품 좋아요
Write-Host "7. 상품 좋아요 (ID: $createdId)" -ForegroundColor Yellow
Invoke-RestMethod -Uri "$baseUrl/$createdId/like" -Method Post | Out-Null
Write-Host "좋아요 완료!" -ForegroundColor Green
Write-Host ""

# 8. 상품 수정
Write-Host "8. 상품 수정 (ID: $createdId)" -ForegroundColor Yellow
$updateProduct = @{
    title = "테스트 상품 - 갤럭시 S23 (가격 인하)"
    description = "가격을 낮췄습니다!"
    price = 700000
    category = "ELECTRONICS"
    productCondition = "LIKE_NEW"
    location = "서울 강남구"
    imageUrls = @("https://picsum.photos/400/400?random=100")
} | ConvertTo-Json

$updated = Invoke-RestMethod -Uri "$baseUrl/$createdId" -Method Put -Body $updateProduct -ContentType "application/json"
Write-Host "상품 수정 완료!" -ForegroundColor Green
Write-Host "새 제목: $($updated.title)" -ForegroundColor Green
Write-Host "새 가격: $($updated.price)원" -ForegroundColor Green
Write-Host ""

# 9. 판매자의 상품 목록 조회
Write-Host "9. 판매자의 상품 목록 조회 (Seller ID: 1)" -ForegroundColor Yellow
$response = Invoke-RestMethod -Uri "$baseUrl/seller/1?page=0&size=10" -Method Get
Write-Host "판매자의 상품 수: $($response.totalElements)" -ForegroundColor Green
$response.content | ForEach-Object {
    Write-Host "  - [$($_.id)] $($_.title) - $($_.price)원" -ForegroundColor White
}
Write-Host ""

# 10. 가격 범위 검색
Write-Host "10. 가격 범위 검색 (10만원 ~ 50만원)" -ForegroundColor Yellow
$response = Invoke-RestMethod -Uri "$baseUrl/price-range?minPrice=100000&maxPrice=500000&page=0&size=10" -Method Get
Write-Host "검색 결과: $($response.totalElements)개" -ForegroundColor Green
$response.content | Select-Object -First 5 | ForEach-Object {
    Write-Host "  - [$($_.id)] $($_.title) - $($_.price)원" -ForegroundColor White
}
Write-Host ""

# 11. 상품 판매 완료 처리
Write-Host "11. 상품 판매 완료 처리 (ID: $createdId)" -ForegroundColor Yellow
Invoke-RestMethod -Uri "$baseUrl/$createdId/sold" -Method Post | Out-Null
Write-Host "판매 완료 처리됨!" -ForegroundColor Green
Write-Host ""

# 12. 상품 삭제
Write-Host "12. 상품 삭제 (ID: $createdId)" -ForegroundColor Yellow
Invoke-RestMethod -Uri "$baseUrl/$createdId" -Method Delete | Out-Null
Write-Host "상품 삭제 완료!" -ForegroundColor Green
Write-Host ""

Write-Host "=== 모든 테스트 완료! ===" -ForegroundColor Cyan
