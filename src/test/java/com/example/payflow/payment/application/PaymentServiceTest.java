package com.example.payflow.payment.application;

import com.example.payflow.logging.application.EventLoggingService;
import com.example.payflow.logging.application.PaymentEventSourcingService;
import com.example.payflow.order.application.OrderService;
import com.example.payflow.payment.domain.Payment;
import com.example.payflow.payment.domain.PaymentRepository;
import com.example.payflow.payment.domain.PaymentStatus;
import com.example.payflow.payment.infrastructure.TossPaymentsClient;
import com.example.payflow.payment.infrastructure.dto.TossPaymentConfirmRequest;
import com.example.payflow.payment.infrastructure.dto.TossPaymentResponse;
import com.example.payflow.payment.presentation.dto.PaymentConfirmRequest;
import com.example.payflow.payment.presentation.dto.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * PaymentService 단위 테스트
 * Mock을 활용한 비즈니스 로직 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 테스트")
class PaymentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private TossPaymentsClient tossPaymentsClient;
    
    @Mock
    private OrderService orderService;
    
    @Mock
    private EventLoggingService eventLoggingService;
    
    @Mock
    private PaymentEventSourcingService eventSourcingService;
    
    @InjectMocks
    private PaymentService paymentService;
    
    private Payment testPayment;
    
    @BeforeEach
    void setUp() {
        testPayment = new Payment("ORDER-001", "테스트 상품", 10000L, "test@example.com");
    }
    
    @Test
    @DisplayName("결제 생성 시 READY 상태로 저장된다")
    void 결제_생성_성공() {
        // Given
        String orderId = "ORDER-001";
        String orderName = "테스트 상품";
        Long amount = 10000L;
        String customerEmail = "test@example.com";
        
        given(paymentRepository.save(any(Payment.class))).willReturn(testPayment);
        
        // When
        paymentService.createPayment(orderId, orderName, amount, customerEmail);
        
        // Then
        then(paymentRepository).should(times(1)).save(any(Payment.class));
        then(eventSourcingService).should(times(1))
            .storePaymentEvent(eq(orderId), eq("PaymentCreated"), isNull(), eq("PENDING"), anyMap(), eq("SYSTEM"));
        then(eventLoggingService).should(times(1))
            .logEvent(eq(orderId), eq("PaymentCreated"), eq("payment"), anyMap());
    }
    
    @Test
    @DisplayName("결제 승인 성공 시 DONE 상태로 변경되고 주문이 확정된다")
    void 결제_승인_성공() {
        // Given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
            "TOSS-KEY-123",
            "ORDER-001",
            10000L
        );
        
        TossPaymentResponse tossResponse = TossPaymentResponse.builder()
            .paymentKey("TOSS-KEY-123")
            .orderId("ORDER-001")
            .method("카드")
            .totalAmount(10000L)
            .status("DONE")
            .approvedAt(LocalDateTime.now())
            .build();
        
        given(paymentRepository.findByOrderId("ORDER-001")).willReturn(Optional.of(testPayment));
        given(tossPaymentsClient.confirmPayment(any(TossPaymentConfirmRequest.class)))
            .willReturn(tossResponse);
        
        // When
        PaymentResponse response = paymentService.confirmPayment(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(testPayment.getPaymentKey()).isEqualTo("TOSS-KEY-123");
        assertThat(testPayment.getApprovedAt()).isNotNull();
        
        then(orderService).should(times(1)).confirmOrder("ORDER-001");
        then(eventLoggingService).should(times(1))
            .logEvent(eq("ORDER-001"), eq("PaymentApproved"), eq("payment"), anyMap());
    }
    
    @Test
    @DisplayName("결제 승인 실패 시 FAILED 상태로 변경되고 주문이 실패 처리된다")
    void 결제_승인_실패() {
        // Given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
            "INVALID-KEY",
            "ORDER-001",
            10000L
        );
        
        given(paymentRepository.findByOrderId("ORDER-001")).willReturn(Optional.of(testPayment));
        given(tossPaymentsClient.confirmPayment(any(TossPaymentConfirmRequest.class)))
            .willThrow(new RuntimeException("토스 API 오류"));
        
        // When & Then
        assertThatThrownBy(() -> paymentService.confirmPayment(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("결제 승인 실패");
        
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        
        then(orderService).should(times(1)).failOrder("ORDER-001");
        then(eventLoggingService).should(times(1))
            .logFailedEvent(eq("ORDER-001"), eq("PaymentFailed"), eq("payment"), anyString());
    }
    
    @Test
    @DisplayName("존재하지 않는 주문 ID로 결제 승인 시 예외 발생")
    void 존재하지_않는_주문_결제_승인_실패() {
        // Given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
            "TOSS-KEY-123",
            "INVALID-ORDER",
            10000L
        );
        
        given(paymentRepository.findByOrderId("INVALID-ORDER")).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> paymentService.confirmPayment(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("결제 정보를 찾을 수 없습니다");
    }
    
    @Test
    @DisplayName("결제 조회 성공")
    void 결제_조회_성공() {
        // Given
        testPayment.approve("TOSS-KEY-123", "카드");
        given(paymentRepository.findByOrderId("ORDER-001")).willReturn(Optional.of(testPayment));
        
        // When
        PaymentResponse response = paymentService.getPayment("ORDER-001");
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo("ORDER-001");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(response.getPaymentKey()).isEqualTo("TOSS-KEY-123");
    }
    
    @Test
    @DisplayName("존재하지 않는 결제 조회 시 예외 발생")
    void 존재하지_않는_결제_조회_실패() {
        // Given
        given(paymentRepository.findByOrderId("INVALID-ORDER")).willReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> paymentService.getPayment("INVALID-ORDER"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("결제 정보를 찾을 수 없습니다");
    }
    
    @Test
    @DisplayName("결제 금액이 요청 금액과 일치하지 않으면 승인 실패")
    void 결제_금액_불일치_승인_실패() {
        // Given: 10000원 결제 생성
        PaymentConfirmRequest request = new PaymentConfirmRequest(
            "TOSS-KEY-123",
            "ORDER-001",
            20000L  // 다른 금액으로 승인 시도
        );
        
        given(paymentRepository.findByOrderId("ORDER-001")).willReturn(Optional.of(testPayment));
        given(tossPaymentsClient.confirmPayment(any(TossPaymentConfirmRequest.class)))
            .willThrow(new RuntimeException("결제 금액 불일치"));
        
        // When & Then
        assertThatThrownBy(() -> paymentService.confirmPayment(request))
            .isInstanceOf(RuntimeException.class);
        
        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
    
    @Test
    @DisplayName("결제 승인 시 이벤트 소싱 기록이 남는다")
    void 결제_승인_이벤트_소싱() {
        // Given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
            "TOSS-KEY-123",
            "ORDER-001",
            10000L
        );
        
        TossPaymentResponse tossResponse = TossPaymentResponse.builder()
            .paymentKey("TOSS-KEY-123")
            .orderId("ORDER-001")
            .method("카드")
            .totalAmount(10000L)
            .status("DONE")
            .approvedAt(LocalDateTime.now())
            .build();
        
        given(paymentRepository.findByOrderId("ORDER-001")).willReturn(Optional.of(testPayment));
        given(tossPaymentsClient.confirmPayment(any(TossPaymentConfirmRequest.class)))
            .willReturn(tossResponse);
        
        // When
        paymentService.confirmPayment(request);
        
        // Then: 이벤트 소싱 3번 호출 (요청 -> 승인 대기 -> 승인 완료)
        then(eventSourcingService).should(times(1))
            .storePaymentEvent(eq("ORDER-001"), eq("PaymentApprovalRequested"), anyString(), eq("APPROVING"), anyMap(), eq("USER"));
        then(eventSourcingService).should(times(1))
            .storePaymentEvent(eq("ORDER-001"), eq("PaymentApproved"), eq("APPROVING"), eq("APPROVED"), anyMap(), eq("TOSS_PAYMENTS"));
    }
}
