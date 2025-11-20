package com.example.payflow.crypto.presentation;

import com.example.payflow.crypto.application.BithumbWebSocketService;
import com.example.payflow.crypto.application.ExchangeComparisonService;
import com.example.payflow.crypto.application.UpbitWebSocketService;
import com.example.payflow.crypto.domain.CoinTicker;
import com.example.payflow.crypto.domain.ExchangeComparison;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/crypto")
@RequiredArgsConstructor
public class CryptoController {
    
    private final UpbitWebSocketService upbitWebSocketService;
    private final BithumbWebSocketService bithumbWebSocketService;
    private final ExchangeComparisonService comparisonService;
    
    // 업비트 시세
    @GetMapping("/upbit/tickers")
    public ResponseEntity<List<CoinTicker>> getUpbitTickers() {
        return ResponseEntity.ok(upbitWebSocketService.getAllTickers());
    }
    
    @GetMapping("/upbit/tickers/{market}")
    public ResponseEntity<CoinTicker> getUpbitTicker(@PathVariable String market) {
        CoinTicker ticker = upbitWebSocketService.getTicker(market);
        if (ticker == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticker);
    }
    
    // 빗썸 시세
    @GetMapping("/bithumb/tickers")
    public ResponseEntity<List<CoinTicker>> getBithumbTickers() {
        return ResponseEntity.ok(bithumbWebSocketService.getAllTickers());
    }
    
    @GetMapping("/bithumb/tickers/{market}")
    public ResponseEntity<CoinTicker> getBithumbTicker(@PathVariable String market) {
        CoinTicker ticker = bithumbWebSocketService.getTicker(market);
        if (ticker == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticker);
    }
    
    // 거래소 비교
    @GetMapping("/comparison")
    public ResponseEntity<List<ExchangeComparison>> getComparisons() {
        return ResponseEntity.ok(comparisonService.getComparisons());
    }
    
    @GetMapping("/comparison/{market}")
    public ResponseEntity<ExchangeComparison> getComparison(@PathVariable String market) {
        ExchangeComparison comparison = comparisonService.getComparison(market);
        if (comparison == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(comparison);
    }
    
    // 차익거래 기회
    @GetMapping("/arbitrage")
    public ResponseEntity<List<ExchangeComparison>> getArbitrageOpportunities(
            @RequestParam(defaultValue = "1.0") BigDecimal minDiffPercent) {
        return ResponseEntity.ok(comparisonService.getArbitrageOpportunities(minDiffPercent));
    }
    
    // 레거시 호환 (기존 API)
    @GetMapping("/tickers")
    public ResponseEntity<List<CoinTicker>> getAllTickers() {
        return ResponseEntity.ok(upbitWebSocketService.getAllTickers());
    }
    
    @GetMapping("/tickers/{market}")
    public ResponseEntity<CoinTicker> getTicker(@PathVariable String market) {
        CoinTicker ticker = upbitWebSocketService.getTicker(market);
        if (ticker == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticker);
    }
}
