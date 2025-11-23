package com.example.payflow.settlement.presentation;

import com.example.payflow.settlement.application.IngredientSettlementService;
import com.example.payflow.settlement.presentation.dto.CompleteSettlementRequest;
import com.example.payflow.settlement.presentation.dto.SettlementResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
@Slf4j
public class IngredientSettlementController {
    
    private final IngredientSettlementService settlementService;
    
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<SettlementResponse>> getSettlementsByStore(@PathVariable String storeId) {
        List<SettlementResponse> settlements = settlementService.getSettlementsByStore(storeId);
        return ResponseEntity.ok(settlements);
    }
    
    @GetMapping("/distributor/{distributorId}")
    public ResponseEntity<List<SettlementResponse>> getSettlementsByDistributor(@PathVariable String distributorId) {
        List<SettlementResponse> settlements = settlementService.getSettlementsByDistributor(distributorId);
        return ResponseEntity.ok(settlements);
    }
    
    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponse> getSettlement(@PathVariable String settlementId) {
        SettlementResponse settlement = settlementService.getSettlement(settlementId);
        return ResponseEntity.ok(settlement);
    }
    
    @PostMapping("/{settlementId}/complete")
    public ResponseEntity<Void> completeSettlement(@PathVariable String settlementId,
                                                   @Valid @RequestBody CompleteSettlementRequest request) {
        log.info("💰 정산 완료 요청: settlementId={}, paidAmount={}", settlementId, request.getPaidAmount());
        settlementService.completeSettlement(settlementId, request.getPaidAmount());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/store/{storeId}/outstanding")
    public ResponseEntity<Map<String, Long>> getTotalOutstanding(@PathVariable String storeId) {
        Long totalOutstanding = settlementService.calculateTotalOutstanding(storeId);
        return ResponseEntity.ok(Map.of("totalOutstanding", totalOutstanding));
    }
}
