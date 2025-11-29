package com.example.payflow.groupbuying.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupBuyingRoomRepository extends JpaRepository<GroupBuyingRoom, Long> {
    
    /**
     * 방 ID로 조회
     */
    Optional<GroupBuyingRoom> findByRoomId(String roomId);
    
    /**
     * 유통업자의 방 목록 조회
     */
    List<GroupBuyingRoom> findByDistributorIdOrderByCreatedAtDesc(String distributorId);
    
    /**
     * 상태별 방 목록 조회
     */
    List<GroupBuyingRoom> findByStatusOrderByCreatedAtDesc(RoomStatus status);
    
    /**
     * 오픈 중인 방 목록 조회 (지역 필터)
     */
    @Query("SELECT r FROM GroupBuyingRoom r WHERE r.status = 'OPEN' AND r.region LIKE %:region% ORDER BY r.createdAt DESC")
    List<GroupBuyingRoom> findOpenRoomsByRegion(@Param("region") String region);
    
    /**
     * 오픈 중인 모든 방 조회
     */
    List<GroupBuyingRoom> findByStatusAndDeadlineAfterOrderByCreatedAtDesc(RoomStatus status, LocalDateTime deadline);
    
    /**
     * 추천 방 목록 조회
     */
    List<GroupBuyingRoom> findByFeaturedTrueAndStatusOrderByCreatedAtDesc(RoomStatus status);
    
    /**
     * 카테고리별 오픈 방 조회
     */
    @Query("SELECT r FROM GroupBuyingRoom r WHERE r.status = 'OPEN' AND r.category = :category ORDER BY r.createdAt DESC")
    List<GroupBuyingRoom> findOpenRoomsByCategory(@Param("category") String category);
    
    /**
     * 마감 임박 방 조회 (24시간 이내)
     */
    @Query("SELECT r FROM GroupBuyingRoom r WHERE r.status = 'OPEN' AND r.deadline BETWEEN :now AND :endTime ORDER BY r.deadline ASC")
    List<GroupBuyingRoom> findDeadlineSoonRooms(@Param("now") LocalDateTime now, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 마감 시간이 지난 오픈 방 조회 (스케줄러용)
     */
    @Query("SELECT r FROM GroupBuyingRoom r WHERE r.status = 'OPEN' AND r.deadline < :now")
    List<GroupBuyingRoom> findExpiredOpenRooms(@Param("now") LocalDateTime now);
    
    /**
     * 유통업자의 특정 상품 오픈 방 조회
     */
    @Query("SELECT r FROM GroupBuyingRoom r WHERE r.distributorId = :distributorId AND r.productId = :productId AND r.status = 'OPEN'")
    Optional<GroupBuyingRoom> findOpenRoomByDistributorAndProduct(@Param("distributorId") String distributorId, @Param("productId") Long productId);
}
