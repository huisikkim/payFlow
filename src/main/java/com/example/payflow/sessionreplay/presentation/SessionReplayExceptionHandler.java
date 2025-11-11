package com.example.payflow.sessionreplay.presentation;

import com.example.payflow.sessionreplay.application.EventSerializationException;
import com.example.payflow.sessionreplay.application.SessionNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Session Replay 예외 처리 핸들러
 */
@RestControllerAdvice(basePackages = "com.example.payflow.sessionreplay")
@Slf4j
public class SessionReplayExceptionHandler {
    
    /**
     * 세션을 찾을 수 없을 때
     */
    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotFound(SessionNotFoundException e) {
        log.warn("Session not found: {}", e.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "SESSION_NOT_FOUND",
            e.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * 이벤트 직렬화 실패
     */
    @ExceptionHandler(EventSerializationException.class)
    public ResponseEntity<ErrorResponse> handleEventSerializationError(EventSerializationException e) {
        log.error("Event serialization failed: {}", e.getMessage(), e);
        
        ErrorResponse error = new ErrorResponse(
            "EVENT_SERIALIZATION_ERROR",
            "Failed to process event data"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * 요청 검증 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException e) {
        log.warn("Validation error: {}", e.getMessage());
        
        String errors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            "Invalid request: " + errors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralError(Exception e) {
        log.error("Unexpected error occurred", e);
        
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
