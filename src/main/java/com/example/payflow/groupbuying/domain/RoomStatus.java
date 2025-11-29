package com.example.payflow.groupbuying.domain;

/**
 * 공동구매 방 상태
 */
public enum RoomStatus {
    WAITING,         // 대기 중 (생성됨, 아직 오픈 전)
    OPEN,            // 오픈 (참여 가능)
    CLOSED_SUCCESS,  // 마감 성공 (목표 달성)
    CLOSED_FAILED,   // 마감 실패 (목표 미달)
    ORDER_CREATED,   // 주문 생성 완료
    COMPLETED,       // 완료 (배송 완료)
    CANCELLED        // 취소됨
}
