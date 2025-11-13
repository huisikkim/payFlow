package com.example.payflow.escrow.presentation;

import com.example.payflow.escrow.application.EscrowPaymentService;
import com.example.payflow.escrow.application.dto.DepositResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 에스크로 결제 컨트롤러
 * 토스 페이먼츠를 통한 에스크로 입금 처리
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class EscrowPaymentController {
    
    private final EscrowPaymentService escrowPaymentService;
    
    @Value("${toss.payments.client-key}")
    private String clientKey;
    
    /**
     * 에스크로 입금 결제 페이지
     */
    @GetMapping("/escrow/{transactionId}/payment")
    public String paymentPage(@PathVariable String transactionId, Model model) {
        EscrowPaymentService.EscrowPaymentInfo paymentInfo = 
            escrowPaymentService.createPaymentInfo(transactionId);
        
        model.addAttribute("clientKey", clientKey);
        model.addAttribute("paymentInfo", paymentInfo);
        
        return "escrow-payment";
    }
    
    /**
     * 에스크로 결제 승인 API
     */
    @PostMapping("/api/escrow/{transactionId}/payment/confirm")
    @ResponseBody
    public ResponseEntity<DepositResponse> confirmPayment(
            @PathVariable String transactionId,
            @RequestBody PaymentConfirmRequest request) {
        
        log.info("에스크로 결제 승인 요청: transactionId={}, paymentKey={}", 
            transactionId, request.getPaymentKey());
        
        DepositResponse response = escrowPaymentService.confirmPaymentAndDeposit(
            transactionId,
            request.getPaymentKey(),
            request.getOrderId(),
            request.getAmount()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 결제 성공 페이지
     */
    @GetMapping("/escrow/payment/success")
    public String paymentSuccess(
            @RequestParam String transactionId,
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model) {
        
        model.addAttribute("transactionId", transactionId);
        model.addAttribute("paymentKey", paymentKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        
        return "escrow-payment-success";
    }
    
    /**
     * 결제 실패 페이지
     */
    @GetMapping("/escrow/payment/fail")
    public String paymentFail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam(required = false) String transactionId,
            Model model) {
        
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("transactionId", transactionId);
        
        return "escrow-payment-fail";
    }
    
    @lombok.Data
    public static class PaymentConfirmRequest {
        private String paymentKey;
        private String orderId;
        private BigDecimal amount;
    }
}
