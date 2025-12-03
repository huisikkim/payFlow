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
                
                if (accessor != null) {
                    StompCommand command = accessor.getCommand();
                    
                    // CONNECT 시 JWT 토큰 검증 및 인증 정보 설정
                    if (StompCommand.CONNECT.equals(command)) {
                        // YouTube 익명 채팅은 인증 불필요 (X-Anonymous-User 헤더로 판단)
                        String anonymousUser = accessor.getFirstNativeHeader("X-Anonymous-User");
                        if (anonymousUser != null && !anonymousUser.isEmpty()) {
                            accessor.getSessionAttributes().put("anonymousUser", anonymousUser);
                            accessor.getSessionAttributes().put("isAnonymous", true);
                            log.info("YouTube 익명 채팅 연결 - user: {}", anonymousUser);
                            return message;
                        }
                        
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
                                    
                                    // 세션에 사용자 정보와 권한 저장
                                    accessor.getSessionAttributes().put("username", username);
                                    accessor.getSessionAttributes().put("authorities", authorities);
                                    
                                    log.info("WebSocket 연결 인증 성공 - username: {}, roles: {}", username, roles);
                                } else {
                                    log.error("WebSocket 연결 실패 - 유효하지 않은 토큰");
                                    return null;  // 연결 거부
                                }
                            } catch (Exception e) {
                                log.error("WebSocket 인증 실패: {}", e.getMessage());
                                return null;  // 연결 거부
                            }
                        } else {
                            log.error("WebSocket 연결 실패 - Authorization 헤더 없음");
                            return null;  // 연결 거부
                        }
                    }
                    
                    // SEND, SUBSCRIBE 등 다른 명령어는 이미 설정된 User 정보 사용
                    if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
                        // YouTube 익명 채팅은 인증 체크 스킵
                        Boolean isAnonymous = (Boolean) accessor.getSessionAttributes().get("isAnonymous");
                        if (isAnonymous != null && isAnonymous) {
                            log.debug("{} 명령 - 익명 사용자", command);
                            return message;
                        }
                        
                        if (accessor.getUser() == null) {
                            log.error("인증되지 않은 사용자의 {} 시도", command);
                            return null;  // 요청 거부
                        }
                        log.debug("{} 명령 - 인증된 사용자: {}", command, accessor.getUser().getName());
                    }
                }
                
                return message;
            }
        });
    }
}
