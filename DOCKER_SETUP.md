# 🐳 Docker 설정 가이드

## 현재 설정

docker-compose.yml에는 다음 서비스만 포함됩니다:
- **Zookeeper** (Kafka 의존성)
- **Kafka** (메시징)
- **Ollama** (LLM 서버)

## PaddleOCR 설정

PaddleOCR은 Python으로 직접 실행합니다 (Docker 이미지 문제 회피).

### 1단계: Python 패키지 설치

```bash
# PaddleOCR 설치
pip install paddleocr pillow

# 또는 conda 사용
conda install -c conda-forge paddleocr
```

### 2단계: Docker 서비스 시작

```bash
# Kafka + Ollama 시작
docker-compose up -d

# 상태 확인
docker ps
```

### 3단계: Ollama 모델 다운로드

```bash
# Qwen2.5:7b 다운로드 (약 5-10분)
docker exec ollama ollama pull qwen2.5:7b

# 또는 더 가벼운 Phi-3
docker exec ollama ollama pull phi:3

# 모델 확인
docker exec ollama ollama list
```

## 🚀 전체 실행 순서

```bash
# 1. Docker 서비스 시작
docker-compose up -d

# 2. Ollama 모델 다운로드 (첫 실행 시만)
docker exec ollama ollama pull qwen2.5:7b

# 3. 애플리케이션 실행
./gradlew bootRun

# 4. 웹 브라우저에서 접속
# http://localhost:8080/specification/upload
```

## 🔍 상태 확인

```bash
# 실행 중인 컨테이너 확인
docker ps

# Kafka 상태
docker logs kafka

# Ollama 상태
docker logs ollama

# Ollama 모델 확인
docker exec ollama ollama list
```

## 🛑 서비스 중지

```bash
# 모든 서비스 중지
docker-compose down

# 볼륨 포함 삭제
docker-compose down -v
```

## 🔧 문제 해결

### Kafka 연결 실패
```bash
# Kafka 로그 확인
docker logs kafka

# Zookeeper 상태 확인
docker logs zookeeper

# 재시작
docker-compose restart kafka
```

### Ollama 연결 실패
```bash
# Ollama 상태 확인
curl http://localhost:11434/api/tags

# 로그 확인
docker logs ollama

# 재시작
docker restart ollama
```

### 포트 충돌
```bash
# 포트 사용 확인
lsof -i :9092   # Kafka
lsof -i :11434  # Ollama
lsof -i :2181   # Zookeeper

# 프로세스 종료
kill -9 <PID>
```

## 📊 시스템 요구사항

| 항목 | 최소 | 권장 |
|------|------|------|
| RAM | 4GB | 8GB+ |
| 디스크 | 20GB | 50GB+ |
| CPU | 2 cores | 4 cores+ |

## 💡 팁

1. **첫 실행**: Ollama 모델 다운로드에 5-10분 소요
2. **메모리**: Qwen2.5:7b는 약 2GB 메모리 필요
3. **네트워크**: 안정적인 인터넷 필요
4. **GPU**: Apple Silicon Mac은 자동으로 GPU 가속 사용

## 📝 설정 파일

### docker-compose.yml
```yaml
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    ports:
      - "9092:9092"

  ollama:
    image: ollama/ollama:latest
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
```

### application.properties
```properties
# Kafka
spring.kafka.bootstrap-servers=localhost:9092

# Ollama
ollama.url=http://localhost:11434
ollama.model=qwen2.5:7b

# OCR (Python)
ocr.upload-dir=uploads/specifications
ocr.python-script=ocr_service.py
```
