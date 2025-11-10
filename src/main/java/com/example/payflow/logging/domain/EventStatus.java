package com.example.payflow.logging.domain;

/**
 * 이벤트 처리 상태
 */
public enum EventStatus {
    SUCCESS,      // 성공
    FAILED,       // 실패
    PROCESSING,   // 처리 중
    COMPENSATED   // 보상 트랜잭션 완료
}
