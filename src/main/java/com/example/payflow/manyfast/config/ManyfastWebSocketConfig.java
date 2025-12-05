package com.example.payflow.manyfast.config;

import com.example.payflow.manyfast.websocket.CollaborationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration("manyfastWebSocketConfig")
@EnableWebSocket
@RequiredArgsConstructor
public class ManyfastWebSocketConfig implements WebSocketConfigurer {
    
    private final CollaborationWebSocketHandler collaborationWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(collaborationWebSocketHandler, "/ws/collaboration")
            .setAllowedOrigins("*");
    }
}
