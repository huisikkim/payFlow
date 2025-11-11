package com.example.payflow.sessionreplay.application;

/**
 * 세션을 찾을 수 없을 때 발생하는 예외
 */
public class SessionNotFoundException extends RuntimeException {
    
    public SessionNotFoundException(String message) {
        super(message);
    }
}
