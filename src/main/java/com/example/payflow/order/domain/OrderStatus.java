package com.example.payflow.order.domain;

public enum OrderStatus {
    PENDING,      // 주문 생성됨
    CONFIRMED,    // 결제 완료
    CANCELLED,    // 주문 취소
    FAILED        // 결제 실패
}
