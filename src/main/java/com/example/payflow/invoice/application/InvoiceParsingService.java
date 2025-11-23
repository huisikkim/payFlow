package com.example.payflow.invoice.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.invoice.domain.Invoice;
import com.example.payflow.invoice.domain.InvoiceItem;
import com.example.payflow.invoice.domain.InvoiceRepository;
import com.example.payflow.invoice.domain.event.InvoiceParsedEvent;
import com.example.payflow.logging.application.EventLoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceParsingService {
    
    private final InvoiceRepository invoiceRepository;
    private final EventPublisher eventPublisher;
    private final EventLoggingService eventLoggingService;
    
    @Transactional
    public void parseInvoice(String invoiceId) {
        Invoice invoice = invoiceRepository.findByInvoiceId(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("명세서를 찾을 수 없습니다: " + invoiceId));
        
        try {
            invoice.startParsing();
            invoiceRepository.save(invoice);
            
            log.info("🔍 명세서 파싱 시작: invoiceId={}", invoiceId);
            
            // 간단한 CSV/TXT 파싱
            List<String> lines = Files.readAllLines(Paths.get(invoice.getFilePath()));
            parseLines(invoice, lines);
            
            invoice.completeParsing();
            invoiceRepository.save(invoice);
            
            Long totalAmount = invoice.getItems().stream()
                .mapToLong(InvoiceItem::getAmount)
                .sum();
            
            log.info("✅ 명세서 파싱 완료: invoiceId={}, itemCount={}, totalAmount={}", 
                invoiceId, invoice.getItems().size(), totalAmount);
            
            // Kafka 이벤트 발행
            InvoiceParsedEvent event = new InvoiceParsedEvent(
                invoiceId,
                invoice.getOrderId(),
                invoice.getItems().size(),
                totalAmount
            );
            eventPublisher.publish(event);
            
            // 이벤트 로그
            eventLoggingService.logEvent(
                invoiceId,
                "InvoiceParsed",
                "invoice",
                Map.of(
                    "invoiceId", invoiceId,
                    "orderId", invoice.getOrderId(),
                    "itemCount", invoice.getItems().size(),
                    "totalAmount", totalAmount
                )
            );
            
        } catch (Exception e) {
            log.error("❌ 명세서 파싱 실패: invoiceId={}", invoiceId, e);
            invoice.failParsing(e.getMessage());
            invoiceRepository.save(invoice);
            
            eventLoggingService.logFailedEvent(
                invoiceId,
                "InvoiceParsingFailed",
                "invoice",
                e.getMessage()
            );
        }
    }
    
    private void parseLines(Invoice invoice, List<String> lines) throws IOException {
        // 간단한 CSV 파싱 (품목명,수량,단가,단위)
        // 첫 줄은 헤더로 스킵
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                try {
                    String itemName = parts[0].trim();
                    Integer quantity = Integer.parseInt(parts[1].trim());
                    Long unitPrice = Long.parseLong(parts[2].trim());
                    String unit = parts.length > 3 ? parts[3].trim() : "개";
                    
                    InvoiceItem item = new InvoiceItem(itemName, quantity, unitPrice, unit);
                    invoice.addItem(item);
                    
                } catch (NumberFormatException e) {
                    log.warn("⚠️ 파싱 실패한 라인: {}", line);
                }
            }
        }
    }
}
