package com.example.payflow.specification.presentation;

import com.example.payflow.specification.application.SpecificationService;
import com.example.payflow.specification.domain.ProcessingStatus;
import com.example.payflow.specification.presentation.dto.SpecificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/specification")
@CrossOrigin(origins = "*")
public class SpecificationController {


    
    private final SpecificationService service;
    
    public SpecificationController(SpecificationService service) {
        this.service = service;
    }
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndProcess(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일을 선택해주세요");
            }
            
            SpecificationResponse response = service.uploadAndProcess(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("업로드 실패: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getSpecification(@PathVariable Long id) {
        try {
            SpecificationResponse response = service.getSpecification(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Get specification failed", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("명세표를 찾을 수 없습니다");
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllSpecifications() {
        try {
            List<SpecificationResponse> responses = service.getAllSpecifications();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Get all specifications failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("조회 실패: " + e.getMessage());
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getSpecificationsByStatus(@PathVariable String status) {
        try {
            ProcessingStatus processingStatus = ProcessingStatus.valueOf(status.toUpperCase());
            List<SpecificationResponse> responses = service.getSpecificationsByStatus(processingStatus);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("유효하지 않은 상태입니다");
        } catch (Exception e) {
            log.error("Get specifications by status failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("조회 실패: " + e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchByProductName(@RequestParam String productName) {
        try {
            List<SpecificationResponse> responses = service.searchByProductName(productName);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Search failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("검색 실패: " + e.getMessage());
        }
    }
}
