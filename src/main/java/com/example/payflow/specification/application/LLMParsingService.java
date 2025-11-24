package com.example.payflow.specification.application;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.example.payflow.specification.presentation.dto.ParsedSpecificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LLMParsingService {
    
    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;
    
    @Value("${ollama.model:qwen2.5:7b}")
    private String model;
    
    private final RestTemplate restTemplate;
    private final Gson gson;
    
    public LLMParsingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.gson = new Gson();
    }
    
    public ParsedSpecificationDto parseWithLLM(String extractedText) {
        try {
            log.info("Parsing specification with LLM (Ollama)");
            
            // Ollama로 파싱
            String prompt = buildPrompt(extractedText);
            String jsonResponse = callOllama(prompt);
            ParsedSpecificationDto result = parseJsonResponse(jsonResponse);
            
            log.info("LLM parsing completed");
            return result;
        } catch (Exception e) {
            log.error("LLM parsing failed, using dummy data", e);
            // 실패 시 더미 데이터 반환
            return generateDummyParsedData(extractedText);
        }
    }
    
    private ParsedSpecificationDto generateDummyParsedData(String text) {
        List<ParsedSpecificationDto.SpecItem> specs = new ArrayList<>();
        specs.add(ParsedSpecificationDto.SpecItem.builder()
                .name("규격")
                .value("표준")
                .unit("개")
                .build());
        specs.add(ParsedSpecificationDto.SpecItem.builder()
                .name("원산지")
                .value("한국")
                .unit("")
                .build());
        
        return ParsedSpecificationDto.builder()
                .productName("샘플 상품")
                .category("테스트")
                .price(10000.0)
                .quantity(100)
                .specifications(specs)
                .build();
    }
    
    private String buildPrompt(String text) {
        return """
            다음 거래명세서/명세표 텍스트를 분석하여 JSON으로 변환해줘.
            
            규칙:
            1. 거래명세서인 경우: 첫 번째 품목을 productName으로, 전체 합계금액을 price로 사용
            2. 일반 명세표인 경우: 상품명과 가격을 추출
            3. 품목 목록이 있으면 specifications 배열에 포함
            4. 반드시 아래 JSON 형식으로만 응답
            
            필수 응답 형식 (다른 형식 사용 금지):
            {
              "productName": "상품명 또는 첫번째 품목명",
              "category": "카테고리 또는 거래명세서",
              "price": 가격숫자,
              "quantity": 수량숫자,
              "specifications": [
                {"name": "품목명", "value": "규격/값", "unit": "단위"}
              ]
            }
            
            명세표 텍스트:
            """ + text + """
            
            위 형식의 JSON만 반환하고 다른 설명이나 형식은 절대 사용하지 마.""";
    }
    
    private String callOllama(String prompt) {
        try {
            OllamaRequest request = new OllamaRequest(model, prompt, false);
            String requestBody = gson.toJson(request);
            
            log.debug("Calling Ollama API: {}", ollamaUrl);
            log.debug("Model: {}", model);
            
            String url = ollamaUrl + "/api/generate";
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            org.springframework.http.HttpEntity<String> entity = 
                new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            String response = restTemplate.postForObject(url, entity, String.class);
            
            if (response == null || response.trim().isEmpty()) {
                throw new RuntimeException("Ollama returned empty response");
            }
            
            log.debug("Ollama response received");
            
            // 응답에서 JSON 추출
            return extractJsonFromResponse(response);
        } catch (Exception e) {
            log.error("Ollama API call failed", e);
            throw new RuntimeException("Ollama 호출 실패: " + e.getMessage(), e);
        }
    }
    
    private String extractJsonFromResponse(String response) {
        try {
            // 응답은 여러 줄의 JSON 객체들로 구성됨
            String[] lines = response.split("\n");
            StringBuilder jsonBuilder = new StringBuilder();
            
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                
                try {
                    JsonObject obj = JsonParser.parseString(line).getAsJsonObject();
                    if (obj.has("response")) {
                        jsonBuilder.append(obj.get("response").getAsString());
                    }
                } catch (Exception e) {
                    // 파싱 실패한 줄은 무시
                }
            }
            
            String extractedJson = jsonBuilder.toString();
            log.debug("Extracted JSON from Ollama: {}", extractedJson.substring(0, Math.min(200, extractedJson.length())));
            
            return extractedJson;
        } catch (Exception e) {
            log.error("Failed to extract JSON from response", e);
            throw new RuntimeException("응답 파싱 실패: " + e.getMessage(), e);
        }
    }
    
    private ParsedSpecificationDto parseJsonResponse(String jsonStr) {
        try {
            // JSON 문자열에서 JSON 객체 추출
            String cleanJson = extractValidJson(jsonStr);
            log.info("Clean JSON to parse: {}", cleanJson.substring(0, Math.min(300, cleanJson.length())));
            
            JsonObject json = JsonParser.parseString(cleanJson).getAsJsonObject();
            
            ParsedSpecificationDto.ParsedSpecificationDtoBuilder builder = ParsedSpecificationDto.builder();
            
            if (json.has("productName")) {
                builder.productName(json.get("productName").getAsString());
            } else {
                builder.productName("상품명 없음");
            }
            
            if (json.has("category")) {
                builder.category(json.get("category").getAsString());
            } else {
                builder.category("미분류");
            }
            
            if (json.has("price")) {
                try {
                    builder.price(json.get("price").getAsDouble());
                } catch (Exception e) {
                    builder.price(0.0);
                }
            } else {
                builder.price(0.0);
            }
            
            if (json.has("quantity")) {
                try {
                    builder.quantity(json.get("quantity").getAsInt());
                } catch (Exception e) {
                    builder.quantity(0);
                }
            } else {
                builder.quantity(0);
            }
            
            List<ParsedSpecificationDto.SpecItem> specs = new ArrayList<>();
            if (json.has("specifications")) {
                json.getAsJsonArray("specifications").forEach(item -> {
                    JsonObject spec = item.getAsJsonObject();
                    specs.add(ParsedSpecificationDto.SpecItem.builder()
                            .name(spec.has("name") ? spec.get("name").getAsString() : "")
                            .value(spec.has("value") ? spec.get("value").getAsString() : "")
                            .unit(spec.has("unit") ? spec.get("unit").getAsString() : "")
                            .build());
                });
            }
            builder.specifications(specs);
            
            ParsedSpecificationDto result = builder.build();
            log.info("Parsed result: productName={}, category={}, price={}, items={}", 
                    result.getProductName(), result.getCategory(), result.getPrice(), specs.size());
            
            return result;
        } catch (Exception e) {
            log.error("Failed to parse JSON response: {}", jsonStr.substring(0, Math.min(200, jsonStr.length())), e);
            throw new RuntimeException("JSON 파싱 실패: " + e.getMessage(), e);
        }
    }
    
    private String extractValidJson(String text) {
        // 첫 번째 { 부터 마지막 } 까지 추출
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        
        if (start == -1 || end == -1 || start > end) {
            throw new RuntimeException("유효한 JSON을 찾을 수 없습니다");
        }
        
        return text.substring(start, end + 1);
    }
    
    // Ollama API 요청 DTO
    private static class OllamaRequest {
        public String model;
        public String prompt;
        public boolean stream;
        
        OllamaRequest(String model, String prompt, boolean stream) {
            this.model = model;
            this.prompt = prompt;
            this.stream = stream;
        }
    }
}
