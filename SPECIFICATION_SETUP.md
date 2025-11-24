# 명세표 OCR + LLM 파싱 시스템 설정 가이드

## 📋 개요

PayFlow의 명세표 OCR + LLM 파싱 시스템은 다음과 같이 구성됩니다:

- **OCR**: PaddleOCR (한글 지원)
- **LLM**: Ollama + Qwen2.5:7b (또는 Phi-3)
- **백엔드**: Spring Boot 3.5.7
- **데이터베이스**: H2 (또는 MySQL)

## 🚀 빠른 시작

### 1단계: Docker 서비스 시작

```bash
# 전체 서비스 시작 (Kafka, Ollama, PaddleOCR)
docker-compose up -d

# 또는 개별 실행

# Ollama 실행
docker run -d --name ollama -p 11434:11434 ollama/ollama:latest

# Qwen2.5:7b 모델 다운로드 (약 4.7GB, 5-10분 소요)
docker exec ollama ollama pull qwen2.5:7b

# PaddleOCR 실행
docker run -d --name paddleocr -p 8501:8501 paddlepaddle/paddleocr:latest-en
```

### 2단계: 애플리케이션 빌드 및 실행

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

### 3단계: 웹 접속

```
http://localhost:8080/specification/upload
```

## 📦 설치 상세 가이드

### Ollama 설치

#### macOS
```bash
# Homebrew로 설치
brew install ollama

# 또는 Docker로 실행
docker run -d --name ollama -p 11434:11434 ollama/ollama:latest
```

#### Linux
```bash
# 공식 설치 스크립트
curl -fsSL https://ollama.ai/install.sh | sh

# 또는 Docker
docker run -d --name ollama -p 11434:11434 ollama/ollama:latest
```

#### Windows
```bash
# 공식 설치 프로그램 다운로드
# https://ollama.ai/download

# 또는 Docker Desktop 사용
docker run -d --name ollama -p 11434:11434 ollama/ollama:latest
```

### 모델 다운로드

```bash
# Qwen2.5:7b (권장, 약 4.7GB)
ollama pull qwen2.5:7b

# 또는 Phi-3 (더 빠름, 약 2.3GB)
ollama pull phi:3

# 또는 Mistral (더 강력함, 약 4.1GB)
ollama pull mistral:7b

# 모델 확인
ollama list
```

### PaddleOCR 설치

#### Python 직접 설치
```bash
# Python 3.7+ 필요
pip install paddleocr pillow

# 테스트
python3 -c "from paddleocr import PaddleOCR; ocr = PaddleOCR(lang='korean'); print('OK')"
```

#### Docker 사용
```bash
docker run -d --name paddleocr -p 8501:8501 paddlepaddle/paddleocr:latest-en
```

## 🔧 설정

### application.properties

```properties
# OCR Configuration
ocr.upload-dir=uploads/specifications
ocr.python-script=ocr_service.py

# Ollama Configuration
ollama.url=http://localhost:11434
ollama.model=qwen2.5:7b
```

### docker-compose.yml

```yaml
services:
  ollama:
    image: ollama/ollama:latest
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    environment:
      - OLLAMA_HOST=0.0.0.0:11434

  paddleocr:
    image: paddlepaddle/paddleocr:latest-en
    ports:
      - "8501:8501"
    environment:
      - LANG=ko_KR.UTF-8

volumes:
  ollama_data:
```

## 📊 API 사용 예시

### 1. 명세표 업로드

```bash
curl -X POST http://localhost:8080/api/specifications/upload \
  -F "file=@명세표.png"
```

응답:
```json
{
  "id": 1,
  "imagePath": "uploads/specifications/uuid_filename.png",
  "extractedText": "상품명: 테스트...",
  "parsedJson": "{\"productName\": \"테스트\", ...}",
  "items": [...],
  "productName": "테스트 명세표",
  "category": "전자제품",
  "price": 50000,
  "quantity": 10,
  "status": "PARSED",
  "createdAt": "2025-11-24T12:00:00"
}
```

### 2. 명세표 조회

```bash
# 상세 조회
curl http://localhost:8080/api/specifications/1

# 목록 조회
curl http://localhost:8080/api/specifications

# 상태별 조회
curl http://localhost:8080/api/specifications/status/PARSED

# 검색
curl "http://localhost:8080/api/specifications/search?productName=테스트"
```

## 🧪 테스트

```bash
# 자동 테스트 스크립트 실행
./test-specification-api.sh
```

이 스크립트는:
1. 명세표 목록 조회
2. 테스트 이미지 생성
3. 명세표 업로드 및 처리
4. 명세표 상세 조회
5. 상태별 조회
6. 상품명 검색

## 🌐 웹 UI

### 업로드 페이지
```
http://localhost:8080/specification/upload
```
- 드래그 앤 드롭 지원
- 파일 선택 버튼
- 실시간 처리 상태 표시

### 목록 페이지
```
http://localhost:8080/specification
```
- 모든 명세표 조회
- 카드 형식 표시
- 상태별 색상 구분

### 상세 페이지
```
http://localhost:8080/specification/{id}
```
- 원본 이미지 표시
- 추출된 텍스트 표시
- 파싱된 JSON 표시
- 명세 항목 테이블

## 📈 성능 최적화

### 1. 모델 선택

| 모델 | 크기 | 속도 | 정확도 | 추천 |
|------|------|------|--------|------|
| Phi-3 | 2.3GB | ⚡⚡⚡ | ⭐⭐⭐ | 빠른 처리 |
| Qwen2.5:7b | 4.7GB | ⚡⚡ | ⭐⭐⭐⭐ | 균형 |
| Mistral:7b | 4.1GB | ⚡⚡ | ⭐⭐⭐⭐ | 강력함 |
| Llama2:7b | 3.8GB | ⚡⚡ | ⭐⭐⭐ | 다목적 |

### 2. GPU 가속 (선택사항)

```bash
# NVIDIA GPU 사용
docker run -d --name ollama \
  --gpus all \
  -p 11434:11434 \
  ollama/ollama:latest

# 모델 다운로드
docker exec ollama ollama pull qwen2.5:7b
```

### 3. 배치 처리

```java
@Scheduled(cron = "0 0 * * * *")  // 매시간
public void processPendingSpecifications() {
    List<Specification> pending = repository.findByStatus(ProcessingStatus.UPLOADED);
    pending.forEach(spec -> processSpecification(spec));
}
```

## 🐛 문제 해결

### Ollama 연결 실패

```bash
# Ollama 상태 확인
curl http://localhost:11434/api/tags

# 로그 확인
docker logs ollama

# 재시작
docker restart ollama
```

### PaddleOCR 오류

```bash
# Python 패키지 확인
pip list | grep paddle

# 재설치
pip install --upgrade paddleocr

# 테스트
python3 ocr_service.py test_images/test_spec.png
```

### 메모리 부족

```bash
# 더 가벼운 모델 사용
ollama pull phi:3

# 또는 application.properties에서 변경
ollama.model=phi:3
```

## 📚 추가 리소스

- [PaddleOCR 문서](https://github.com/PaddlePaddle/PaddleOCR)
- [Ollama 문서](https://ollama.ai)
- [Qwen2.5 모델](https://huggingface.co/Qwen/Qwen2.5-7B)
- [Spring Boot 문서](https://spring.io/projects/spring-boot)

## 💡 팁

1. **첫 실행 시간**: 모델 다운로드에 5-10분 소요
2. **메모리**: 최소 8GB RAM 권장 (Qwen2.5:7b 기준)
3. **디스크**: 최소 20GB 여유 공간 필요
4. **네트워크**: 모델 다운로드 시 안정적인 인터넷 필요

## 🎯 다음 단계

1. ✅ Docker 서비스 시작
2. ✅ 애플리케이션 빌드 및 실행
3. ✅ 웹 UI에서 명세표 업로드
4. ✅ API로 결과 조회
5. 📊 데이터 분석 및 활용
