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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 업비트 캔들 데이터 조회 클라이언트
 */
@Slf4j
//@Component  // 🔒 코인 비교 기능 비활성화 - 나중에 다시 활성화하려면 주석 해제
public class UpbitCandleClient {
    
    private static final String UPBIT_API_URL = "https://api.upbit.com/v1/candles/minutes/1";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    
    /**
     * 1분봉 캔들 데이터 조회
     * @param market 마켓 코드 (예: KRW-BTC)
     * @param count 조회할 캔들 개수
     * @return 캔들 데이터 리스트 (과거 -> 현재 순서)
     */
    public List<CandleData> getMinuteCandles(String market, int count) {
        try {
            String url = String.format("%s?market=%s&count=%d", UPBIT_API_URL, market, count);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseCandles(market, response.body());
            } else {
                log.error("❌ 업비트 캔들 조회 실패: {} - {}", response.statusCode(), response.body());
                return Collections.emptyList();
            }
            
        } catch (Exception e) {
            log.error("❌ 업비트 캔들 조회 오류: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private List<CandleData> parseCandles(String market, String jsonResponse) {
        List<CandleData> candles = new ArrayList<>();
        
        try {
            JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);
            
            // 업비트는 최신 -> 과거 순서로 반환하므로 역순으로 저장
            for (int i = jsonArray.size() - 1; i >= 0; i--) {
                JsonObject json = jsonArray.get(i).getAsJsonObject();
                
                CandleData candle = CandleData.builder()
                        .market(market)
                        .timestamp(LocalDateTime.parse(
                                json.get("candle_date_time_kst").getAsString(),
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .openPrice(json.get("opening_price").getAsBigDecimal())
                        .highPrice(json.get("high_price").getAsBigDecimal())
                        .lowPrice(json.get("low_price").getAsBigDecimal())
                        .closePrice(json.get("trade_price").getAsBigDecimal())
                        .volume(json.get("candle_acc_trade_volume").getAsBigDecimal())
                        .build();
                
                candles.add(candle);
            }
            
            log.debug("✅ 업비트 캔들 {}개 조회 완료: {}", candles.size(), market);
            
        } catch (Exception e) {
            log.error("❌ 업비트 캔들 파싱 실패: {}", e.getMessage());
        }
        
        return candles;
    }
}
