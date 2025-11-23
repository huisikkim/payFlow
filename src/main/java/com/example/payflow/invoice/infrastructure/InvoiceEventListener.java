package com.example.payflow.invoice.infrastructure;

import com.example.payflow.invoice.application.InvoiceParsingService;
import com.example.payflow.invoice.domain.event.InvoiceUploadedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceEventListener {
    
    private final ObjectMapper objectMapper;
    private final InvoiceParsingService parsingService;
    
    @KafkaListener(topics = "InvoiceUploaded", groupId = "invoice-group")
    public void handleInvoiceUploaded(String message) {
        try {
            log.info("🎧 [Kafka] InvoiceUploaded 이벤트 수신: {}", message);
            
            InvoiceUploadedEvent event = objectMapper.readValue(message, InvoiceUploadedEvent.class);
            
            // 자동 파싱 트리거
            parsingService.parseInvoice(event.getInvoiceId());
            
        } catch (Exception e) {
            log.error("❌ InvoiceUploaded 이벤트 처리 실패", e);
        }
    }
    
    @KafkaListener(topics = "InvoiceParsed", groupId = "invoice-group")
    public void handleInvoiceParsed(String message) {
        try {
            log.info("🎧 [Kafka] InvoiceParsed 이벤트 수신: {}", message);
            // 발주 품목 매칭 등 추가 처리
        } catch (Exception e) {
            log.error("❌ InvoiceParsed 이벤트 처리 실패", e);
        }
    }
}
