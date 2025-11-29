package com.example.payflow.groupbuying.presentation;

import com.example.payflow.groupbuying.application.CreateRoomRequest;
import com.example.payflow.groupbuying.application.GroupBuyingRoomService;
import com.example.payflow.groupbuying.domain.GroupBuyingRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공동구매 방 API
 */
@RestController
@RequestMapping("/api/group-buying/rooms")
@RequiredArgsConstructor
@Slf4j
public class GroupBuyingRoomController {
    
    private final GroupBuyingRoomService roomService;
    
    /**
     * 공동구매 방 생성 (유통업자)
     */
    @PostMapping
    public ResponseEntity<GroupBuyingRoomResponse> createRoom(@RequestBody CreateRoomRequest request) {
        GroupBuyingRoom room = roomService.createRoom(request);
        return ResponseEntity.ok(GroupBuyingRoomResponse.from(room));
    }
    
    /**
     * 방 오픈 (유통업자)
     */
    @PostMapping("/{roomId}/open")
    public ResponseEntity<GroupBuyingRoomResponse> openRoom(
            @PathVariable String roomId,
            @RequestParam String distributorId) {
        GroupBuyingRoom room = roomService.openRoom(roomId, distributorId);
        return ResponseEntity.ok(GroupBuyingRoomResponse.from(room));
    }
    
    /**
     * 방 상세 조회
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<GroupBuyingRoomResponse> getRoom(@PathVariable String roomId) {
        GroupBuyingRoom room = roomService.getRoom(roomId);
        return ResponseEntity.ok(GroupBuyingRoomResponse.from(room));
    }
    
    /**
     * 오픈 중인 방 목록 조회
     */
    @GetMapping("/open")
    public ResponseEntity<List<GroupBuyingRoomResponse>> getOpenRooms(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category) {
        
        List<GroupBuyingRoom> rooms;
        
        if (region != null) {
            rooms = roomService.getOpenRoomsByRegion(region);
        } else if (category != null) {
            rooms = roomService.getOpenRoomsByCategory(category);
        } else {
            rooms = roomService.getOpenRooms();
        }
        
        return ResponseEntity.ok(rooms.stream()
                .map(GroupBuyingRoomResponse::from)
                .toList());
    }
    
    /**
     * 추천 방 목록 조회
     */
    @GetMapping("/featured")
    public ResponseEntity<List<GroupBuyingRoomResponse>> getFeaturedRooms() {
        List<GroupBuyingRoom> rooms = roomService.getFeaturedRooms();
        return ResponseEntity.ok(rooms.stream()
                .map(GroupBuyingRoomResponse::from)
                .toList());
    }
    
    /**
     * 마감 임박 방 조회
     */
    @GetMapping("/deadline-soon")
    public ResponseEntity<List<GroupBuyingRoomResponse>> getDeadlineSoonRooms() {
        List<GroupBuyingRoom> rooms = roomService.getDeadlineSoonRooms();
        return ResponseEntity.ok(rooms.stream()
                .map(GroupBuyingRoomResponse::from)
                .toList());
    }
    
    /**
     * 유통업자의 방 목록 조회
     */
    @GetMapping("/distributor/{distributorId}")
    public ResponseEntity<List<GroupBuyingRoomResponse>> getDistributorRooms(
            @PathVariable String distributorId) {
        List<GroupBuyingRoom> rooms = roomService.getDistributorRooms(distributorId);
        return ResponseEntity.ok(rooms.stream()
                .map(GroupBuyingRoomResponse::from)
                .toList());
    }
    
    /**
     * 방 수동 마감 (유통업자)
     */
    @PostMapping("/{roomId}/close")
    public ResponseEntity<GroupBuyingRoomResponse> closeRoom(
            @PathVariable String roomId,
            @RequestParam String distributorId) {
        GroupBuyingRoom room = roomService.closeRoom(roomId, distributorId);
        return ResponseEntity.ok(GroupBuyingRoomResponse.from(room));
    }
    
    /**
     * 방 취소 (유통업자)
     */
    @PostMapping("/{roomId}/cancel")
    public ResponseEntity<GroupBuyingRoomResponse> cancelRoom(
            @PathVariable String roomId,
            @RequestParam String distributorId,
            @RequestParam String reason) {
        GroupBuyingRoom room = roomService.cancelRoom(roomId, distributorId, reason);
        return ResponseEntity.ok(GroupBuyingRoomResponse.from(room));
    }
}
