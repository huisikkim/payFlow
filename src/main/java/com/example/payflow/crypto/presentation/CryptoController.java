package com.example.payflow.crypto.presentation;

import com.example.payflow.crypto.application.UpbitWebSocketService;
import com.example.payflow.crypto.domain.CoinTicker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crypto")
@RequiredArgsConstructor
public class CryptoController {
    
    private final UpbitWebSocketService upbitWebSocketService;
    
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
