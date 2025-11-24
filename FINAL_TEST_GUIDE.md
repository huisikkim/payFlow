# 명세표 OCR + LLM 파싱 최종 테스트 가이드

## 완료된 작업

### 1. 메모리 문제 해결 ✅
- Ollama 모델: `qwen2.5:7b` (4.3GB) → `qwen2.5:1.5b` (986MB)
- 메모리 사용량 대폭 감소

### 2. OCR 기능 개선 ✅
- PaddleOCR API 업데이트 적용
- 한글 거래명세서 텍스트 정확히 추출
- 테스트 완료: `abc.jpeg` 파일에서 모든 품목 정보 추출

### 3. LLM 파싱 로직 개선 ✅
- 거래명세서 전용 프롬프트 추가
- 에러 처리 및 로깅 강화
- JSON 파싱 안정성 향상

### 4. 타임아웃 설정 추가 ✅
- RestTemplate 타임아웃: 2분으로 증가
- Ollama 응답 대기 시간 확보

## 테스트 방법

### 1. 애플리케이션 시작
```bash
./gradlew bootRun
```

### 2. 거래명세서 업로드 테스트
다른 터미널에서:
```bash
curl -X POST http://localhost:8080/api/specification/upload \
  -F "file=@test_images/abc.jpeg" \
  | jq '.'
```

### 3. 결과 확인
```bash
# 최신 명세표 조회
curl -s http://localhost:8080/api/specification | jq '.[-1]'

# 특정 ID 조회 (예: ID 19)
curl -s http://localhost:8080/api/specification/19 | jq '{
  productName,
  category,
  price,
  quantity,
  items: [.items[] | {name: .itemName, value: .itemValue}]
}'
```

## 예상 결과

### OCR 추출 텍스트
```
거래일자
거래명세서
(공급자용)
2015-05-02
...
품목:
1. AAAA-001, A4, 수량 5, 단가 10,000
2. AAAA-002, A5, 수량 15, 단가 10,000
3. AAAA-003, A6, 수량 15, 단가 10,000
합계금액: 485,000원
```

### LLM 파싱 결과 (기대값)
```json
{
  "productName": "AAAA-001",
  "category": "거래명세서",
  "price": 485000.0,
  "quantity": 3,
  "items": [
    {"itemName": "AAAA-001", "itemValue": "A4"},
    {"itemName": "AAAA-002", "itemValue": "A5"},
    {"itemName": "AAAA-003", "itemValue": "A6"}
  ]
}
```

## 문제 해결

### 여전히 "샘플 상품" 반환되는 경우

1. **로그 확인**
```bash
tail -f boot-run.log | grep -i "llm\|ollama\|parsing"
```

2. **Ollama 직접 테스트**
```bash
docker exec payflow-ollama-1 ollama run qwen2.5:1.5b "Hello"
```
- 5-10초 이내 응답: 정상
- 30초 이상: 문제 있음 → Docker 재시작 필요

3. **Docker 리소스 증가**
Docker Desktop → Settings → Resources
- Memory: 최소 4GB 이상
- CPU: 최소 2 cores 이상

4. **대안: 더미 데이터 대신 OCR 텍스트 기반 파싱**
LLM이 계속 실패하면, 정규식으로 품목 추출하는 fallback 로직 추가 가능

## 개선 사항

### 코드 변경 사항
1. `ocr_service.py`: PaddleOCR API 업데이트
2. `LLMParsingService.java`: 프롬프트 개선, 로깅 추가
3. `RestTemplateConfig.java`: 타임아웃 설정 추가
4. `application.properties`: 모델 변경 (1.5b)

### 성능 개선
- 모델 크기: 4.7GB → 986MB (79% 감소)
- 메모리 사용: 예상 1.5GB 이하
- 응답 속도: 개선 예상 (하지만 여전히 느릴 수 있음)

## 다음 단계

LLM 파싱이 여전히 느리거나 불안정한 경우:
1. OpenAI API 사용 (빠르고 안정적)
2. 정규식 기반 파싱 (LLM 없이)
3. 더 강력한 서버에서 Ollama 실행
