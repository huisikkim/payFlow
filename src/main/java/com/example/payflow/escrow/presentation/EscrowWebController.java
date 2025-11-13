package com.example.payflow.escrow.presentation;

import com.example.payflow.escrow.application.DepositService;
import com.example.payflow.escrow.application.EscrowEventSourcingService;
import com.example.payflow.escrow.application.EscrowService;
import com.example.payflow.escrow.application.SettlementService;
import com.example.payflow.escrow.application.VerificationService;
import com.example.payflow.escrow.application.dto.DepositResponse;
import com.example.payflow.escrow.application.dto.EscrowResponse;
import com.example.payflow.escrow.application.dto.SettlementResponse;
import com.example.payflow.escrow.application.dto.VerificationResponse;
import com.example.payflow.escrow.domain.EscrowEventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 에스크로 웹 UI 컨트롤러
 */
@Controller
@RequestMapping("/escrow")
@RequiredArgsConstructor
public class EscrowWebController {
    
    private final EscrowService escrowService;
    private final DepositService depositService;
    private final VerificationService verificationService;
    private final SettlementService settlementService;
    private final EscrowEventSourcingService eventSourcingService;
    
    /**
     * 에스크로 거래 목록 페이지
     */
    @GetMapping
    public String listEscrows(Model model) {
        List<EscrowResponse> escrows = escrowService.getAllEscrows();
        model.addAttribute("escrows", escrows);
        return "escrow-list";
    }
    
    /**
     * 에스크로 거래 상세 페이지
     */
    @GetMapping("/{transactionId}")
    public String escrowDetail(@PathVariable String transactionId, Model model) {
        try {
            EscrowResponse escrow = escrowService.getEscrow(transactionId);
            model.addAttribute("escrow", escrow);
            return "escrow-detail";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    /**
     * 에스크로 거래 생성 페이지
     */
    @GetMapping("/create")
    public String createEscrowForm() {
        return "escrow-create";
    }
    
    /**
     * 입금 내역 페이지
     */
    @GetMapping("/{transactionId}/deposits")
    public String depositList(@PathVariable String transactionId, Model model) {
        try {
            EscrowResponse escrow = escrowService.getEscrow(transactionId);
            List<DepositResponse> deposits = depositService.getDepositsByTransaction(transactionId);
            model.addAttribute("escrow", escrow);
            model.addAttribute("deposits", deposits);
            return "escrow-deposits";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    /**
     * 검증 내역 페이지
     */
    @GetMapping("/{transactionId}/verifications")
    public String verificationList(@PathVariable String transactionId, Model model) {
        try {
            EscrowResponse escrow = escrowService.getEscrow(transactionId);
            List<VerificationResponse> verifications = verificationService.getVerificationsByTransaction(transactionId);
            model.addAttribute("escrow", escrow);
            model.addAttribute("verifications", verifications);
            return "escrow-verifications";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    /**
     * 이벤트 히스토리 페이지
     */
    @GetMapping("/{transactionId}/events")
    public String eventHistory(@PathVariable String transactionId, Model model) {
        try {
            EscrowResponse escrow = escrowService.getEscrow(transactionId);
            List<EscrowEventStore> events = eventSourcingService.getEventHistory(transactionId);
            model.addAttribute("escrow", escrow);
            model.addAttribute("events", events);
            return "escrow-events";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    /**
     * 정산 내역 페이지
     */
    @GetMapping("/{transactionId}/settlement")
    public String settlement(@PathVariable String transactionId, Model model) {
        try {
            EscrowResponse escrow = escrowService.getEscrow(transactionId);
            SettlementResponse settlement = settlementService.getSettlementByTransaction(transactionId);
            model.addAttribute("escrow", escrow);
            model.addAttribute("settlement", settlement);
            return "escrow-settlement";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
