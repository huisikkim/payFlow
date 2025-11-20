package com.example.payflow.crypto.infrastructure;

import com.example.payflow.crypto.application.UpbitWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class CryptoWebSocketHandler extends TextWebSocketHandler {
    
    private final UpbitWebSocketService upbitWebSocketService;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        upbitWebSocketService.addClientSession(session);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 클라이언트로부터의 메시지 처리 (필요시)
        log.debug("클라이언트 메시지: {}", message.getPayload());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        upbitWebSocketService.removeClientSession(session);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("웹소켓 전송 에러", exception);
        upbitWebSocketService.removeClientSession(session);
    }
}
