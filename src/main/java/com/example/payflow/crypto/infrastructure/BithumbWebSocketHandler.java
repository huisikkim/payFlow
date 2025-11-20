package com.example.payflow.crypto.infrastructure;

import com.example.payflow.crypto.application.BithumbWebSocketService;
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
public class BithumbWebSocketHandler extends TextWebSocketHandler {
    
    private final BithumbWebSocketService bithumbWebSocketService;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        bithumbWebSocketService.addClientSession(session);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("빗썸 클라이언트 메시지: {}", message.getPayload());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        bithumbWebSocketService.removeClientSession(session);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("빗썸 웹소켓 전송 에러", exception);
        bithumbWebSocketService.removeClientSession(session);
    }
}
