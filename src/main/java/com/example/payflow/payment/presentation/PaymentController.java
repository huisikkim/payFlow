package com.example.payflow.payment.presentation;

import com.example.payflow.payment.application.PaymentService;
import com.example.payflow.payment.presentation.dto.PaymentConfirmRequest;
import com.example.payflow.payment.presentation.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        PaymentResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String orderId) {
        PaymentResponse response = paymentService.getPayment(orderId);
        return ResponseEntity.ok(response);
    }
}
