package com.example.payflow.crypto.application;

import com.example.payflow.crypto.domain.CoinTicker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Service
public class BithumbWebSocketService extends TextWebSocketHandler {
    
    private static final String BITHUMB_WS_URL = "wss://pubwss.bithumb.com/pub/ws";
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                context.serialize(src.toString()))
            .create();
    
    // 구독 중인 마켓 코드
    private final Set<String> subscribedMarkets = new CopyOnWriteArraySet<>();
    
    // 최신 시세 데이터 캐시 (KRW-BTC 형식으로 저장)
    private final Map<String, CoinTicker> tickerCache = new ConcurrentHashMap<>();
    
    // 연결된 클라이언트 세션들
    private final Set<WebSocketSession> clientSessions = new CopyOnWriteArraySet<>();
    
    // 빗썸 웹소켓 세션
    private WebSocketSession bithumbSession;
    
    // 한글명 매핑
    private final Map<String, String> koreanNames = new HashMap<>();
    
    // 빗썸 -> 업비트 심볼 매핑
    private final Map<String, String> symbolMapping = new HashMap<>();
    
    @PostConstruct
    public void init() {
        initMappings();
        connectToBithumb();
    }
    
    private void initMappings() {
        // 한글명
        koreanNames.put("BTC", "비트코인");
        koreanNames.put("ETH", "이더리움");
        koreanNames.put("XRP", "리플");
        koreanNames.put("ADA", "에이다");
        koreanNames.put("SOL", "솔라나");
        koreanNames.put("DOGE", "도지코인");
        koreanNames.put("AVAX", "아발란체");
        koreanNames.put("MATIC", "폴리곤");
        koreanNames.put("DOT", "폴카닷");
        koreanNames.put("SHIB", "시바이누");
        
        // 빗썸 심볼 -> 업비트 형식 매핑
        symbolMapping.put("BTC", "KRW-BTC");
        symbolMapping.put("ETH", "KRW-ETH");
        symbolMapping.put("XRP", "KRW-XRP");
        symbolMapping.put("ADA", "KRW-ADA");
        symbolMapping.put("SOL", "KRW-SOL");
        symbolMapping.put("DOGE", "KRW-DOGE");
        symbolMapping.put("AVAX", "KRW-AVAX");
        symbolMapping.put("MATIC", "KRW-MATIC");
        symbolMapping.put("DOT", "KRW-DOT");
        symbolMapping.put("SHIB", "KRW-SHIB");
    }
    
    private void connectToBithumb() {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            
            // 기본 구독 마켓 설정
            subscribedMarkets.addAll(Arrays.asList(
                "BTC", "ETH", "XRP", "ADA", "SOL",
                "DOGE", "AVAX", "MATIC", "DOT", "SHIB"
            ));
            
            bithumbSession = client.execute(this, null, URI.create(BITHUMB_WS_URL)).get();
            
            // 구독 메시지 전송
            sendSubscribeMessage();
            
            log.info("✅ 빗썸 웹소켓 연결 성공");
        } catch (Exception e) {
            log.error("❌ 빗썸 웹소켓 연결 실패", e);
        }
    }
    
    private void sendSubscribeMessage() {
        try {
            // 빗썸 웹소켓 구독 메시지 형식
            // {"type":"ticker","symbols":["BTC_KRW","ETH_KRW"],"tickTypes":["24H"]}
            List<String> symbols = new ArrayList<>();
            for (String market : subscribedMarkets) {
                symbols.add(market + "_KRW");
            }
            
            Map<String, Object> subscribeRequest = new HashMap<>();
            subscribeRequest.put("type", "ticker");
            subscribeRequest.put("symbols", symbols);
            subscribeRequest.put("tickTypes", Arrays.asList("24H"));  // 24시간 기준
            
            String message = gson.toJson(subscribeRequest);
            
            log.info("📡 빗썸 구독 메시지 전송: {}", message);
            
            bithumbSession.sendMessage(new TextMessage(message));
            
            log.info("✅ 빗썸 구독 완료: {}", subscribedMarkets);
        } catch (Exception e) {
            log.error("❌ 빗썸 구독 메시지 전송 실패", e);
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        if (session == bithumbSession) {
            handleBithumbMessage(message.getPayload().getBytes(StandardCharsets.UTF_8));
        }
    }
    
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        if (session == bithumbSession) {
            byte[] payload = new byte[message.getPayload().remaining()];
            message.getPayload().get(payload);
            handleBithumbMessage(payload);
        }
    }
    
    private void handleBithumbMessage(byte[] payload) {
        try {
            String jsonString = new String(payload, StandardCharsets.UTF_8);
            
            log.debug("📨 빗썸 메시지 수신: {}", jsonString);
            
            JsonObject json = gson.fromJson(jsonString, JsonObject.class);
            
            // 연결 성공 메시지 확인
            if (json.has("status")) {
                String status = json.get("status").getAsString();
                String resmsg = json.get("resmsg").getAsString();
                log.info("📡 빗썸 응답: {} - {}", status, resmsg);
                return;
            }
            
            // 빗썸 응답 타입 확인
            if (json.has("type") && "ticker".equals(json.get("type").getAsString())) {
                JsonObject content = json.getAsJsonObject("content");
                
                CoinTicker ticker = parseBithumbTicker(content);
                if (ticker != null) {
                    tickerCache.put(ticker.getMarket(), ticker);
                    
                    //log.info("💰 빗썸 시세 업데이트: {} - {}원", ticker.getKoreanName(), ticker.getTradePrice());
                    
                    // 모든 클라이언트에게 브로드캐스트
                    broadcastToClients(ticker);
                }
            }
            
        } catch (Exception e) {
            log.error("❌ 빗썸 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }
    
    private CoinTicker parseBithumbTicker(JsonObject content) {
        try {
            // 필수 필드 확인
            if (!content.has("symbol") || !content.has("closePrice")) {
                log.warn("⚠️ 빗썸 데이터에 필수 필드 없음: {}", content);
                return null;
            }
            
            // 빗썸 심볼 형식: BTC_KRW -> BTC
            String symbolFull = content.get("symbol").getAsString();
            String symbol = symbolFull.replace("_KRW", "");
            String market = symbolMapping.getOrDefault(symbol, "KRW-" + symbol);
            
            // 빗썸 데이터 파싱 (안전하게)
            BigDecimal closePrice = getDecimalValue(content, "closePrice");
            BigDecimal openPrice = getDecimalValue(content, "openPrice");
            BigDecimal highPrice = getDecimalValue(content, "highPrice");
            BigDecimal lowPrice = getDecimalValue(content, "lowPrice");
            BigDecimal prevClosePrice = getDecimalValue(content, "prevClosePrice");
            BigDecimal volume = getDecimalValue(content, "volume");
            BigDecimal value = getDecimalValue(content, "value");
            
            // 변동가 및 변동률 계산
            BigDecimal changePrice = closePrice.subtract(prevClosePrice);
            BigDecimal changeRate = prevClosePrice.compareTo(BigDecimal.ZERO) != 0 
                ? changePrice.divide(prevClosePrice, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
            
            String change = changePrice.compareTo(BigDecimal.ZERO) > 0 ? "RISE" 
                          : changePrice.compareTo(BigDecimal.ZERO) < 0 ? "FALL" 
                          : "EVEN";
            
            return CoinTicker.builder()
                .market(market)
                .koreanName(koreanNames.getOrDefault(symbol, symbol))
                .tradePrice(closePrice)
                .changePrice(changePrice)
                .changeRate(changeRate.abs())
                .signedChangeRate(changeRate)
                .highPrice(highPrice)
                .lowPrice(lowPrice)
                .openingPrice(openPrice)
                .prevClosingPrice(prevClosePrice)
                .accTradePrice24h(value)
                .accTradeVolume24h(volume)
                .change(change)
                .timestamp(LocalDateTime.now())
                .build();
        } catch (Exception e) {
            log.error("❌ 빗썸 티커 파싱 실패: {}", e.getMessage());
            return null;
        }
    }
    
    private BigDecimal getDecimalValue(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            try {
                return new BigDecimal(json.get(key).getAsString());
            } catch (Exception e) {
                log.warn("⚠️ 필드 파싱 실패: {} = {}", key, json.get(key));
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
    
    private void broadcastToClients(CoinTicker ticker) {
        String message = gson.toJson(ticker);
        
        clientSessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (Exception e) {
                log.error("❌ 클라이언트 메시지 전송 실패", e);
            }
        });
    }
    
    public void addClientSession(WebSocketSession session) {
        clientSessions.add(session);
        log.info("✅ 빗썸 클라이언트 연결: {} (총 {}명)", session.getId(), clientSessions.size());
        
        // 연결 시 캐시된 데이터 전송
        sendCachedData(session);
    }
    
    public void removeClientSession(WebSocketSession session) {
        clientSessions.remove(session);
        log.info("❌ 빗썸 클라이언트 연결 해제: {} (총 {}명)", session.getId(), clientSessions.size());
    }
    
    private void sendCachedData(WebSocketSession session) {
        tickerCache.values().forEach(ticker -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(gson.toJson(ticker)));
                }
            } catch (Exception e) {
                log.error("❌ 캐시 데이터 전송 실패", e);
            }
        });
    }
    
    public List<CoinTicker> getAllTickers() {
        return new ArrayList<>(tickerCache.values());
    }
    
    public CoinTicker getTicker(String market) {
        return tickerCache.get(market);
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (bithumbSession != null && bithumbSession.isOpen()) {
                bithumbSession.close();
            }
            clientSessions.forEach(session -> {
                try {
                    session.close();
                } catch (Exception e) {
                    log.error("클라이언트 세션 종료 실패", e);
                }
            });
            log.info("✅ 빗썸 웹소켓 서비스 종료");
        } catch (Exception e) {
            log.error("❌ 서비스 종료 실패", e);
        }
    }
}
