package com.example.payflow.groupbuying.domain;

/**
 * 배송비 타입
 */
public enum DeliveryFeeType {
    FREE,      // 무료 배송
    FIXED,     // 고정 배송비 (가게당)
    SHARED     // 분담 배송비 (총 배송비를 참여자 수로 나눔)
}
