package com.example.payflow.crypto.application;

import com.example.payflow.crypto.domain.OrderbookData;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 호가 데이터 수집 서비스
 */
@Slf4j
@Service
public class OrderbookService {
    
    private final WebClient webClient;
    private final Gson gson = new Gson();
    
    // 호가 데이터 캐시
    private final ConcurrentHashMap<String, OrderbookData> orderbookCache = new ConcurrentHashMap<>();
    
    public OrderbookService() {
        this.webClient = WebClient.builder().build();
    }
    
    /**
     * 업비트 호가 조회
     */
    public Mono<OrderbookData> getUpbitOrderbook(String market) {
        String url = "https://api.upbit.com/v1/orderbook?markets=" + market;
        
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> {
                try {
                    JsonArray jsonArray = gson.fromJson(response, JsonArray.class);
                    if (jsonArray.size() > 0) {
                        JsonObject data = jsonArray.get(0).getAsJsonObject();
                        return parseUpbitOrderbook(data, market);
                    }
                } catch (Exception e) {
                    log.error("업비트 호가 파싱 실패: {}", e.getMessage());
                }
                return null;
            })
            .doOnNext(orderbook -> {
                if (orderbook != null) {
                    orderbookCache.put("UPBIT:" + market, orderbook);
                }
            });
    }
    
    /**
     * 빗썸 호가 조회
     */
    public Mono<OrderbookData> getBithumbOrderbook(String market) {
        // KRW-BTC -> BTC_KRW
        String symbol = market.replace("KRW-", "") + "_KRW";
        String url = "https://api.bithumb.com/public/orderbook/" + symbol;
        
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> {
                try {
                    JsonObject json = gson.fromJson(response, JsonObject.class);
                    if ("0000".equals(json.get("status").getAsString())) {
                        JsonObject data = json.getAsJsonObject("data");
                        return parseBithumbOrderbook(data, market);
                    }
                } catch (Exception e) {
                    log.error("빗썸 호가 파싱 실패: {}", e.getMessage());
                }
                return null;
            })
            .doOnNext(orderbook -> {
                if (orderbook != null) {
                    orderbookCache.put("BITHUMB:" + market, orderbook);
                }
            });
    }
    
    /**
     * 캐시된 호가 데이터 조회
     */
    public OrderbookData getCachedOrderbook(String exchange, String market) {
        return orderbookCache.get(exchange + ":" + market);
    }
    
    private OrderbookData parseUpbitOrderbook(JsonObject data, String market) {
        JsonArray orderbookUnits = data.getAsJsonArray("orderbook_units");
        
        List<OrderbookData.OrderbookUnit> bids = new ArrayList<>();
        List<OrderbookData.OrderbookUnit> asks = new ArrayList<>();
        
        for (int i = 0; i < Math.min(10, orderbookUnits.size()); i++) {
            JsonObject unit = orderbookUnits.get(i).getAsJsonObject();
            
            // 매수 호가
            BigDecimal bidPrice = unit.get("bid_price").getAsBigDecimal();
            BigDecimal bidSize = unit.get("bid_size").getAsBigDecimal();
            bids.add(OrderbookData.OrderbookUnit.builder()
                .price(bidPrice)
                .quantity(bidSize)
                .total(bidPrice.multiply(bidSize))
                .build());
            
            // 매도 호가
            BigDecimal askPrice = unit.get("ask_price").getAsBigDecimal();
            BigDecimal askSize = unit.get("ask_size").getAsBigDecimal();
            asks.add(OrderbookData.OrderbookUnit.builder()
                .price(askPrice)
                .quantity(askSize)
                .total(askPrice.multiply(askSize))
                .build());
        }
        
        BigDecimal bestBid = bids.isEmpty() ? BigDecimal.ZERO : bids.get(0).getPrice();
        BigDecimal bestAsk = asks.isEmpty() ? BigDecimal.ZERO : asks.get(0).getPrice();
        BigDecimal spread = bestAsk.subtract(bestBid);
        BigDecimal spreadPercent = bestBid.compareTo(BigDecimal.ZERO) > 0
            ? spread.divide(bestBid, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        return OrderbookData.builder()
            .exchange("UPBIT")
            .market(market)
            .bestBidPrice(bestBid)
            .bestAskPrice(bestAsk)
            .spread(spread)
            .spreadPercent(spreadPercent)
            .bids(bids)
            .asks(asks)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    private OrderbookData parseBithumbOrderbook(JsonObject data, String market) {
        JsonArray bidsArray = data.getAsJsonArray("bids");
        JsonArray asksArray = data.getAsJsonArray("asks");
        
        List<OrderbookData.OrderbookUnit> bids = new ArrayList<>();
        List<OrderbookData.OrderbookUnit> asks = new ArrayList<>();
        
        // 매수 호가
        for (int i = 0; i < Math.min(10, bidsArray.size()); i++) {
            JsonObject unit = bidsArray.get(i).getAsJsonObject();
            BigDecimal price = new BigDecimal(unit.get("price").getAsString());
            BigDecimal quantity = new BigDecimal(unit.get("quantity").getAsString());
            bids.add(OrderbookData.OrderbookUnit.builder()
                .price(price)
                .quantity(quantity)
                .total(price.multiply(quantity))
                .build());
        }
        
        // 매도 호가
        for (int i = 0; i < Math.min(10, asksArray.size()); i++) {
            JsonObject unit = asksArray.get(i).getAsJsonObject();
            BigDecimal price = new BigDecimal(unit.get("price").getAsString());
            BigDecimal quantity = new BigDecimal(unit.get("quantity").getAsString());
            asks.add(OrderbookData.OrderbookUnit.builder()
                .price(price)
                .quantity(quantity)
                .total(price.multiply(quantity))
                .build());
        }
        
        BigDecimal bestBid = bids.isEmpty() ? BigDecimal.ZERO : bids.get(0).getPrice();
        BigDecimal bestAsk = asks.isEmpty() ? BigDecimal.ZERO : asks.get(0).getPrice();
        BigDecimal spread = bestAsk.subtract(bestBid);
        BigDecimal spreadPercent = bestBid.compareTo(BigDecimal.ZERO) > 0
            ? spread.divide(bestBid, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        return OrderbookData.builder()
            .exchange("BITHUMB")
            .market(market)
            .bestBidPrice(bestBid)
            .bestAskPrice(bestAsk)
            .spread(spread)
            .spreadPercent(spreadPercent)
            .bids(bids)
            .asks(asks)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
