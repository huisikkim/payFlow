package com.example.payflow.groupbuying.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공동구매 참여 요청
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRoomRequest {
    
    private String roomId; // 방 ID
    private String storeId; // 가게 ID
    private Integer quantity; // 주문 수량
    
    private String deliveryAddress; // 배송 주소 (null이면 가게 주소 사용)
    private String deliveryPhone; // 배송 연락처 (null이면 가게 연락처 사용)
    private String deliveryRequest; // 배송 요청사항
}
