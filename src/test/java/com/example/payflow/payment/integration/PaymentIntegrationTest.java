package com.example.payflow.payment.integration;

import com.example.payflow.payment.application.PaymentService;
import com.example.payflow.payment.domain.Payment;
import com.example.payflow.payment.domain.PaymentRepository;
import com.example.payflow.payment.domain.PaymentStatus;
import com.example.payflow.payment.presentation.dto.PaymentResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * Payment 통합 테스트
 * 실제 DB와 Spring Context를 사용한 E2E 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Payment 통합 테스트")
class PaymentIntegrationTest {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
    }
    
    @Test
    @DisplayName("결제 생성부터 조회까지 전체 플로우 테스트")
    void 결제_생성_및_조회_플로우() {
        // Given
        String orderId = "ORDER-TEST-001";
        String orderName = "통합 테스트 상품";
        Long amount = 50000L;
        String customerEmail = "integration@test.com";
        
        // When: 결제 생성
        paymentService.createPayment(orderId, orderName, amount, customerEmail);
        
        // Then: DB에 저장 확인
        Payment savedPayment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getOrderId()).isEqualTo(orderId);
        assertThat(savedPayment.getOrderName()).isEqualTo(orderName);
        assertThat(savedPayment.getAmount()).isEqualTo(amount);
        assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.READY);
        
        // When: 결제 조회
        PaymentResponse response = paymentService.getPayment(orderId);
        
        // Then: 조회 결과 확인
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getAmount()).isEqualTo(amount);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.READY);
    }
    
    @Test
    @DisplayName("여러 결제를 생성하고 각각 조회할 수 있다")
    void 다중_결제_생성_및_조회() {
        // Given
        String orderId1 = "ORDER-001";
        String orderId2 = "ORDER-002";
        String orderId3 = "ORDER-003";
        
        // When: 3개의 결제 생성
        paymentService.createPayment(orderId1, "상품1", 10000L, "user1@test.com");
        paymentService.createPayment(orderId2, "상품2", 20000L, "user2@test.com");
        paymentService.createPayment(orderId3, "상품3", 30000L, "user3@test.com");
        
        // Then: 각각 조회 가능
        PaymentResponse payment1 = paymentService.getPayment(orderId1);
        PaymentResponse payment2 = paymentService.getPayment(orderId2);
        PaymentResponse payment3 = paymentService.getPayment(orderId3);
        
        assertThat(payment1.getAmount()).isEqualTo(10000L);
        assertThat(payment2.getAmount()).isEqualTo(20000L);
        assertThat(payment3.getAmount()).isEqualTo(30000L);
        
        // DB에 3개 저장 확인
        long count = paymentRepository.count();
        assertThat(count).isEqualTo(3);
    }
    
    @Test
    @DisplayName("결제 상태 변경이 DB에 반영된다")
    void 결제_상태_변경_영속성() {
        // Given: 결제 생성
        String orderId = "ORDER-STATUS-TEST";
        paymentService.createPayment(orderId, "상태 테스트", 10000L, "test@test.com");
        
        // When: 결제 승인 (도메인 로직)
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        payment.approve("TOSS-KEY-123", "카드");
        paymentRepository.save(payment);
        
        // Then: 변경사항이 DB에 반영됨
        Payment updatedPayment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(updatedPayment.getPaymentKey()).isEqualTo("TOSS-KEY-123");
        assertThat(updatedPayment.getMethod()).isEqualTo("카드");
        assertThat(updatedPayment.getApprovedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("결제 실패 상태가 DB에 반영된다")
    void 결제_실패_상태_영속성() {
        // Given: 결제 생성
        String orderId = "ORDER-FAIL-TEST";
        paymentService.createPayment(orderId, "실패 테스트", 10000L, "test@test.com");
        
        // When: 결제 실패 처리
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        payment.fail();
        paymentRepository.save(payment);
        
        // Then: 실패 상태가 DB에 반영됨
        Payment failedPayment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(failedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
    
    @Test
    @DisplayName("결제 취소 상태가 DB에 반영된다")
    void 결제_취소_상태_영속성() {
        // Given: 결제 생성 및 승인
        String orderId = "ORDER-CANCEL-TEST";
        paymentService.createPayment(orderId, "취소 테스트", 10000L, "test@test.com");
        
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        payment.approve("TOSS-KEY-123", "카드");
        paymentRepository.save(payment);
        
        // When: 결제 취소
        payment.cancel();
        paymentRepository.save(payment);
        
        // Then: 취소 상태가 DB에 반영됨
        Payment cancelledPayment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(cancelledPayment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }
    
    @Test
    @DisplayName("동일한 orderId로 결제 조회 시 정상 조회된다")
    void 결제_조회_성공() {
        // Given: 결제 생성
        String orderId = "ORDER-QUERY-TEST";
        paymentService.createPayment(orderId, "조회 테스트", 10000L, "test@test.com");
        
        // When: 결제 조회
        PaymentResponse response = paymentService.getPayment(orderId);
        
        // Then: 정상 조회됨
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getOrderName()).isEqualTo("조회 테스트");
        assertThat(response.getAmount()).isEqualTo(10000L);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.READY);
    }
    
    @Test
    @DisplayName("결제 금액은 양수여야 한다")
    void 결제_금액_검증() {
        // Given & When: 정상 금액으로 결제 생성
        String orderId = "ORDER-AMOUNT-TEST";
        paymentService.createPayment(orderId, "정상 상품", 10000L, "test@test.com");
        
        // Then: 정상적으로 생성됨
        PaymentResponse response = paymentService.getPayment(orderId);
        assertThat(response.getAmount()).isPositive();
        assertThat(response.getAmount()).isEqualTo(10000L);
    }
    
    @Test
    @DisplayName("결제 생성 시간이 자동으로 기록된다")
    void 결제_생성_시간_자동_기록() {
        // Given & When
        String orderId = "ORDER-TIME-TEST";
        paymentService.createPayment(orderId, "시간 테스트", 10000L, "test@test.com");
        
        // Then
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(payment.getCreatedAt()).isNotNull();
        assertThat(payment.getCreatedAt()).isBeforeOrEqualTo(java.time.LocalDateTime.now());
    }
    
    @Test
    @DisplayName("결제 승인 시간이 자동으로 기록된다")
    void 결제_승인_시간_자동_기록() {
        // Given: 결제 생성
        String orderId = "ORDER-APPROVE-TIME";
        paymentService.createPayment(orderId, "승인 시간 테스트", 10000L, "test@test.com");
        
        // When: 결제 승인
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        payment.approve("TOSS-KEY-123", "카드");
        paymentRepository.save(payment);
        
        // Then: 승인 시간 기록 확인
        Payment approvedPayment = paymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(approvedPayment.getApprovedAt()).isNotNull();
        assertThat(approvedPayment.getApprovedAt()).isAfterOrEqualTo(approvedPayment.getCreatedAt());
    }
}
