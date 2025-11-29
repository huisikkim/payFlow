package com.example.payflow.groupbuying.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupBuyingParticipantRepository extends JpaRepository<GroupBuyingParticipant, Long> {
    
    /**
     * 방의 모든 참여자 조회
     */
    @Query("SELECT p FROM GroupBuyingParticipant p WHERE p.room.id = :roomId ORDER BY p.joinedAt ASC")
    List<GroupBuyingParticipant> findByRoomIdOrderByJoinedAtAsc(@Param("roomId") Long roomId);
    
    /**
     * 가게의 참여 내역 조회
     */
    List<GroupBuyingParticipant> findByStoreIdOrderByJoinedAtDesc(String storeId);
    
    /**
     * 가게의 특정 방 참여 여부 확인
     */
    @Query("SELECT p FROM GroupBuyingParticipant p WHERE p.room.id = :roomId AND p.storeId = :storeId")
    Optional<GroupBuyingParticipant> findByRoomIdAndStoreId(@Param("roomId") Long roomId, @Param("storeId") String storeId);
    
    /**
     * 가게가 이미 참여했는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM GroupBuyingParticipant p WHERE p.room.id = :roomId AND p.storeId = :storeId")
    boolean existsByRoomIdAndStoreId(@Param("roomId") Long roomId, @Param("storeId") String storeId);
    
    /**
     * 방의 참여자 수 카운트
     */
    @Query("SELECT COUNT(p) FROM GroupBuyingParticipant p WHERE p.room.id = :roomId AND p.status != 'CANCELLED'")
    Integer countActiveParticipantsByRoomId(@Param("roomId") Long roomId);
    
    /**
     * 방의 총 주문 수량 합계
     */
    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM GroupBuyingParticipant p WHERE p.room.id = :roomId AND p.status != 'CANCELLED'")
    Integer sumQuantityByRoomId(@Param("roomId") Long roomId);
    
    /**
     * 상태별 참여자 조회
     */
    @Query("SELECT p FROM GroupBuyingParticipant p WHERE p.room.id = :roomId AND p.status = :status")
    List<GroupBuyingParticipant> findByRoomIdAndStatus(@Param("roomId") Long roomId, @Param("status") ParticipantStatus status);
    
    /**
     * 확정된 참여자 조회 (주문 생성용)
     */
    @Query("SELECT p FROM GroupBuyingParticipant p WHERE p.room.id = :roomId AND p.status = :status ORDER BY p.joinedAt ASC")
    List<GroupBuyingParticipant> findByRoomIdAndStatusOrderByJoinedAtAsc(@Param("roomId") Long roomId, @Param("status") ParticipantStatus status);
}
