package com.example.payflow.invoice.presentation;

import com.example.payflow.invoice.application.InvoiceService;
import com.example.payflow.invoice.presentation.dto.InvoiceResponse;
import com.example.payflow.invoice.presentation.dto.InvoiceUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    @PostMapping("/upload")
    public ResponseEntity<InvoiceUploadResponse> uploadInvoice(
            @RequestParam String orderId,
            @RequestParam("file") MultipartFile file) {
        
        log.info("📄 명세서 업로드 요청: orderId={}, fileName={}", orderId, file.getOriginalFilename());
        InvoiceUploadResponse response = invoiceService.uploadInvoice(orderId, file);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable String invoiceId) {
        InvoiceResponse response = invoiceService.getInvoice(invoiceId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<InvoiceResponse> getInvoiceByOrderId(@PathVariable String orderId) {
        InvoiceResponse response = invoiceService.getInvoiceByOrderId(orderId);
        return ResponseEntity.ok(response);
    }
}
