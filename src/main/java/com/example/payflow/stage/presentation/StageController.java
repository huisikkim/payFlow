package com.example.payflow.stage.presentation;

import com.example.payflow.stage.application.StagePaymentService;
import com.example.payflow.stage.application.StagePayoutService;
import com.example.payflow.stage.application.StageService;
import com.example.payflow.stage.domain.*;
import com.example.payflow.stage.presentation.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stages")
@RequiredArgsConstructor
public class StageController {

    private final StageService stageService;
    private final StagePaymentService paymentService;
    private final StagePayoutService payoutService;

    @PostMapping
    public ResponseEntity<StageResponse> createStage(
            @Valid @RequestBody CreateStageRequest request) {
        Stage stage = stageService.createStage(
                request.getName(),
                request.getTotalParticipants(),
                request.getMonthlyPayment(),
                request.getInterestRate(),
                request.getPaymentDay()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(StageResponse.from(stage));
    }

    @PostMapping("/{stageId}/join")
    public ResponseEntity<StageParticipantResponse> joinStage(
            @PathVariable Long stageId,
            @Valid @RequestBody JoinStageRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        StageParticipant participant = stageService.joinStage(stageId, username, request.getTurnNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(StageParticipantResponse.from(participant));
    }

    @PostMapping("/{stageId}/start")
    public ResponseEntity<Void> startStage(@PathVariable Long stageId) {
        stageService.startStage(stageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{stageId}")
    public ResponseEntity<StageResponse> getStage(@PathVariable Long stageId) {
        Stage stage = stageService.getStage(stageId);
        return ResponseEntity.ok(StageResponse.from(stage));
    }

    @GetMapping
    public ResponseEntity<List<StageResponse>> getStages(
            @RequestParam(required = false) StageStatus status) {
        List<Stage> stages = status != null 
                ? stageService.getStagesByStatus(status)
                : stageService.getStagesByStatus(StageStatus.RECRUITING);
        
        List<StageResponse> responses = stages.stream()
                .map(StageResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my")
    public ResponseEntity<List<StageResponse>> getMyStages(Authentication authentication) {
        String username = authentication.getName();
        List<Stage> stages = stageService.getMyStages(username);
        
        List<StageResponse> responses = stages.stream()
                .map(StageResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{stageId}/participants")
    public ResponseEntity<List<StageParticipantResponse>> getParticipants(@PathVariable Long stageId) {
        List<StageParticipant> participants = stageService.getParticipants(stageId);
        
        List<StageParticipantResponse> responses = participants.stream()
                .map(StageParticipantResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{stageId}/payments")
    public ResponseEntity<List<StagePaymentResponse>> getPayments(@PathVariable Long stageId) {
        List<StagePayment> payments = paymentService.getPaymentsByStage(stageId);
        
        List<StagePaymentResponse> responses = payments.stream()
                .map(StagePaymentResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/payments/my")
    public ResponseEntity<List<StagePaymentResponse>> getMyPayments(Authentication authentication) {
        String username = authentication.getName();
        List<StagePayment> payments = paymentService.getMyPayments(username);
        
        List<StagePaymentResponse> responses = payments.stream()
                .map(StagePaymentResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{stageId}/payouts")
    public ResponseEntity<List<StagePayoutResponse>> getPayouts(@PathVariable Long stageId) {
        List<StagePayout> payouts = payoutService.getPayoutsByStage(stageId);
        
        List<StagePayoutResponse> responses = payouts.stream()
                .map(StagePayoutResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/payouts/my")
    public ResponseEntity<List<StagePayoutResponse>> getMyPayouts(Authentication authentication) {
        String username = authentication.getName();
        List<StagePayout> payouts = payoutService.getMyPayouts(username);
        
        List<StagePayoutResponse> responses = payouts.stream()
                .map(StagePayoutResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/payouts/{payoutId}/complete")
    public ResponseEntity<Void> completePayout(
            @PathVariable Long payoutId,
            @Valid @RequestBody CompletePayoutRequest request) {
        payoutService.completePayout(payoutId, request.getTransactionId());
        return ResponseEntity.ok().build();
    }
}
