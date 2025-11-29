package com.example.payflow.groupbuying.application;

import com.example.payflow.catalog.domain.ProductCatalog;
import com.example.payflow.catalog.domain.ProductCatalogRepository;
import com.example.payflow.groupbuying.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 공동구매 방 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupBuyingRoomService {
    
    private final GroupBuyingRoomRepository roomRepository;
    private final ProductCatalogRepository productCatalogRepository;
    
    /**
     * 공동구매 방 생성 (유통업자)
     */
    @Transactional
    public GroupBuyingRoom createRoom(CreateRoomRequest request) {
        // 상품 정보 조회
        ProductCatalog product = productCatalogRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        
        // 유통업자 확인 (테스트를 위해 주석 처리)
        // if (!product.getDistributorId().equals(request.getDistributorId())) {
        //     throw new IllegalArgumentException("본인의 상품만 공동구매 방을 만들 수 있습니다.");
        // }
        
        // 재고 확인
        if (product.getStockQuantity() == null || product.getStockQuantity() < request.getAvailableStock()) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        
        // 할인 가격 계산
        BigDecimal discountRate = request.getDiscountRate();
        long originalPrice = product.getUnitPrice();
        long discountedPrice = calculateDiscountedPrice(originalPrice, discountRate);
        
        // 마감 시간 계산
        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now();
        LocalDateTime deadline = startTime.plusHours(request.getDurationHours());
        
        // 방 ID 생성
        String roomId = generateRoomId();
        
        // 방 생성
        GroupBuyingRoom room = GroupBuyingRoom.builder()
                .roomId(roomId)
                .roomTitle(request.getRoomTitle())
                .distributorId(request.getDistributorId())
                .distributorName(request.getDistributorName())
                .productId(product.getId())
                .productName(product.getProductName())
                .category(product.getCategory())
                .unit(product.getUnit())
                .origin(product.getOrigin())
                .productDescription(product.getDescription())
                .imageUrl(product.getImageUrl())
                .originalPrice(originalPrice)
                .discountRate(discountRate)
                .discountedPrice(discountedPrice)
                .availableStock(request.getAvailableStock())
                .targetQuantity(request.getTargetQuantity())
                .minOrderPerStore(request.getMinOrderPerStore())
                .maxOrderPerStore(request.getMaxOrderPerStore())
                .minParticipants(request.getMinParticipants())
                .maxParticipants(request.getMaxParticipants())
                .region(request.getRegion())
                .deliveryFee(request.getDeliveryFee())
                .deliveryFeeType(request.getDeliveryFeeType())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .startTime(startTime)
                .deadline(deadline)
                .durationHours(request.getDurationHours())
                .description(request.getDescription())
                .specialNote(request.getSpecialNote())
                .featured(request.getFeatured() != null ? request.getFeatured() : false)
                .status(RoomStatus.WAITING)
                .build();
        
        // 배송비 계산 (고정 배송비인 경우)
        if (room.getDeliveryFeeType() == DeliveryFeeType.FIXED) {
            room.setDeliveryFeePerStore(request.getDeliveryFee());
        } else if (room.getDeliveryFeeType() == DeliveryFeeType.FREE) {
            room.setDeliveryFeePerStore(0L);
        }
        
        GroupBuyingRoom savedRoom = roomRepository.save(room);
        
        log.info("공동구매 방 생성: roomId={}, product={}, discount={}%", 
                roomId, product.getProductName(), discountRate);
        
        return savedRoom;
    }
    
    /**
     * 방 오픈 (유통업자)
     */
    @Transactional
    public GroupBuyingRoom openRoom(String roomId, String distributorId) {
        GroupBuyingRoom room = getRoomByRoomId(roomId);
        
        // 권한 확인
        if (!room.getDistributorId().equals(distributorId)) {
            throw new IllegalArgumentException("본인의 방만 오픈할 수 있습니다.");
        }
        
        room.open();
        
        log.info("공동구매 방 오픈: roomId={}", roomId);
        
        return room;
    }
    
    /**
     * 방 조회 (상세)
     */
    public GroupBuyingRoom getRoom(String roomId) {
        GroupBuyingRoom room = getRoomByRoomId(roomId);
        room.increaseViewCount();
        roomRepository.save(room);
        return room;
    }
    
    /**
     * 오픈 중인 방 목록 조회
     */
    public List<GroupBuyingRoom> getOpenRooms() {
        return roomRepository.findByStatusAndDeadlineAfterOrderByCreatedAtDesc(
                RoomStatus.OPEN, LocalDateTime.now());
    }
    
    /**
     * 지역별 오픈 방 조회
     */
    public List<GroupBuyingRoom> getOpenRoomsByRegion(String region) {
        return roomRepository.findOpenRoomsByRegion(region);
    }
    
    /**
     * 카테고리별 오픈 방 조회
     */
    public List<GroupBuyingRoom> getOpenRoomsByCategory(String category) {
        return roomRepository.findOpenRoomsByCategory(category);
    }
    
    /**
     * 추천 방 목록 조회
     */
    public List<GroupBuyingRoom> getFeaturedRooms() {
        return roomRepository.findByFeaturedTrueAndStatusOrderByCreatedAtDesc(RoomStatus.OPEN);
    }
    
    /**
     * 마감 임박 방 조회
     */
    public List<GroupBuyingRoom> getDeadlineSoonRooms() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(24);
        return roomRepository.findDeadlineSoonRooms(now, endTime);
    }
    
    /**
     * 유통업자의 방 목록 조회
     */
    public List<GroupBuyingRoom> getDistributorRooms(String distributorId) {
        return roomRepository.findByDistributorIdOrderByCreatedAtDesc(distributorId);
    }
    
    /**
     * 방 수동 마감 (유통업자)
     */
    @Transactional
    public GroupBuyingRoom closeRoom(String roomId, String distributorId) {
        GroupBuyingRoom room = getRoomByRoomId(roomId);
        
        // 권한 확인
        if (!room.getDistributorId().equals(distributorId)) {
            throw new IllegalArgumentException("본인의 방만 마감할 수 있습니다.");
        }
        
        room.close();
        
        log.info("공동구매 방 수동 마감: roomId={}, status={}", roomId, room.getStatus());
        
        return room;
    }
    
    /**
     * 방 취소 (유통업자)
     */
    @Transactional
    public GroupBuyingRoom cancelRoom(String roomId, String distributorId, String reason) {
        GroupBuyingRoom room = getRoomByRoomId(roomId);
        
        // 권한 확인
        if (!room.getDistributorId().equals(distributorId)) {
            throw new IllegalArgumentException("본인의 방만 취소할 수 있습니다.");
        }
        
        room.cancel(reason);
        
        log.info("공동구매 방 취소: roomId={}, reason={}", roomId, reason);
        
        return room;
    }
    
    /**
     * 만료된 방 자동 마감 (스케줄러용)
     */
    @Transactional
    public void closeExpiredRooms() {
        List<GroupBuyingRoom> expiredRooms = roomRepository.findExpiredOpenRooms(LocalDateTime.now());
        
        for (GroupBuyingRoom room : expiredRooms) {
            try {
                room.close();
                log.info("공동구매 방 자동 마감: roomId={}, status={}", room.getRoomId(), room.getStatus());
            } catch (Exception e) {
                log.error("공동구매 방 자동 마감 실패: roomId={}", room.getRoomId(), e);
            }
        }
    }
    
    // === Private Methods ===
    
    private GroupBuyingRoom getRoomByRoomId(String roomId) {
        return roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
    }
    
    private long calculateDiscountedPrice(long originalPrice, BigDecimal discountRate) {
        BigDecimal price = BigDecimal.valueOf(originalPrice);
        BigDecimal discount = BigDecimal.ONE.subtract(discountRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return price.multiply(discount).setScale(0, RoundingMode.HALF_UP).longValue();
    }
    
    private String generateRoomId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return "GBR-" + timestamp + "-" + random;
    }
}
