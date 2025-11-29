package com.example.payflow.groupbuying.application;

import com.example.payflow.groupbuying.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 공동구매 통계 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupBuyingStatisticsService {
    
    private final GroupBuyingRoomRepository roomRepository;
    private final GroupBuyingParticipantRepository participantRepository;
    
    /**
     * 유통업자 통계
     */
    public Map<String, Object> getDistributorStatistics(String distributorId) {
        List<GroupBuyingRoom> rooms = roomRepository.findByDistributorIdOrderByCreatedAtDesc(distributorId);
        
        long totalRooms = rooms.size();
        long openRooms = rooms.stream().filter(r -> r.getStatus() == RoomStatus.OPEN).count();
        long successRooms = rooms.stream().filter(r -> r.getStatus() == RoomStatus.CLOSED_SUCCESS).count();
        long failedRooms = rooms.stream().filter(r -> r.getStatus() == RoomStatus.CLOSED_FAILED).count();
        
        long totalRevenue = rooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.CLOSED_SUCCESS || r.getStatus() == RoomStatus.ORDER_CREATED)
                .mapToLong(r -> r.getDiscountedPrice() * r.getCurrentQuantity())
                .sum();
        
        int totalParticipants = rooms.stream()
                .mapToInt(GroupBuyingRoom::getCurrentParticipants)
                .sum();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("distributorId", distributorId);
        stats.put("totalRooms", totalRooms);
        stats.put("openRooms", openRooms);
        stats.put("successRooms", successRooms);
        stats.put("failedRooms", failedRooms);
        stats.put("successRate", totalRooms > 0 ? (double) successRooms / totalRooms * 100 : 0);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalParticipants", totalParticipants);
        
        return stats;
    }
    
    /**
     * 가게 통계
     */
    public Map<String, Object> getStoreStatistics(String storeId) {
        List<GroupBuyingParticipant> participations = participantRepository.findByStoreIdOrderByJoinedAtDesc(storeId);
        
        long totalParticipations = participations.size();
        long activeParticipations = participations.stream()
                .filter(p -> p.getStatus() == ParticipantStatus.JOINED || p.getStatus() == ParticipantStatus.CONFIRMED)
                .count();
        long completedOrders = participations.stream()
                .filter(p -> p.getStatus() == ParticipantStatus.ORDER_CREATED || p.getStatus() == ParticipantStatus.DELIVERED)
                .count();
        
        long totalSavings = participations.stream()
                .filter(p -> p.getStatus() != ParticipantStatus.CANCELLED)
                .mapToLong(GroupBuyingParticipant::getSavingsAmount)
                .sum();
        
        long totalSpent = participations.stream()
                .filter(p -> p.getStatus() != ParticipantStatus.CANCELLED)
                .mapToLong(GroupBuyingParticipant::getTotalAmount)
                .sum();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("storeId", storeId);
        stats.put("totalParticipations", totalParticipations);
        stats.put("activeParticipations", activeParticipations);
        stats.put("completedOrders", completedOrders);
        stats.put("totalSavings", totalSavings);
        stats.put("totalSpent", totalSpent);
        
        return stats;
    }
    
    /**
     * 전체 시스템 통계
     */
    public Map<String, Object> getSystemStatistics() {
        List<GroupBuyingRoom> allRooms = roomRepository.findAll();
        List<GroupBuyingParticipant> allParticipants = participantRepository.findAll();
        
        long totalRooms = allRooms.size();
        long openRooms = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.OPEN).count();
        long successRooms = allRooms.stream().filter(r -> r.getStatus() == RoomStatus.CLOSED_SUCCESS).count();
        
        long totalParticipants = allParticipants.stream()
                .filter(p -> p.getStatus() != ParticipantStatus.CANCELLED)
                .count();
        
        long totalRevenue = allRooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.CLOSED_SUCCESS || r.getStatus() == RoomStatus.ORDER_CREATED)
                .mapToLong(r -> r.getDiscountedPrice() * r.getCurrentQuantity())
                .sum();
        
        long totalSavings = allParticipants.stream()
                .filter(p -> p.getStatus() != ParticipantStatus.CANCELLED)
                .mapToLong(GroupBuyingParticipant::getSavingsAmount)
                .sum();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRooms", totalRooms);
        stats.put("openRooms", openRooms);
        stats.put("successRooms", successRooms);
        stats.put("successRate", totalRooms > 0 ? (double) successRooms / totalRooms * 100 : 0);
        stats.put("totalParticipants", totalParticipants);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalSavings", totalSavings);
        
        return stats;
    }
}
