package com.example.payflow.crypto.infrastructure;

import com.example.payflow.crypto.domain.CandleData;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 빗썸 캔들 데이터 조회 클라이언트
 */
@Slf4j
@Component
public class BithumbCandleClient {
    
    private static final String BITHUMB_API_URL = "https://api.bithumb.com/public/candlestick";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    
    /**
     * 1분봉 캔들 데이터 조회
     * @param symbol 심볼 (예: BTC)
     * @param count 조회할 캔들 개수 (최대 100개)
     * @return 캔들 데이터 리스트 (과거 -> 현재 순서)
     */
    public List<CandleData> getMinuteCandles(String symbol, int count) {
        try {
            String url = String.format("%s/%s_KRW/1m", BITHUMB_API_URL, symbol);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseCandles(symbol, response.body(), count);
            } else {
                log.error("❌ 빗썸 캔들 조회 실패: {} - {}", response.statusCode(), response.body());
                return Collections.emptyList();
            }
            
        } catch (Exception e) {
            log.error("❌ 빗썸 캔들 조회 오류: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private List<CandleData> parseCandles(String symbol, String jsonResponse, int count) {
        List<CandleData> candles = new ArrayList<>();
        
        try {
            JsonObject response = gson.fromJson(jsonResponse, JsonObject.class);
            
            if (!"0000".equals(response.get("status").getAsString())) {
                log.error("❌ 빗썸 API 오류: {}", response.get("message").getAsString());
                return Collections.emptyList();
            }
            
            JsonArray data = response.getAsJsonArray("data");
            
            // 최근 count개만 가져오기 (빗썸은 과거 -> 현재 순서)
            int startIndex = Math.max(0, data.size() - count);
            
            for (int i = startIndex; i < data.size(); i++) {
                JsonArray candleArray = data.get(i).getAsJsonArray();
                
                // [timestamp, open, close, high, low, volume]
                CandleData candle = CandleData.builder()
                        .market("KRW-" + symbol)
                        .timestamp(LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(candleArray.get(0).getAsLong()),
                                ZoneId.systemDefault()))
                        .openPrice(new BigDecimal(candleArray.get(1).getAsString()))
                        .closePrice(new BigDecimal(candleArray.get(2).getAsString()))
                        .highPrice(new BigDecimal(candleArray.get(3).getAsString()))
                        .lowPrice(new BigDecimal(candleArray.get(4).getAsString()))
                        .volume(new BigDecimal(candleArray.get(5).getAsString()))
                        .build();
                
                candles.add(candle);
            }
            
            log.debug("✅ 빗썸 캔들 {}개 조회 완료: {}", candles.size(), symbol);
            
        } catch (Exception e) {
            log.error("❌ 빗썸 캔들 파싱 실패: {}", e.getMessage(), e);
        }
        
        return candles;
    }
}
