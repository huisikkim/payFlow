package com.example.payflow.crypto.presentation;

import com.example.payflow.crypto.application.ExchangeComparisonServiceV2;
import com.example.payflow.crypto.application.OrderbookService;
import com.example.payflow.crypto.domain.ExchangePremium;
import com.example.payflow.crypto.domain.OrderbookData;
import com.example.payflow.crypto.domain.TradeStrength;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 거래소 비교 REST API 컨트롤러
 * Spring WebFlux를 사용한 실시간 스트리밍
 */
@Slf4j
@RestController
@RequestMapping("/api/crypto/comparison")
@RequiredArgsConstructor
public class ExchangeComparisonController {
    
    private final ExchangeComparisonServiceV2 comparisonService;
    private final OrderbookService orderbookService;
    
    /**
     * 모든 코인의 프리미엄 비교 (일회성)
     */
    @GetMapping("/premiums")
    public Mono<List<ExchangePremium>> getAllPremiums(
            @RequestParam(defaultValue = "UPBIT") String referenceExchange) {
        return Mono.fromCallable(() -> comparisonService.getAllPremiums(referenceExchange));
    }
    
    /**
     * 특정 코인의 프리미엄 비교 (일회성)
     */
    @GetMapping("/premiums/{market}")
    public Mono<ExchangePremium> getPremium(
            @PathVariable String market,
            @RequestParam(defaultValue = "UPBIT") String referenceExchange) {
        return Mono.fromCallable(() -> comparisonService.calculatePremium(market, referenceExchange));
    }
    
    /**
     * 실시간 프리미엄 스트리밍 (SSE)
     * 1초마다 모든 코인의 프리미엄 데이터 전송
     */
    @GetMapping(value = "/premiums/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<ExchangePremium>> streamPremiums(
            @RequestParam(defaultValue = "UPBIT") String referenceExchange) {
        return Flux.interval(Duration.ofSeconds(1))
            .map(tick -> comparisonService.getAllPremiums(referenceExchange))
            .doOnNext(premiums -> log.debug("📡 프리미엄 데이터 전송: {} 건", premiums.size()));
    }
    
    /**
     * 특정 코인의 실시간 프리미엄 스트리밍
     */
    @GetMapping(value = "/premiums/{market}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ExchangePremium> streamPremium(
            @PathVariable String market,
            @RequestParam(defaultValue = "UPBIT") String referenceExchange) {
        return Flux.interval(Duration.ofMillis(500))
            .map(tick -> comparisonService.calculatePremium(market, referenceExchange))
            .filter(premium -> premium != null);
    }
    
    /**
     * 체결강도 조회
     */
    @GetMapping("/trade-strength/{exchange}/{market}")
    public Mono<TradeStrength> getTradeStrength(
            @PathVariable String exchange,
            @PathVariable String market) {
        return Mono.fromCallable(() -> comparisonService.calculateTradeStrength(exchange, market));
    }
    
    /**
     * 차익거래 기회 찾기
     */
    @GetMapping("/arbitrage")
    public Mono<List<ExchangePremium>> findArbitrageOpportunities(
            @RequestParam(defaultValue = "UPBIT") String referenceExchange,
            @RequestParam(defaultValue = "1.0") BigDecimal minPremium) {
        return Mono.fromCallable(() -> 
            comparisonService.findArbitrageOpportunities(referenceExchange, minPremium));
    }
    
    /**
     * 차익거래 기회 실시간 스트리밍
     */
    @GetMapping(value = "/arbitrage/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<ExchangePremium>> streamArbitrageOpportunities(
            @RequestParam(defaultValue = "UPBIT") String referenceExchange,
            @RequestParam(defaultValue = "1.0") BigDecimal minPremium) {
        return Flux.interval(Duration.ofSeconds(2))
            .map(tick -> comparisonService.findArbitrageOpportunities(referenceExchange, minPremium))
            .filter(opportunities -> !opportunities.isEmpty())
            .doOnNext(opportunities -> log.info("🎯 차익거래 기회 발견: {} 건", opportunities.size()));
    }
    
    /**
     * 호가 데이터 조회
     */
    @GetMapping("/orderbook/{exchange}/{market}")
    public Mono<OrderbookData> getOrderbook(
            @PathVariable String exchange,
            @PathVariable String market) {
        if ("UPBIT".equalsIgnoreCase(exchange)) {
            return orderbookService.getUpbitOrderbook(market);
        } else if ("BITHUMB".equalsIgnoreCase(exchange)) {
            return orderbookService.getBithumbOrderbook(market);
        }
        return Mono.empty();
    }
    
    /**
     * 거래소별 호가 스프레드 비교
     */
    @GetMapping("/orderbook/spread/{market}")
    public Mono<Map<String, OrderbookData>> getOrderbookSpread(@PathVariable String market) {
        return Mono.zip(
            orderbookService.getUpbitOrderbook(market),
            orderbookService.getBithumbOrderbook(market)
        ).map(tuple -> {
            Map<String, OrderbookData> result = new HashMap<>();
            result.put("UPBIT", tuple.getT1());
            result.put("BITHUMB", tuple.getT2());
            return result;
        });
    }
}
