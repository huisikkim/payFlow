package com.example.payflow.escrow.presentation;

import com.example.payflow.escrow.application.EscrowPaymentService;
import com.example.payflow.escrow.application.VirtualAccountService;
import com.example.payflow.escrow.application.dto.DepositResponse;
import com.example.payflow.escrow.application.dto.VirtualAccountDepositResponse;
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
    private final VirtualAccountService virtualAccountService;
    
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
    
    /**
     * 가상계좌 입금 페이지
     */
    @GetMapping("/escrow/{transactionId}/payment/virtual-account")
    public String virtualAccountPaymentPage(@PathVariable String transactionId, Model model) {
        EscrowPaymentService.EscrowPaymentInfo paymentInfo = 
            escrowPaymentService.createPaymentInfo(transactionId);
        
        model.addAttribute("clientKey", clientKey);
        model.addAttribute("paymentInfo", paymentInfo);
        
        return "escrow-payment-virtual-account";
    }
    
    /**
     * 가상계좌 발급 API
     */
    @PostMapping("/api/escrow/{transactionId}/payment/virtual-account/issue")
    @ResponseBody
    public ResponseEntity<VirtualAccountDepositResponse> issueVirtualAccount(
            @PathVariable String transactionId,
            @RequestBody PaymentConfirmRequest request) {
        
        log.info("가상계좌 발급 요청: transactionId={}, paymentKey={}", 
            transactionId, request.getPaymentKey());
        
        VirtualAccountDepositResponse response = virtualAccountService.issueVirtualAccount(
            transactionId,
            request.getPaymentKey(),
            request.getOrderId(),
            request.getAmount()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 가상계좌 입금 완료 페이지
     */
    @GetMapping("/escrow/payment/virtual-account/success")
    public String virtualAccountSuccess(
            @RequestParam String transactionId,
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model) {
        
        model.addAttribute("transactionId", transactionId);
        model.addAttribute("paymentKey", paymentKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        
        return "escrow-payment-virtual-account-success";
    }
    
    /**
     * 토스페이먼츠 웹훅 - 가상계좌 입금 완료
     */
    @PostMapping("/api/escrow/webhook/virtual-account")
    @ResponseBody
    public ResponseEntity<String> virtualAccountWebhook(@RequestBody TossWebhookRequest request) {
        log.info("가상계좌 웹훅 수신: eventType={}, orderId={}", 
            request.getEventType(), request.getData().getOrderId());
        
        try {
            if ("VIRTUAL_ACCOUNT_ISSUED".equals(request.getEventType())) {
                log.info("가상계좌 발급 완료 웹훅");
                // 가상계좌 발급은 이미 처리됨
            } else if ("PAYMENT_STATUS_CHANGED".equals(request.getEventType())) {
                String status = request.getData().getStatus();
                if ("DONE".equals(status)) {
                    // 입금 완료 처리
                    virtualAccountService.completeVirtualAccountDeposit(
                        request.getData().getOrderId(),
                        request.getData().getVirtualAccount() != null ? 
                            request.getData().getVirtualAccount().getCustomerName() : "Unknown"
                    );
                } else if ("CANCELED".equals(status)) {
                    // 취소 처리
                    virtualAccountService.cancelVirtualAccount(
                        request.getData().getOrderId(),
                        request.getData().getCancels() != null && !request.getData().getCancels().isEmpty() ?
                            request.getData().getCancels().get(0).getCancelReason() : "Unknown"
                    );
                }
            }
            
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            log.error("웹훅 처리 실패", e);
            return ResponseEntity.internalServerError().body("FAILED");
        }
    }
    
    @lombok.Data
    public static class PaymentConfirmRequest {
        private String paymentKey;
        private String orderId;
        private BigDecimal amount;
    }
    
    @lombok.Data
    public static class TossWebhookRequest {
        private String eventType;
        private String createdAt;
        private WebhookData data;
    }
    
    @lombok.Data
    public static class WebhookData {
        private String orderId;
        private String status;
        private WebhookVirtualAccount virtualAccount;
        private java.util.List<WebhookCancel> cancels;
    }
    
    @lombok.Data
    public static class WebhookVirtualAccount {
        private String accountNumber;
        private String bank;
        private String customerName;
    }
    
    @lombok.Data
    public static class WebhookCancel {
        private String cancelReason;
    }
}
