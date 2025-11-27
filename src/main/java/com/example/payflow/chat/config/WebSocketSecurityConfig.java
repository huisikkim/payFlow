package com.example.payflow.chat.config;

import com.example.payflow.security.infrastructure.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        
                        try {
                            if (jwtTokenProvider.validateToken(token)) {
                                String username = jwtTokenProvider.getUsernameFromToken(token);
                                List<String> roles = jwtTokenProvider.getRolesFromToken(token);
                                
                                List<SimpleGrantedAuthority> authorities = roles.stream()
                                        .map(SimpleGrantedAuthority::new)
                                        .toList();
                                
                                Authentication auth = new UsernamePasswordAuthenticationToken(
                                        username, null, authorities);
                                
                                accessor.setUser(auth);
                                
                                // 세션에 권한 정보 저장
                                accessor.getSessionAttributes().put("authorities", authorities);
                                
                                log.info("WebSocket 인증 성공: {}", username);
                            }
                        } catch (Exception e) {
                            log.error("WebSocket 인증 실패: {}", e.getMessage());
                        }
                    }
                }
                
                return message;
            }
        });
    }
}
