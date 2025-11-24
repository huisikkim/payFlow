package com.example.payflow.specification.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class OCRService {
    
    @Value("${ocr.upload-dir:uploads/specifications}")
    private String uploadDir;
    
    @Value("${ocr.python-script:ocr_service.py}")
    private String pythonScript;
    
    public String extractTextFromImage(MultipartFile file) throws IOException {
        try {
            // 1. 이미지 저장
            String imagePath = saveImage(file);
            log.info("Image saved: {}", imagePath);
            
            // 2. Python OCR 스크립트 실행
            String extractedText = runOCRScript(imagePath);
            log.info("OCR extraction completed");
            
            return extractedText;
        } catch (Exception e) {
            log.error("OCR extraction failed", e);
            // 실패 시 더미 데이터 반환 (테스트용)
            log.warn("Using dummy OCR data due to error");
            return generateDummyOCRText(file.getOriginalFilename());
        }
    }
    
    private String generateDummyOCRText(String filename) {
        return """
            상품명: 샘플 상품
            카테고리: 테스트
            가격: 10000
            수량: 100
            규격: 표준
            원산지: 한국
            유통기한: 2025-12-31
            """;
    }
    
    private String saveImage(MultipartFile file) throws IOException {
        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 파일명 생성
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        
        // 파일 저장
        Files.write(filePath, file.getBytes());
        
        return filePath.toString();
    }
    
    private String runOCRScript(String imagePath) throws IOException, InterruptedException {
        // Python 스크립트 경로 확인
        File scriptFile = new File(pythonScript);
        if (!scriptFile.exists()) {
            log.warn("Python script not found at: {}", pythonScript);
            throw new RuntimeException("OCR 스크립트를 찾을 수 없습니다: " + pythonScript);
        }
        
        // Python 스크립트 실행
        ProcessBuilder pb = new ProcessBuilder(
                "python3",
                scriptFile.getAbsolutePath(),
                imagePath
        );
        
        pb.redirectErrorStream(false);
        Process process = pb.start();
        
        // 출력 읽기
        String output = new String(process.getInputStream().readAllBytes());
        String error = new String(process.getErrorStream().readAllBytes());
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            log.error("OCR script error: {}", error);
            throw new RuntimeException("OCR 스크립트 실행 실패: " + error);
        }
        
        if (output.trim().isEmpty()) {
            log.warn("OCR script returned empty output");
            throw new RuntimeException("OCR 결과가 비어있습니다");
        }
        
        return output.trim();
    }
}
