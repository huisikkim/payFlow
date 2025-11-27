package com.example.payflow.payment.infrastructure;

import com.example.payflow.payment.infrastructure.dto.TossPaymentConfirmRequest;
import com.example.payflow.payment.infrastructure.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class TossPaymentsClient {
    
    @Value("${toss.payments.secret-key}")
    private String secretKey;
    
    @Value("${toss.payments.api-url}")
    private String apiUrl;
    
    private final WebClient webClient;
    
    public TossPaymentResponse confirmPayment(TossPaymentConfirmRequest request) {
        String encodedAuth = Base64.getEncoder()
            .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        
        log.info("토스페이먼츠 결제 승인 요청: paymentKey={}, orderId={}, amount={}", 
            request.getPaymentKey(), request.getOrderId(), request.getAmount());
        
        try {
            TossPaymentResponse response = webClient.post()
                .uri(apiUrl + "/payments/confirm")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> clientResponse.bodyToMono(String.class)
                        .map(errorBody -> {
                            log.error("토스페이먼츠 에러 응답: status={}, body={}", 
                                clientResponse.statusCode(), errorBody);
                            return new RuntimeException("토스페이먼츠 API 오류: " + errorBody);
                        })
                )
                .bodyToMono(TossPaymentResponse.class)
                .block();
            
            log.info("토스페이먼츠 결제 승인 성공: paymentKey={}", response.getPaymentKey());
            return response;
        } catch (Exception e) {
            log.error("토스페이먼츠 결제 승인 실패: paymentKey={}, orderId={}, amount={}", 
                request.getPaymentKey(), request.getOrderId(), request.getAmount(), e);
            throw new RuntimeException("결제 승인 실패: " + e.getMessage(), e);
        }
    }
}
