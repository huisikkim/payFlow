package com.example.payflow.groupbuying.application;

import com.example.payflow.catalog.domain.DistributorOrder;
import com.example.payflow.catalog.domain.DistributorOrderItem;
import com.example.payflow.catalog.domain.DistributorOrderRepository;
import com.example.payflow.catalog.domain.OrderStatus;
import com.example.payflow.groupbuying.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 공동구매 주문 생성 서비스
 * 방 마감 후 자동으로 각 참여자별 주문 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupBuyingOrderService {
    
    private final GroupBuyingRoomRepository roomRepository;
    private final GroupBuyingParticipantRepository participantRepository;
    private final DistributorOrderRepository distributorOrderRepository;
    private final GroupBuyingParticipantService participantService;
    
    /**
     * 공동구매 방 마감 후 주문 자동 생성
     */
    @Transactional
    public void createOrdersForClosedRoom(String roomId) {
        // 방 조회
        GroupBuyingRoom room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
        
        // 상태 확인
        if (room.getStatus() != RoomStatus.CLOSED_SUCCESS) {
            throw new IllegalStateException("성공적으로 마감된 방만 주문을 생성할 수 있습니다.");
        }
        
        // 참여자 확정
        participantService.confirmParticipants(room.getId());
        
        // 확정된 참여자 조회
        List<GroupBuyingParticipant> participants = participantRepository.findByRoomIdAndStatusOrderByJoinedAtAsc(
                room.getId(), ParticipantStatus.CONFIRMED);
        
        if (participants.isEmpty()) {
            log.warn("확정된 참여자가 없습니다: roomId={}", roomId);
            return;
        }
        
        // 각 참여자별로 주문 생성
        int successCount = 0;
        for (GroupBuyingParticipant participant : participants) {
            try {
                DistributorOrder order = createOrderForParticipant(room, participant);
                participant.completeOrder(order.getId());
                successCount++;
                
                log.info("공동구매 주문 생성 성공: roomId={}, storeId={}, orderId={}", 
                        roomId, participant.getStoreId(), order.getOrderNumber());
            } catch (Exception e) {
                log.error("공동구매 주문 생성 실패: roomId={}, storeId={}", 
                        roomId, participant.getStoreId(), e);
            }
        }
        
        // 방 상태 업데이트
        if (successCount > 0) {
            room.completeOrders();
            log.info("공동구매 주문 생성 완료: roomId={}, totalOrders={}, successOrders={}", 
                    roomId, participants.size(), successCount);
        }
    }
    
    /**
     * 참여자별 주문 생성
     */
    private DistributorOrder createOrderForParticipant(GroupBuyingRoom room, GroupBuyingParticipant participant) {
        // 주문 번호 생성
        String orderNumber = generateOrderNumber();
        
        // 주문 생성
        DistributorOrder order = DistributorOrder.builder()
                .storeId(participant.getStoreId())
                .distributorId(room.getDistributorId())
                .orderNumber(orderNumber)
                .totalAmount(participant.getTotalAmount())
                .totalQuantity(participant.getQuantity())
                .status(OrderStatus.PENDING)
                .deliveryAddress(participant.getDeliveryAddress())
                .deliveryPhone(participant.getDeliveryPhone())
                .deliveryRequest(participant.getDeliveryRequest())
                .desiredDeliveryDate(room.getExpectedDeliveryDate())
                .orderedAt(LocalDateTime.now())
                .build();
        
        // 주문 아이템 생성
        DistributorOrderItem orderItem = DistributorOrderItem.builder()
                .productId(room.getProductId())
                .productName(room.getProductName())
                .quantity(participant.getQuantity())
                .unitPrice(participant.getUnitPrice())
                .subtotal(participant.getTotalProductAmount())
                .unit(room.getUnit())
                .imageUrl(room.getImageUrl())
                .build();
        
        order.addItem(orderItem);
        
        return distributorOrderRepository.save(order);
    }
    
    /**
     * 주문 번호 생성
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return "GB-" + timestamp + "-" + random;
    }
    
    /**
     * 성공적으로 마감된 방들의 주문 자동 생성 (스케줄러용)
     */
    @Transactional
    public void createOrdersForSuccessfulRooms() {
        List<GroupBuyingRoom> successfulRooms = roomRepository.findByStatusOrderByCreatedAtDesc(
                RoomStatus.CLOSED_SUCCESS);
        
        for (GroupBuyingRoom room : successfulRooms) {
            try {
                createOrdersForClosedRoom(room.getRoomId());
            } catch (Exception e) {
                log.error("공동구매 주문 자동 생성 실패: roomId={}", room.getRoomId(), e);
            }
        }
    }
}
