# 코인 시세 API 테스트 스크립트
$baseUrl = "http://localhost:8080"

Write-Host "================================" -ForegroundColor Cyan
Write-Host "코인 시세 API 테스트" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# 1. 전체 코인 시세 조회
Write-Host "1. 전체 코인 시세 조회" -ForegroundColor Yellow
Write-Host "GET $baseUrl/api/crypto/tickers" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/crypto/tickers" -Method Get
    Write-Host "✅ 성공: $($response.Count)개 코인 시세 조회됨" -ForegroundColor Green
    
    if ($response.Count -gt 0) {
        Write-Host "`n📊 상위 3개 코인:" -ForegroundColor Cyan
        $response | Select-Object -First 3 | ForEach-Object {
            $changeSymbol = if ($_.change -eq "RISE") { "▲" } elseif ($_.change -eq "FALL") { "▼" } else { "-" }
            Write-Host "  $($_.koreanName) ($($_.market))" -ForegroundColor White
            Write-Host "    현재가: $([math]::Round($_.tradePrice, 0))원" -ForegroundColor White
            Write-Host "    변동률: $changeSymbol $([math]::Abs([math]::Round($_.signedChangeRate, 2)))%" -ForegroundColor $(if ($_.change -eq "RISE") { "Red" } elseif ($_.change -eq "FALL") { "Blue" } else { "Gray" })
            Write-Host ""
        }
    }
} catch {
    Write-Host "❌ 실패: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan

# 2. 특정 코인 시세 조회 (비트코인)
Write-Host "2. 비트코인 시세 조회" -ForegroundColor Yellow
Write-Host "GET $baseUrl/api/crypto/tickers/KRW-BTC" -ForegroundColor Gray
try {
    $btc = Invoke-RestMethod -Uri "$baseUrl/api/crypto/tickers/KRW-BTC" -Method Get
    Write-Host "✅ 성공" -ForegroundColor Green
    Write-Host "`n📊 비트코인 상세 정보:" -ForegroundColor Cyan
    Write-Host "  이름: $($btc.koreanName) ($($btc.market))" -ForegroundColor White
    Write-Host "  현재가: $([math]::Round($btc.tradePrice, 0))원" -ForegroundColor White
    Write-Host "  시가: $([math]::Round($btc.openingPrice, 0))원" -ForegroundColor White
    Write-Host "  고가: $([math]::Round($btc.highPrice, 0))원" -ForegroundColor Red
    Write-Host "  저가: $([math]::Round($btc.lowPrice, 0))원" -ForegroundColor Blue
    Write-Host "  24시간 거래대금: $([math]::Round($btc.accTradePrice24h / 100000000, 0))억원" -ForegroundColor White
} catch {
    Write-Host "❌ 실패: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan

# 3. 이더리움 시세 조회
Write-Host "3. 이더리움 시세 조회" -ForegroundColor Yellow
Write-Host "GET $baseUrl/api/crypto/tickers/KRW-ETH" -ForegroundColor Gray
try {
    $eth = Invoke-RestMethod -Uri "$baseUrl/api/crypto/tickers/KRW-ETH" -Method Get
    Write-Host "✅ 성공" -ForegroundColor Green
    Write-Host "`n📊 이더리움 상세 정보:" -ForegroundColor Cyan
    Write-Host "  이름: $($eth.koreanName) ($($eth.market))" -ForegroundColor White
    Write-Host "  현재가: $([math]::Round($eth.tradePrice, 0))원" -ForegroundColor White
    $changeSymbol = if ($eth.change -eq "RISE") { "▲" } elseif ($eth.change -eq "FALL") { "▼" } else { "-" }
    Write-Host "  변동률: $changeSymbol $([math]::Abs([math]::Round($eth.signedChangeRate, 2)))%" -ForegroundColor $(if ($eth.change -eq "RISE") { "Red" } elseif ($eth.change -eq "FALL") { "Blue" } else { "Gray" })
} catch {
    Write-Host "❌ 실패: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "✅ 테스트 완료!" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 웹 UI 접속: $baseUrl/crypto" -ForegroundColor Cyan
Write-Host "📡 웹소켓 연결: ws://localhost:8080/ws/crypto" -ForegroundColor Cyan
Write-Host ""
