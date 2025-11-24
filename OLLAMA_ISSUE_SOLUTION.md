# Ollama 성능 문제 및 해결 방법

## 현재 상황
1. ✅ OCR 텍스트 추출: 정상 작동 (거래명세서 텍스트 정확히 추출)
2. ❌ LLM 파싱: Ollama 응답 시간이 1-2분 이상 소요되어 타임아웃 발생
3. 결과: 더미 데이터("샘플 상품") 반환

## 문제 원인
- `qwen2.5:3b` 모델이 CPU에서 실행되어 매우 느림
- Docker 컨테이너의 리소스 제한
- 긴 프롬프트 처리 시간

## 해결 방법

### 방법 1: 더 작은 모델 사용 (권장)
```bash
# 1.5b 모델로 변경 (더 빠름)
docker exec payflow-ollama-1 ollama pull qwen2.5:1.5b
```

그리고 `application.properties` 수정:
```properties
ollama.model=qwen2.5:1.5b
```

### 방법 2: Ollama 재시작
```bash
docker restart payflow-ollama-1
```

### 방법 3: GPU 가속 활성화 (Mac M1/M2/M3인 경우)
Docker Desktop 설정에서 GPU 사용 활성화

### 방법 4: RestTemplate 타임아웃 증가
`src/main/java/com/example/payflow/config/RestTemplateConfig.java` 생성:
```java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(120000); // 2분
        factory.setReadTimeout(120000);    // 2분
        return new RestTemplate(factory);
    }
}
```

### 방법 5: 프롬프트 단순화 (이미 적용됨)
코드에서 프롬프트를 개선했지만, 여전히 느릴 수 있습니다.

## 테스트 방법

### 1. Ollama 응답 속도 테스트
```bash
time docker exec payflow-ollama-1 ollama run qwen2.5:3b "Hello"
```

정상: 5-10초 이내
문제: 30초 이상

### 2. 애플리케이션 재시작 후 테스트
```bash
./gradlew bootRun
```

다른 터미널에서:
```bash
curl -X POST http://localhost:8080/api/specification/upload \
  -F "file=@test_images/abc.jpeg"
```

## 권장 조치
1. **즉시**: Ollama 재시작
2. **단기**: 1.5b 모델로 변경
3. **장기**: GPU 가속 또는 외부 LLM API 사용 (OpenAI, Claude 등)
