package com.example.payflow.escrow.domain;

public enum EscrowStatus {
    INITIATED,              // 거래 시작
    DEPOSITED,             // 입금 완료
    DELIVERED,             // 차량 인도 완료
    VERIFIED,              // 차량 검증 완료
    OWNERSHIP_TRANSFERRED, // 명의 이전 완료
    SETTLING,              // 정산 진행 중
    COMPLETED,             // 거래 완료
    VERIFICATION_FAILED,   // 검증 실패
    SETTLEMENT_FAILED,     // 정산 실패
    CANCELLED,             // 거래 취소
    DISPUTED               // 분쟁 중
}
