package com.example.payflow.groupbuying.domain;

/**
 * 참여자 상태
 */
public enum ParticipantStatus {
    JOINED,         // 참여 완료 (방이 아직 진행 중)
    CONFIRMED,      // 확정됨 (방 마감 성공)
    ORDER_CREATED,  // 주문 생성됨
    DELIVERED,      // 배송 완료
    CANCELLED       // 취소됨
}
