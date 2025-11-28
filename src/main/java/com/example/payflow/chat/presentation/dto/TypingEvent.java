package com.example.payflow.chat.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 타이핑 인디케이터 이벤트 DTO
 * 사용자가 채팅 입력 중일 때 실시간으로 상태를 전달
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TypingEvent {
    
    /**
     * 채팅방 ID
     */
    private String roomId;
    
    /**
     * 입력 중인 사용자 ID (서버에서 자동 설정)
     */
    private String userId;
    
    /**
     * 입력 중인 사용자 이름 (선택사항)
     */
    private String userName;
    
    /**
     * 타이핑 상태
     * true: 입력 중
     * false: 입력 중단
     */
    @JsonProperty("isTyping")
    private boolean typing;
    
    /**
     * 이벤트 발생 시간 (서버에서 자동 설정)
     */
    private LocalDateTime timestamp;
}
