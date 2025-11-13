package com.example.payflow.escrow.domain;

/**
 * 가상계좌 상태
 */
public enum VirtualAccountStatus {
    WAITING_FOR_DEPOSIT,  // 입금 대기
    DONE,                 // 입금 완료
    CANCELED,             // 취소됨
    EXPIRED               // 기한 만료
}
