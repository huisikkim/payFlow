package com.example.payflow.crypto.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

//@Configuration  // 🔒 코인 비교 기능 비활성화 - 나중에 다시 활성화하려면 주석 해제
//@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final CryptoWebSocketHandler cryptoWebSocketHandler;
    private final BithumbWebSocketHandler bithumbWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(cryptoWebSocketHandler, "/ws/crypto/upbit")
                .setAllowedOrigins("*");
        
        registry.addHandler(bithumbWebSocketHandler, "/ws/crypto/bithumb")
                .setAllowedOrigins("*");
        
        // 레거시 호환
        registry.addHandler(cryptoWebSocketHandler, "/ws/crypto")
                .setAllowedOrigins("*");
    }
}
