package com.example.payflow.specification.application;

import com.google.gson.Gson;
import com.example.payflow.specification.domain.Specification;
import com.example.payflow.specification.domain.SpecificationItem;
import com.example.payflow.specification.domain.SpecificationRepository;
import com.example.payflow.specification.domain.ProcessingStatus;
import com.example.payflow.specification.presentation.dto.ParsedSpecificationDto;
import com.example.payflow.specification.presentation.dto.SpecificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class SpecificationService {
    
    private final SpecificationRepository repository;
    private final OCRService ocrService;
    private final LLMParsingService llmService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Gson gson;
    
    public SpecificationService(
            SpecificationRepository repository,
            OCRService ocrService,
            LLMParsingService llmService,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.ocrService = ocrService;
        this.llmService = llmService;
        this.kafkaTemplate = kafkaTemplate;
        this.gson = new Gson();
        log.info("SpecificationService initialized");
    }
    
    public SpecificationResponse uploadAndProcess(MultipartFile file) {
        try {
            // 1. 명세표 엔티티 생성
            Specification spec = Specification.builder()
                    .imagePath(file.getOriginalFilename())
                    .productName("Processing...")  // 임시 값
                    .status(ProcessingStatus.UPLOADED)
                    .createdAt(LocalDateTime.now())
                    .build();
            spec = repository.save(spec);
            log.info("Specification created: {}", spec.getId());
            
            // 2. OCR 추출
            String extractedText = ocrService.extractTextFromImage(file);
            spec.updateExtractedText(extractedText);
            spec = repository.save(spec);
            log.info("Text extracted for specification: {}", spec.getId());
            
            // 3. LLM 파싱
            ParsedSpecificationDto parsed = llmService.parseWithLLM(extractedText);
            
            // 4. 파싱 결과 저장
            List<SpecificationItem> items = new ArrayList<>();
            List<ParsedSpecificationDto.SpecItem> specs = parsed.getSpecifications();
            for (int i = 0; i < specs.size(); i++) {
                ParsedSpecificationDto.SpecItem spec_item = specs.get(i);
                items.add(SpecificationItem.builder()
                        .itemName(spec_item.getName())
                        .itemValue(spec_item.getValue())
                        .unit(spec_item.getUnit())
                        .sequence(i)
                        .build());
            }
            
            spec.updateParsedData(
                    gson.toJson(parsed), 
                    items,
                    parsed.getProductName(),
                    parsed.getCategory(),
                    parsed.getPrice(),
                    parsed.getQuantity()
            );
            spec = repository.save(spec);
            log.info("Specification parsed: {}", spec.getId());
            
            // 5. Kafka 이벤트 발행
            publishEvent(spec);
            
            return SpecificationResponse.from(spec);
        } catch (Exception e) {
            log.error("Error processing specification", e);
            throw new RuntimeException("명세표 처리 실패: " + e.getMessage(), e);
        }
    }
    
    @Transactional(readOnly = true)
    public SpecificationResponse getSpecification(Long id) {
        Specification spec = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("명세표를 찾을 수 없습니다: " + id));
        return SpecificationResponse.from(spec);
    }
    
    @Transactional(readOnly = true)
    public List<SpecificationResponse> getAllSpecifications() {
        return repository.findAll().stream()
                .map(SpecificationResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<SpecificationResponse> getSpecificationsByStatus(ProcessingStatus status) {
        return repository.findByStatus(status).stream()
                .map(SpecificationResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<SpecificationResponse> searchByProductName(String productName) {
        return repository.findByProductNameContaining(productName).stream()
                .map(SpecificationResponse::from)
                .collect(Collectors.toList());
    }
    
    private void publishEvent(Specification spec) {
        try {
            if (kafkaTemplate != null) {
                String event = gson.toJson(new SpecificationEvent(
                        spec.getId(),
                        spec.getProductName(),
                        spec.getStatus().name(),
                        LocalDateTime.now()
                ));
                kafkaTemplate.send("SpecificationProcessed", event);
                log.info("Event published for specification: {}", spec.getId());
            }
        } catch (Exception e) {
            log.warn("Failed to publish event (non-critical): {}", e.getMessage());
        }
    }
    
    // 이벤트 DTO
    private static class SpecificationEvent {
        public Long specificationId;
        public String productName;
        public String status;
        public LocalDateTime timestamp;
        
        SpecificationEvent(Long specificationId, String productName, String status, LocalDateTime timestamp) {
            this.specificationId = specificationId;
            this.productName = productName;
            this.status = status;
            this.timestamp = timestamp;
        }
    }
}
