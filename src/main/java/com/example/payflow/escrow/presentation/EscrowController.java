package com.example.payflow.escrow.presentation;

import com.example.payflow.escrow.application.*;
import com.example.payflow.escrow.application.dto.*;
import com.example.payflow.escrow.domain.DisputeStatus;
import com.example.payflow.escrow.domain.EscrowEventStore;
import com.example.payflow.escrow.domain.EscrowStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 에스크로 REST API 컨트롤러
 * 개발 환경: SecurityConfig에서 모든 API 허용
 */
@RestController
@RequestMapping("/api/escrow")
@RequiredArgsConstructor
public class EscrowController {
    
    private final EscrowService escrowService;
    private final DepositService depositService;
    private final VerificationService verificationService;
    private final SettlementService settlementService;
    private final DisputeService disputeService;
    private final EscrowEventSourcingService eventSourcingService;
    
    // ===== 기본 CRUD API =====
    
    @PostMapping
    public ResponseEntity<EscrowResponse> createEscrow(@RequestBody CreateEscrowRequest request) {
        EscrowResponse response = escrowService.createEscrow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{transactionId}")
    public ResponseEntity<EscrowResponse> getEscrow(@PathVariable String transactionId) {
        EscrowResponse response = escrowService.getEscrow(transactionId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<EscrowResponse>> getEscrowsByBuyer(@PathVariable String buyerId) {
        List<EscrowResponse> responses = escrowService.getEscrowsByBuyer(buyerId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<EscrowResponse>> getEscrowsBySeller(@PathVariable String sellerId) {
        List<EscrowResponse> responses = escrowService.getEscrowsBySeller(sellerId);
        return ResponseEntity.ok(responses);
    }
    
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<EscrowResponse> cancelEscrow(
            @PathVariable String transactionId,
            @RequestParam String reason) {
        EscrowResponse response = escrowService.cancelEscrow(transactionId, reason);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<EscrowResponse>> getAllEscrows() {
        List<EscrowResponse> responses = escrowService.getAllEscrows();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EscrowResponse>> getEscrowsByStatus(@PathVariable EscrowStatus status) {
        List<EscrowResponse> responses = escrowService.getEscrowsByStatus(status);
        return ResponseEntity.ok(responses);
    }
    
    // ===== 입금 API =====
    
    @PostMapping("/{transactionId}/deposit")
    public ResponseEntity<DepositResponse> processDeposit(@RequestBody DepositRequest request) {
        DepositResponse response = depositService.processDeposit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{transactionId}/deposits")
    public ResponseEntity<List<DepositResponse>> getDeposits(@PathVariable String transactionId) {
        List<DepositResponse> responses = depositService.getDepositsByTransaction(transactionId);
        return ResponseEntity.ok(responses);
    }
    
    // ===== 검증 API =====
    
    @PostMapping("/{transactionId}/delivery")
    public ResponseEntity<EscrowResponse> confirmDelivery(@RequestBody DeliveryConfirmRequest request) {
        EscrowResponse response = verificationService.confirmDelivery(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{transactionId}/verification")
    public ResponseEntity<VerificationResponse> verifyVehicle(@RequestBody VerificationRequest request) {
        VerificationResponse response = verificationService.verifyVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/{transactionId}/ownership-transfer")
    public ResponseEntity<VerificationResponse> confirmOwnershipTransfer(
            @RequestBody OwnershipTransferRequest request) {
        VerificationResponse response = verificationService.confirmOwnershipTransfer(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{transactionId}/verifications")
    public ResponseEntity<List<VerificationResponse>> getVerifications(@PathVariable String transactionId) {
        List<VerificationResponse> responses = verificationService.getVerificationsByTransaction(transactionId);
        return ResponseEntity.ok(responses);
    }
    
    // ===== 정산 및 분쟁 API =====
    
    @PostMapping("/{transactionId}/settlement/start")
    public ResponseEntity<SettlementResponse> startSettlement(@PathVariable String transactionId) {
        SettlementResponse response = settlementService.startSettlement(transactionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/{transactionId}/settlement/complete")
    public ResponseEntity<SettlementResponse> completeSettlement(
            @PathVariable String transactionId,
            @RequestParam String paymentMethod,
            @RequestParam String paymentReference) {
        SettlementResponse response = settlementService.completeSettlement(
            transactionId, paymentMethod, paymentReference);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{transactionId}/settlement")
    public ResponseEntity<SettlementResponse> getSettlement(@PathVariable String transactionId) {
        SettlementResponse response = settlementService.getSettlementByTransaction(transactionId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{transactionId}/dispute")
    public ResponseEntity<DisputeResponse> raiseDispute(@RequestBody DisputeRequest request) {
        DisputeResponse response = disputeService.raiseDispute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/disputes/{disputeId}/resolve")
    public ResponseEntity<DisputeResponse> resolveDispute(
            @PathVariable Long disputeId,
            @RequestParam String resolution,
            @RequestParam String resolvedBy) {
        DisputeResponse response = disputeService.resolveDispute(disputeId, resolution, resolvedBy);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{transactionId}/disputes")
    public ResponseEntity<List<DisputeResponse>> getDisputes(@PathVariable String transactionId) {
        List<DisputeResponse> responses = disputeService.getDisputesByTransaction(transactionId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/disputes/open")
    public ResponseEntity<List<DisputeResponse>> getOpenDisputes() {
        List<DisputeResponse> responses = disputeService.getOpenDisputes();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/disputes/status/{status}")
    public ResponseEntity<List<DisputeResponse>> getDisputesByStatus(@PathVariable DisputeStatus status) {
        List<DisputeResponse> responses = disputeService.getDisputesByStatus(status);
        return ResponseEntity.ok(responses);
    }
    
    // ===== 이벤트 히스토리 API =====
    
    @GetMapping("/{transactionId}/events")
    public ResponseEntity<List<EscrowEventStore>> getEventHistory(@PathVariable String transactionId) {
        List<EscrowEventStore> events = eventSourcingService.getEventHistory(transactionId);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/{transactionId}/events/{sequence}")
    public ResponseEntity<Map<String, Object>> reconstructState(
            @PathVariable String transactionId,
            @PathVariable Integer sequence) {
        Map<String, Object> state = eventSourcingService.reconstructState(transactionId, sequence);
        return ResponseEntity.ok(state);
    }
}
