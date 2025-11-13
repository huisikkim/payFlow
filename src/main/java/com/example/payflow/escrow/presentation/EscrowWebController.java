package com.example.payflow.escrow.presentation;

import com.example.payflow.escrow.application.EscrowService;
import com.example.payflow.escrow.application.dto.EscrowResponse;
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
}
