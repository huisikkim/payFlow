package com.example.payflow.payment.application;

import com.example.payflow.logging.application.EventLoggingService;
import com.example.payflow.logging.application.PaymentEventSourcingService;
import com.example.payflow.order.application.OrderService;
import com.example.payflow.payment.domain.Payment;
import com.example.payflow.payment.domain.PaymentRepository;
import com.example.payflow.payment.infrastructure.TossPaymentsClient;
import com.example.payflow.payment.infrastructure.dto.TossPaymentConfirmRequest;
import com.example.payflow.payment.infrastructure.dto.TossPaymentResponse;
import com.example.payflow.payment.presentation.dto.PaymentConfirmRequest;
import com.example.payflow.payment.presentation.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final OrderService orderService;
    private final EventLoggingService eventLoggingService;
    private final PaymentEventSourcingService eventSourcingService;
    
    @Transactional
    public void createPayment(String orderId, String orderName, Long amount, String customerEmail) {
        long startTime = System.currentTimeMillis();
        
        Payment payment = new Payment(orderId, orderName, amount, customerEmail);
        paymentRepository.save(payment);
        
        // 이벤트 소싱: 결제 생성 이벤트 저장
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("orderId", orderId);
        eventData.put("orderName", orderName);
        eventData.put("amount", amount);
        eventData.put("customerEmail", customerEmail);
        
        eventSourcingService.storePaymentEvent(
            orderId,
            "PaymentCreated",
            null,
            "PENDING",
            eventData,
            "SYSTEM"
        );
        
        // 이벤트 로그 저장
        long processingTime = System.currentTimeMillis() - startTime;
        eventLoggingService.logEvent(orderId, "PaymentCreated", "payment", eventData);
        
        log.info("결제 생성: orderId={}, processingTime={}ms", orderId, processingTime);
    }
    
    @Transactional
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 결제 정보 조회 (없으면 자동 생성)
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
            .orElseGet(() -> {
                log.warn("결제 정보가 없어 자동 생성합니다: orderId={}", request.getOrderId());
                Payment newPayment = new Payment(
                    request.getOrderId(),
                    "주문 " + request.getOrderId(),  // 기본 주문명
                    request.getAmount(),
                    "customer@example.com"  // 기본 이메일
                );
                return paymentRepository.save(newPayment);
            });
        
        String previousState = payment.getStatus().name();
        
        // 토스페이먼츠 API 호출
        TossPaymentConfirmRequest confirmRequest = new TossPaymentConfirmRequest(
            request.getPaymentKey(),
            request.getOrderId(),
            request.getAmount()
        );
        
        try {
            // 이벤트 소싱: 승인 대기 상태
            eventSourcingService.storePaymentEvent(
                payment.getOrderId(),
                "PaymentApprovalRequested",
                previousState,
                "APPROVING",
                Map.of("paymentKey", request.getPaymentKey(), "amount", request.getAmount()),
                "USER"
            );
            
            TossPaymentResponse tossResponse = tossPaymentsClient.confirmPayment(confirmRequest);
            
            // 결제 승인 처리
            payment.approve(tossResponse.getPaymentKey(), tossResponse.getMethod());
            
            // 이벤트 소싱: 승인 완료 상태
            eventSourcingService.storePaymentEvent(
                payment.getOrderId(),
                "PaymentApproved",
                "APPROVING",
                "APPROVED",
                Map.of(
                    "paymentKey", tossResponse.getPaymentKey(),
                    "method", tossResponse.getMethod(),
                    "approvedAt", tossResponse.getApprovedAt()
                ),
                "TOSS_PAYMENTS"
            );
            
            // 주문 확정 처리
            orderService.confirmOrder(payment.getOrderId());
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // 이벤트 로그 저장
            eventLoggingService.logEvent(
                payment.getOrderId(),
                "PaymentApproved",
                "payment",
                Map.of(
                    "orderId", payment.getOrderId(),
                    "paymentKey", payment.getPaymentKey(),
                    "amount", payment.getAmount(),
                    "processingTimeMs", processingTime
                )
            );
            
            log.info("결제 승인 완료: orderId={}, paymentKey={}, processingTime={}ms", 
                payment.getOrderId(), payment.getPaymentKey(), processingTime);
            
            return PaymentResponse.from(payment);
        } catch (Exception e) {
            payment.fail();
            
            // 이벤트 소싱: 실패 상태
            eventSourcingService.storePaymentEvent(
                payment.getOrderId(),
                "PaymentFailed",
                previousState,
                "FAILED",
                Map.of("errorMessage", e.getMessage()),
                "SYSTEM"
            );
            
            // 실패 이벤트 로그
            eventLoggingService.logFailedEvent(
                payment.getOrderId(),
                "PaymentFailed",
                "payment",
                e.getMessage()
            );
            
            orderService.failOrder(payment.getOrderId());
            log.error("결제 승인 실패: orderId={}", payment.getOrderId(), e);
            throw new RuntimeException("결제 승인 실패: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다"));
        return PaymentResponse.from(payment);
    }
}
