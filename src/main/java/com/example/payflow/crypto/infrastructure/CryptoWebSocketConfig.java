package com.example.payflow.crypto.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class CryptoWebSocketConfig implements WebSocketConfigurer {
    
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
