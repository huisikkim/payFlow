package com.example.payflow.groupbuying.application;

import com.example.payflow.groupbuying.domain.*;
import com.example.payflow.store.domain.Store;
import com.example.payflow.store.domain.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공동구매 참여 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupBuyingParticipantService {
    
    private final GroupBuyingRoomRepository roomRepository;
    private final GroupBuyingParticipantRepository participantRepository;
    private final StoreRepository storeRepository;
    
    /**
     * 공동구매 참여 (가게)
     */
    @Transactional
    public GroupBuyingParticipant joinRoom(JoinRoomRequest request) {
        // 방 조회
        GroupBuyingRoom room = roomRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
        
        // 가게 조회
        Store store = storeRepository.findByStoreId(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));
        
        // 참여 가능 여부 확인
        if (!room.canJoin()) {
            throw new IllegalStateException("참여할 수 없는 방입니다.");
        }
        
        // 중복 참여 확인
        if (participantRepository.existsByRoomIdAndStoreId(room.getId(), request.getStoreId())) {
            throw new IllegalStateException("이미 참여한 방입니다.");
        }
        
        // 주문 수량 검증
        if (request.getQuantity() < room.getMinOrderPerStore()) {
            throw new IllegalArgumentException("최소 주문 수량은 " + room.getMinOrderPerStore() + room.getUnit() + " 입니다.");
        }
        
        if (room.getMaxOrderPerStore() != null && request.getQuantity() > room.getMaxOrderPerStore()) {
            throw new IllegalArgumentException("최대 주문 수량은 " + room.getMaxOrderPerStore() + room.getUnit() + " 입니다.");
        }
        
        // 참여자 생성
        GroupBuyingParticipant participant = GroupBuyingParticipant.builder()
                .storeId(request.getStoreId())
                .storeName(store.getStoreName())
                .storeRegion(store.getRegion())
                .storePhone(store.getPhoneNumber())
                .quantity(request.getQuantity())
                .unitPrice(room.getDiscountedPrice())
                .deliveryAddress(request.getDeliveryAddress() != null ? request.getDeliveryAddress() : store.getAddress())
                .deliveryPhone(request.getDeliveryPhone() != null ? request.getDeliveryPhone() : store.getPhoneNumber())
                .deliveryRequest(request.getDeliveryRequest())
                .status(ParticipantStatus.JOINED)
                .build();
        
        // 금액 계산
        participant.calculateTotalAmount();
        participant.calculateSavings(room.getOriginalPrice());
        
        // 방에 참여자 추가
        room.addParticipant(participant);
        
        // 배송비 업데이트
        participant.setDeliveryFee(room.getDeliveryFeePerStore());
        participant.calculateTotalAmount();
        
        GroupBuyingParticipant savedParticipant = participantRepository.save(participant);
        
        log.info("공동구매 참여: roomId={}, storeId={}, quantity={}, savings={}", 
                room.getRoomId(), request.getStoreId(), request.getQuantity(), participant.getSavingsAmount());
        
        return savedParticipant;
    }
    
    /**
     * 참여 취소 (가게)
     */
    @Transactional
    public void cancelParticipation(Long participantId, String storeId, String reason) {
        GroupBuyingParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("참여 정보를 찾을 수 없습니다."));
        
        // 권한 확인
        if (!participant.getStoreId().equals(storeId)) {
            throw new IllegalArgumentException("본인의 참여만 취소할 수 있습니다.");
        }
        
        // 방 조회
        GroupBuyingRoom room = participant.getRoom();
        
        // 취소 가능 여부 확인 (방이 아직 오픈 중일 때만)
        if (room.getStatus() != RoomStatus.OPEN) {
            throw new IllegalStateException("이미 마감된 방은 취소할 수 없습니다.");
        }
        
        // 참여 취소
        participant.cancel(reason);
        room.removeParticipant(participant);
        
        log.info("공동구매 참여 취소: roomId={}, storeId={}, reason={}", 
                room.getRoomId(), storeId, reason);
    }
    
    /**
     * 가게의 참여 내역 조회
     */
    public List<GroupBuyingParticipant> getStoreParticipations(String storeId) {
        return participantRepository.findByStoreIdOrderByJoinedAtDesc(storeId);
    }
    
    /**
     * 방의 참여자 목록 조회
     */
    public List<GroupBuyingParticipant> getRoomParticipants(String roomId) {
        GroupBuyingRoom room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
        
        return participantRepository.findByRoomIdOrderByJoinedAtAsc(room.getId());
    }
    
    /**
     * 참여자 확정 (방 마감 시 자동 호출)
     */
    @Transactional
    public void confirmParticipants(Long roomId) {
        List<GroupBuyingParticipant> participants = participantRepository.findByRoomIdAndStatus(
                roomId, ParticipantStatus.JOINED);
        
        for (GroupBuyingParticipant participant : participants) {
            participant.confirm();
        }
        
        log.info("참여자 확정 완료: roomId={}, count={}", roomId, participants.size());
    }
}
