package com.example.payflow.settlement.domain;

public enum SettlementStatus {
    PENDING,      // 정산 대기
    PROCESSING,   // 정산 처리 중
    COMPLETED,    // 정산 완료
    FAILED        // 정산 실패
}
