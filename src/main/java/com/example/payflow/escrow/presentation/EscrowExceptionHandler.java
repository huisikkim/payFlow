package com.example.payflow.escrow.presentation;

import com.example.payflow.escrow.domain.*;
import com.example.payflow.escrow.presentation.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class EscrowExceptionHandler {
    
    @ExceptionHandler(InvalidEscrowStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidEscrowStateException e) {
        log.error("잘못된 에스크로 상태: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("INVALID_STATE", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(EscrowNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EscrowNotFoundException e) {
        log.error("에스크로 거래를 찾을 수 없음: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("NOT_FOUND", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(DepositAmountMismatchException.class)
    public ResponseEntity<ErrorResponse> handleDepositMismatch(DepositAmountMismatchException e) {
        log.error("입금 금액 불일치: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("DEPOSIT_AMOUNT_MISMATCH", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(VerificationFailedException.class)
    public ResponseEntity<ErrorResponse> handleVerificationFailed(VerificationFailedException e) {
        log.error("검증 실패: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("VERIFICATION_FAILED", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(SettlementFailedException.class)
    public ResponseEntity<ErrorResponse> handleSettlementFailed(SettlementFailedException e) {
        log.error("정산 실패: {}", e.getMessage(), e);
        ErrorResponse response = new ErrorResponse("SETTLEMENT_FAILED", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        log.error("잘못된 상태: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("ILLEGAL_STATE", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.error("잘못된 인자: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("INVALID_ARGUMENT", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("예상치 못한 오류 발생", e);
        ErrorResponse response = new ErrorResponse("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
