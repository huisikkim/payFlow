package com.example.payflow.escrow.presentation;

import com.example.payflow.escrow.application.EscrowPaymentService;
import com.example.payflow.escrow.application.VirtualAccountService;
import com.example.payflow.escrow.application.dto.DepositResponse;
import com.example.payflow.escrow.application.dto.VirtualAccountDepositResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
    public ResponseEntity<String> virtualAccountWebhook(@RequestBody String rawBody) {
        log.info("가상계좌 웹훅 수신 (POST) - Raw Body: {}", rawBody);
        
        try {
            // JSON 파싱
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            TossWebhookRequest webhook = objectMapper.readValue(rawBody, TossWebhookRequest.class);
            
            log.info("파싱된 웹훅 데이터: orderId={}, status={}, paymentKey={}", 
                webhook.getOrderId(), 
                webhook.getStatus(),
                webhook.getPaymentKey());
            
            if (webhook.getOrderId() == null) {
                log.error("orderId가 null입니다. Raw Body를 확인하세요.");
                return ResponseEntity.badRequest().body("FAILED: orderId is null");
            }
            
            String status = webhook.getStatus();
            
            if ("DONE".equals(status)) {
                // 입금 완료 처리
                log.info("가상계좌 입금 완료 처리 시작: orderId={}", webhook.getOrderId());
                
                String customerName = webhook.getVirtualAccount() != null ? 
                    webhook.getVirtualAccount().getCustomerName() : "Unknown";
                
                virtualAccountService.completeVirtualAccountDeposit(
                    webhook.getOrderId(),
                    customerName
                );
                
                log.info("가상계좌 입금 완료 처리 성공: orderId={}, customerName={}", 
                    webhook.getOrderId(), customerName);
                    
            } else if ("CANCELED".equals(status)) {
                // 취소 처리
                log.info("가상계좌 취소 처리 시작: orderId={}", webhook.getOrderId());
                
                String cancelReason = webhook.getCancels() != null && !webhook.getCancels().isEmpty() ?
                    webhook.getCancels().get(0).getCancelReason() : "Unknown";
                
                virtualAccountService.cancelVirtualAccount(
                    webhook.getOrderId(),
                    cancelReason
                );
                
                log.info("가상계좌 취소 처리 성공: orderId={}, reason={}", 
                    webhook.getOrderId(), cancelReason);
            } else {
                log.info("처리하지 않는 상태: status={}", status);
            }
            
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            log.error("웹훅 처리 실패", e);
            return ResponseEntity.internalServerError().body("FAILED: " + e.getMessage());
        }
    }
    
    /**
     * 토스페이먼츠 웹훅 - GET 요청 처리 (개발/테스트용 자동 입금 처리)
     * 가장 최근 입금 대기 중인 가상계좌를 자동으로 입금 완료 처리
     */
    @GetMapping("/api/escrow/webhook/virtual-account")
    @ResponseBody
    public ResponseEntity<String> virtualAccountWebhookTest() {
        log.info("가상계좌 웹훅 테스트 요청 (GET) - 자동 입금 처리 시작");
        
        try {
            // 입금 대기 중인 가상계좌 목록 조회
            List<VirtualAccountDepositResponse> waitingAccounts = 
                virtualAccountService.getVirtualAccountsByStatus(
                    com.example.payflow.escrow.domain.VirtualAccountStatus.WAITING_FOR_DEPOSIT
                );
            
            if (waitingAccounts.isEmpty()) {
                log.warn("입금 대기 중인 가상계좌가 없습니다");
                return ResponseEntity.ok("No waiting virtual accounts found. Please issue a virtual account first.");
            }
            
            // 가장 최근 가상계좌 선택 (마지막 항목)
            VirtualAccountDepositResponse latestAccount = waitingAccounts.get(waitingAccounts.size() - 1);
            
            log.info("자동 입금 처리 대상: orderId={}, accountNumber={}, transactionId={}", 
                latestAccount.getOrderId(), 
                latestAccount.getVirtualAccountNumber(),
                latestAccount.getTransactionId());
            
            // 입금 완료 처리
            virtualAccountService.completeVirtualAccountDeposit(
                latestAccount.getOrderId(),
                "테스트입금자"
            );
            
            log.info("자동 입금 처리 완료: orderId={}, transactionId={}", 
                latestAccount.getOrderId(), latestAccount.getTransactionId());
            
            return ResponseEntity.ok(String.format(
                "✅ Auto deposit completed!\n\n" +
                "OrderId: %s\n" +
                "Account: %s %s\n" +
                "TransactionId: %s\n\n" +
                "You can now proceed to the process execution page.",
                latestAccount.getOrderId(),
                latestAccount.getBankName(),
                latestAccount.getVirtualAccountNumber(),
                latestAccount.getTransactionId()
            ));
            
        } catch (Exception e) {
            log.error("자동 입금 처리 실패", e);
            return ResponseEntity.internalServerError()
                .body("❌ Auto deposit failed: " + e.getMessage());
        }
    }
    
    @lombok.Data
    public static class PaymentConfirmRequest {
        private String paymentKey;
        private String orderId;
        private BigDecimal amount;
    }
    
    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TossWebhookRequest {
        // 토스페이먼츠 웹훅은 최상위 레벨에 데이터가 있음
        private String createdAt;
        private String secret;
        private String status;
        private String orderId;
        private String paymentKey;
        private String method;
        private Long totalAmount;
        private WebhookVirtualAccount virtualAccount;
        private java.util.List<WebhookCancel> cancels;
    }
    
    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookVirtualAccount {
        private String accountNumber;
        private String bankCode;
        private String customerName;
        private String dueDate;
        private String refundStatus;
        private Boolean expired;
        private String settlementStatus;
        private String refundReceiveAccount;
    }
    
    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookCancel {
        private String cancelReason;
        private Long cancelAmount;
        private String canceledAt;
        private String transactionKey;
        private String receiptKey;
    }
}
