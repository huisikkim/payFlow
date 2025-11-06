package com.example.payflow.payment.domain;

public enum PaymentStatus {
    READY,      // 결제 준비
    DONE,       // 결제 완료
    CANCELED,   // 결제 취소
    FAILED      // 결제 실패
}
