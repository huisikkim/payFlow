package com.example.payflow.sessionreplay.application;

/**
 * 이벤트 직렬화 예외
 */
public class EventSerializationException extends RuntimeException {
    
    public EventSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
