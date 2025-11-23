package com.example.payflow.invoice.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.invoice.domain.Invoice;
import com.example.payflow.invoice.domain.InvoiceRepository;
import com.example.payflow.invoice.domain.event.InvoiceUploadedEvent;
import com.example.payflow.invoice.presentation.dto.InvoiceResponse;
import com.example.payflow.invoice.presentation.dto.InvoiceUploadResponse;
import com.example.payflow.logging.application.EventLoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final EventPublisher eventPublisher;
    private final EventLoggingService eventLoggingService;
    
    private static final String UPLOAD_DIR = "uploads/invoices/";
    
    @Transactional
    public InvoiceUploadResponse uploadInvoice(String orderId, MultipartFile file) {
        try {
            String invoiceId = "INV_" + UUID.randomUUID().toString().substring(0, 8);
            String fileName = file.getOriginalFilename();
            String filePath = saveFile(file, invoiceId);
            
            Invoice invoice = new Invoice(invoiceId, orderId, filePath, fileName);
            invoiceRepository.save(invoice);
            
            log.info("📄 명세서 업로드: invoiceId={}, orderId={}, fileName={}", invoiceId, orderId, fileName);
            
            // Kafka 이벤트 발행
            InvoiceUploadedEvent event = new InvoiceUploadedEvent(
                invoiceId,
                orderId,
                filePath,
                fileName
            );
            eventPublisher.publish(event);
            
            // 이벤트 로그
            eventLoggingService.logEvent(
                invoiceId,
                "InvoiceUploaded",
                "invoice",
                Map.of(
                    "invoiceId", invoiceId,
                    "orderId", orderId,
                    "fileName", fileName,
                    "fileSize", file.getSize()
                )
            );
            
            return InvoiceUploadResponse.from(invoice);
        } catch (IOException e) {
            log.error("❌ 명세서 업로드 실패: orderId={}", orderId, e);
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }
    
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(String invoiceId) {
        Invoice invoice = invoiceRepository.findByInvoiceId(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("명세서를 찾을 수 없습니다: " + invoiceId));
        return InvoiceResponse.from(invoice);
    }
    
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByOrderId(String orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("해당 발주의 명세서를 찾을 수 없습니다: " + orderId));
        return InvoiceResponse.from(invoice);
    }
    
    private String saveFile(MultipartFile file, String invoiceId) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String fileName = invoiceId + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());
        
        return filePath.toString();
    }
}
