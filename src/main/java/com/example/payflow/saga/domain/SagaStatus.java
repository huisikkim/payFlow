package com.example.payflow.saga.domain;

public enum SagaStatus {
    STARTED,        // Saga 시작
    COMPENSATING,   // 보상 트랜잭션 진행 중
    COMPENSATED,    // 보상 완료
    COMPLETED,      // 정상 완료
    FAILED          // 실패 (보상 불가)
}
