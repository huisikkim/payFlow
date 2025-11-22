package com.example.payflow.payment.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Payment 도메인 단위 테스트
 * TDD Red-Green-Refactor 사이클로 작성
 */
@DisplayName("Payment 도메인 테스트")
class PaymentTest {
    
    @Test
    @DisplayName("결제 생성 시 READY 상태로 초기화된다")
    void 결제_생성시_READY_상태() {
        // Given
        String orderId = "ORDER-001";
        String orderName = "테스트 상품";
        Long amount = 10000L;
        String customerEmail = "test@example.com";
        
        // When
        Payment payment = new Payment(orderId, orderName, amount, customerEmail);
        
        // Then
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getOrderName()).isEqualTo(orderName);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getCustomerEmail()).isEqualTo(customerEmail);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(payment.getCreatedAt()).isNotNull();
        assertThat(payment.getApprovedAt()).isNull();
    }
    
    @Test
    @DisplayName("결제 승인 시 DONE 상태로 변경되고 승인 정보가 저장된다")
    void 결제_승인_성공() {
        // Given: READY 상태의 결제
        Payment payment = new Payment("ORDER-001", "테스트 상품", 10000L, "test@example.com");
        String paymentKey = "TOSS-KEY-123";
        String method = "카드";
        
        // When: 결제 승인
        payment.approve(paymentKey, method);
        
        // Then: DONE 상태로 변경, 승인 정보 저장
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(payment.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(payment.getMethod()).isEqualTo(method);
        assertThat(payment.getApprovedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("결제 실패 시 FAILED 상태로 변경된다")
    void 결제_실패() {
        // Given: READY 상태의 결제
        Payment payment = new Payment("ORDER-001", "테스트 상품", 10000L, "test@example.com");
        
        // When: 결제 실패 처리
        payment.fail();
        
        // Then: FAILED 상태로 변경
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
    
    @Test
    @DisplayName("결제 취소 시 CANCELED 상태로 변경된다")
    void 결제_취소() {
        // Given: DONE 상태의 결제
        Payment payment = new Payment("ORDER-001", "테스트 상품", 10000L, "test@example.com");
        payment.approve("TOSS-KEY-123", "카드");
        
        // When: 결제 취소
        payment.cancel();
        
        // Then: CANCELED 상태로 변경
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }
    
    @Test
    @DisplayName("결제 금액은 양수여야 한다")
    void 결제_금액_양수_검증() {
        // Given
        String orderId = "ORDER-001";
        String orderName = "테스트 상품";
        Long amount = 10000L;
        String customerEmail = "test@example.com";
        
        // When
        Payment payment = new Payment(orderId, orderName, amount, customerEmail);
        
        // Then
        assertThat(payment.getAmount()).isPositive();
    }
    
    @Test
    @DisplayName("승인된 결제는 paymentKey를 가진다")
    void 승인된_결제는_paymentKey_보유() {
        // Given
        Payment payment = new Payment("ORDER-001", "테스트 상품", 10000L, "test@example.com");
        
        // When
        payment.approve("TOSS-KEY-123", "카드");
        
        // Then
        assertThat(payment.getPaymentKey()).isNotNull();
        assertThat(payment.getPaymentKey()).isNotEmpty();
    }
    
    @Test
    @DisplayName("결제 상태 전이: READY -> DONE")
    void 결제_상태_전이_정상() {
        // Given: READY 상태
        Payment payment = new Payment("ORDER-001", "테스트 상품", 10000L, "test@example.com");
        PaymentStatus initialStatus = payment.getStatus();
        
        // When: 승인 처리
        payment.approve("TOSS-KEY-123", "카드");
        
        // Then: READY -> DONE
        assertThat(initialStatus).isEqualTo(PaymentStatus.READY);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DONE);
    }
    
    @Test
    @DisplayName("결제 상태 전이: READY -> FAILED")
    void 결제_상태_전이_실패() {
        // Given: READY 상태
        Payment payment = new Payment("ORDER-001", "테스트 상품", 10000L, "test@example.com");
        PaymentStatus initialStatus = payment.getStatus();
        
        // When: 실패 처리
        payment.fail();
        
        // Then: READY -> FAILED
        assertThat(initialStatus).isEqualTo(PaymentStatus.READY);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
}
