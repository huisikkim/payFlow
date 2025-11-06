package com.example.payflow.payment.application;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final OrderService orderService;
    
    @Transactional
    public void createPayment(String orderId, String orderName, Long amount, String customerEmail) {
        Payment payment = new Payment(orderId, orderName, amount, customerEmail);
        paymentRepository.save(payment);
        log.info("결제 생성: orderId={}", orderId);
    }
    
    @Transactional
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다"));
        
        // 토스페이먼츠 API 호출
        TossPaymentConfirmRequest confirmRequest = new TossPaymentConfirmRequest(
            request.getPaymentKey(),
            request.getOrderId(),
            request.getAmount()
        );
        
        try {
            TossPaymentResponse tossResponse = tossPaymentsClient.confirmPayment(confirmRequest);
            
            // 결제 승인 처리
            payment.approve(tossResponse.getPaymentKey(), tossResponse.getMethod());
            
            // 주문 확정 처리
            orderService.confirmOrder(payment.getOrderId());
            
            log.info("결제 승인 완료: orderId={}, paymentKey={}", 
                payment.getOrderId(), payment.getPaymentKey());
            
            return PaymentResponse.from(payment);
        } catch (Exception e) {
            payment.fail();
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
