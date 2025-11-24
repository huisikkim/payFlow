# ✅ 최종 설정 가이드

## 현재 상태

Docker 이미지 다운로드 중입니다. 약 2-3분 더 기다려주세요.

## 🎯 완료 후 실행 순서

### 1단계: PaddleOCR 설치 (터미널에서)

```bash
pip install paddleocr pillow
```

### 2단계: Docker 서비스 확인

```bash
# 컨테이너 실행 확인
docker ps

# 다음과 같이 표시되어야 함:
# CONTAINER ID   IMAGE                              COMMAND                  CREATED         STATUS         PORTS
# xxx            confluentinc/cp-kafka:7.5.0        "/etc/confluent/dock…"   2 minutes ago   Up 2 minutes   0.0.0.0:9092->9092/tcp
# xxx            confluentinc/cp-zookeeper:7.5.0    "/etc/confluent/dock…"   2 minutes ago   Up 2 minutes   0.0.0.0:2181->2181/tcp
# xxx            ollama/ollama:latest               "/bin/ollama serve"      2 minutes ago   Up 2 minutes   0.0.0.0:11434->11434/tcp
```

### 3단계: Ollama 모델 다운로드

```bash
# Qwen2.5:7b 다운로드 (약 5-10분)
docker exec ollama ollama pull qwen2.5:7b

# 진행 상황 확인
docker exec ollama ollama list
```

### 4단계: 애플리케이션 빌드

```bash
./gradlew clean build
```

### 5단계: 애플리케이션 실행

```bash
./gradlew bootRun
```

### 6단계: 웹 브라우저에서 접속

```
http://localhost:8080/specification/upload
```

## 📊 포트 확인

| 서비스 | 포트 | 상태 |
|--------|------|------|
| Zookeeper | 2181 | ✅ |
| Kafka | 9092 | ✅ |
| Ollama | 11434 | ✅ |
| Spring Boot | 8080 | 실행 후 |

## 🔍 상태 확인 명령어

```bash
# Docker 컨테이너 상태
docker ps

# Kafka 로그
docker logs kafka

# Ollama 로그
docker logs ollama

# Ollama 모델 확인
docker exec ollama ollama list

# Kafka 연결 테스트
docker exec kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092
```

## 🛑 서비스 중지

```bash
# Docker 서비스 중지
docker-compose down

# 애플리케이션 중지
# Ctrl + C (터미널에서 ./gradlew bootRun 실행 중인 창)
```

## 📝 설정 파일 확인

### docker-compose.yml
- Zookeeper, Kafka, Ollama 포함
- PaddleOCR은 Python으로 직접 실행

### application.properties
```properties
spring.kafka.bootstrap-servers=localhost:9092
ollama.url=http://localhost:11434
ollama.model=qwen2.5:7b
ocr.upload-dir=uploads/specifications
ocr.python-script=ocr_service.py
```

## 🚀 빠른 체크리스트

- [ ] Docker Desktop 실행 중
- [ ] `docker ps` 명령어로 3개 컨테이너 확인
- [ ] PaddleOCR 설치 완료
- [ ] Ollama 모델 다운로드 완료
- [ ] 애플리케이션 빌드 완료
- [ ] 애플리케이션 실행 중
- [ ] 웹 브라우저에서 접속 가능

## 💡 팁

1. **Docker 이미지 다운로드**: 약 2-3분 소요
2. **Ollama 모델 다운로드**: 약 5-10분 소요
3. **첫 OCR 실행**: 모델 로드에 시간 소요
4. **메모리**: 최소 8GB RAM 권장

## 🎉 완료!

모든 설정이 완료되면 명세표 이미지를 업로드하고 OCR + LLM 파싱 결과를 확인할 수 있습니다.