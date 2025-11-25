package com.example.payflow.crypto.application;

import com.example.payflow.crypto.domain.CoinTicker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
//@Service  // 🔒 코인 비교 기능 비활성화 - 나중에 다시 활성화하려면 주석 해제
public class UpbitWebSocketService extends TextWebSocketHandler {
    
    private static final String UPBIT_WS_URL = "wss://api.upbit.com/websocket/v1";
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                context.serialize(src.toString()))
            .create();
    
    // 구독 중인 마켓 코드
    private final Set<String> subscribedMarkets = new CopyOnWriteArraySet<>();
    
    // 최신 시세 데이터 캐시
    private final Map<String, CoinTicker> tickerCache = new ConcurrentHashMap<>();
    
    // 연결된 클라이언트 세션들
    private final Set<WebSocketSession> clientSessions = new CopyOnWriteArraySet<>();
    
    // 업비트 웹소켓 세션
    private WebSocketSession upbitSession;
    
    // 한글명 매핑
    private final Map<String, String> koreanNames = new HashMap<>();
    
    @PostConstruct
    public void init() {
        initKoreanNames();
        connectToUpbit();
    }
    
    private void initKoreanNames() {
        koreanNames.put("KRW-BTC", "비트코인");
        koreanNames.put("KRW-ETH", "이더리움");
        koreanNames.put("KRW-XRP", "리플");
        koreanNames.put("KRW-ADA", "에이다");
        koreanNames.put("KRW-SOL", "솔라나");
        koreanNames.put("KRW-DOGE", "도지코인");
        koreanNames.put("KRW-AVAX", "아발란체");
        koreanNames.put("KRW-MATIC", "폴리곤");
        koreanNames.put("KRW-DOT", "폴카닷");
        koreanNames.put("KRW-SHIB", "시바이누");
    }
    
    private void connectToUpbit() {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            
            // 기본 구독 마켓 설정
            List<String> defaultMarkets = Arrays.asList(
                "KRW-BTC", "KRW-ETH", "KRW-XRP", "KRW-ADA", "KRW-SOL",
                "KRW-DOGE", "KRW-AVAX", "KRW-MATIC", "KRW-DOT", "KRW-SHIB"
            );
            subscribedMarkets.addAll(defaultMarkets);
            
            upbitSession = client.execute(this, null, URI.create(UPBIT_WS_URL)).get();
            
            // 구독 메시지 전송
            sendSubscribeMessage();
            
            log.info("✅ 업비트 웹소켓 연결 성공");
        } catch (Exception e) {
            log.error("❌ 업비트 웹소켓 연결 실패", e);
        }
    }
    
    private void sendSubscribeMessage() {
        try {
            // 업비트 웹소켓 구독 메시지 형식
            List<Object> subscribeRequest = new ArrayList<>();
            
            // 1. ticket
            Map<String, String> ticket = new HashMap<>();
            ticket.put("ticket", UUID.randomUUID().toString());
            subscribeRequest.add(ticket);
            
            // 2. type과 codes
            Map<String, Object> type = new HashMap<>();
            type.put("type", "ticker");
            type.put("codes", new ArrayList<>(subscribedMarkets));
            subscribeRequest.add(type);
            
            String message = gson.toJson(subscribeRequest);
            
            log.info("📡 업비트 구독 메시지 전송: {}", message);
            
            upbitSession.sendMessage(new TextMessage(message));
            
            log.info("✅ 구독 완료: {}", subscribedMarkets);
        } catch (Exception e) {
            log.error("❌ 구독 메시지 전송 실패", e);
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 업비트로부터 받은 텍스트 메시지 처리
        if (session == upbitSession) {
            handleUpbitMessage(message.asBytes());
        }
    }
    
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        // 업비트는 바이너리 메시지로 데이터를 전송
        if (session == upbitSession) {
            byte[] payload = new byte[message.getPayload().remaining()];
            message.getPayload().get(payload);
            handleUpbitMessage(payload);
        }
    }
    
    private void handleUpbitMessage(byte[] payload) {
        try {
            String jsonString = new String(payload, StandardCharsets.UTF_8);
            
            log.debug("📨 업비트 메시지 수신: {}", jsonString);
            
            JsonObject json = gson.fromJson(jsonString, JsonObject.class);
            
            CoinTicker ticker = parseTicker(json);
            tickerCache.put(ticker.getMarket(), ticker);
            
            //log.info("💰 시세 업데이트: {} - {}원", ticker.getKoreanName(), ticker.getTradePrice());
            
            // 모든 클라이언트에게 브로드캐스트
            broadcastToClients(ticker);
            
        } catch (Exception e) {
            log.error("❌ 업비트 메시지 처리 실패", e);
        }
    }
    
    private CoinTicker parseTicker(JsonObject json) {
        String market = json.get("code").getAsString();
        
        return CoinTicker.builder()
            .market(market)
            .koreanName(koreanNames.getOrDefault(market, market))
            .tradePrice(json.get("trade_price").getAsBigDecimal())
            .changePrice(json.get("signed_change_price").getAsBigDecimal())
            .changeRate(json.get("change_rate").getAsBigDecimal().multiply(BigDecimal.valueOf(100)))
            .signedChangeRate(json.get("signed_change_rate").getAsBigDecimal().multiply(BigDecimal.valueOf(100)))
            .highPrice(json.get("high_price").getAsBigDecimal())
            .lowPrice(json.get("low_price").getAsBigDecimal())
            .openingPrice(json.get("opening_price").getAsBigDecimal())
            .prevClosingPrice(json.get("prev_closing_price").getAsBigDecimal())
            .accTradePrice24h(json.get("acc_trade_price_24h").getAsBigDecimal())
            .accTradeVolume24h(json.get("acc_trade_volume_24h").getAsBigDecimal())
            .change(json.get("change").getAsString())
            .timestamp(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(json.get("timestamp").getAsLong()),
                ZoneId.systemDefault()
            ))
            .build();
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
        log.info("✅ 클라이언트 연결: {} (총 {}명)", session.getId(), clientSessions.size());
        
        // 연결 시 캐시된 데이터 전송
        sendCachedData(session);
    }
    
    public void removeClientSession(WebSocketSession session) {
        clientSessions.remove(session);
        log.info("❌ 클라이언트 연결 해제: {} (총 {}명)", session.getId(), clientSessions.size());
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
            if (upbitSession != null && upbitSession.isOpen()) {
                upbitSession.close();
            }
            clientSessions.forEach(session -> {
                try {
                    session.close();
                } catch (Exception e) {
                    log.error("클라이언트 세션 종료 실패", e);
                }
            });
            log.info("✅ 업비트 웹소켓 서비스 종료");
        } catch (Exception e) {
            log.error("❌ 서비스 종료 실패", e);
        }
    }
}
