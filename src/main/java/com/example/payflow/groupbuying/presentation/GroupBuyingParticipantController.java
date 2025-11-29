package com.example.payflow.groupbuying.presentation;

import com.example.payflow.groupbuying.application.GroupBuyingParticipantService;
import com.example.payflow.groupbuying.application.JoinRoomRequest;
import com.example.payflow.groupbuying.domain.GroupBuyingParticipant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공동구매 참여 API
 */
@RestController
@RequestMapping("/api/group-buying/participants")
@RequiredArgsConstructor
@Slf4j
public class GroupBuyingParticipantController {
    
    private final GroupBuyingParticipantService participantService;
    
    /**
     * 공동구매 참여 (가게)
     */
    @PostMapping("/join")
    public ResponseEntity<GroupBuyingParticipantResponse> joinRoom(@RequestBody JoinRoomRequest request) {
        GroupBuyingParticipant participant = participantService.joinRoom(request);
        return ResponseEntity.ok(GroupBuyingParticipantResponse.from(participant));
    }
    
    /**
     * 참여 취소 (가게)
     */
    @PostMapping("/{participantId}/cancel")
    public ResponseEntity<Void> cancelParticipation(
            @PathVariable Long participantId,
            @RequestParam String storeId,
            @RequestParam String reason) {
        participantService.cancelParticipation(participantId, storeId, reason);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 가게의 참여 내역 조회
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<GroupBuyingParticipantResponse>> getStoreParticipations(
            @PathVariable String storeId) {
        List<GroupBuyingParticipant> participants = participantService.getStoreParticipations(storeId);
        return ResponseEntity.ok(participants.stream()
                .map(GroupBuyingParticipantResponse::from)
                .toList());
    }
    
    /**
     * 방의 참여자 목록 조회
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<GroupBuyingParticipantResponse>> getRoomParticipants(
            @PathVariable String roomId) {
        List<GroupBuyingParticipant> participants = participantService.getRoomParticipants(roomId);
        return ResponseEntity.ok(participants.stream()
                .map(GroupBuyingParticipantResponse::from)
                .toList());
    }
}
