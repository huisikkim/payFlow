# 🚀 명세표 OCR + LLM 파싱 시스템 - 시작하기

## 📋 5단계로 시작하기

### 1️⃣ PaddleOCR 설치 (5분)

```bash
# Python 패키지 설치
pip install paddleocr pillow

# 설치 확인
python3 -c "from paddleocr import PaddleOCR; print('✅ OK')"
```

### 2️⃣ Docker 서비스 시작 (2분)

```bash
# Kafka + Ollama 시작
docker-compose up -d

# 상태 확인
docker ps
```

### 3️⃣ Ollama 모델 다운로드 (10분)

```bash
# Qwen2.5:7b 다운로드
docker exec ollama ollama pull qwen2.5:7b

# 모델 확인
docker exec ollama ollama list
```

### 4️⃣ 애플리케이션 빌드 및 실행 (3분)

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

### 5️⃣ 웹 브라우저에서 접속 (1분)

```
http://localhost:8080/specification/upload
```

## ✅ 완료!

이제 명세표 이미지를 업로드하고 OCR + LLM 파싱 결과를 확인할 수 있습니다.

## 🔍 상태 확인

```bash
# Docker 컨테이너 확인
docker ps

# Kafka 상태
docker logs kafka

# Ollama 상태
docker logs ollama

# 애플리케이션 로그
# 터미널에서 ./gradlew bootRun 실행 중인 창 확인
```

## 📊 API 테스트

```bash
# 명세표 업로드
curl -X POST http://localhost:8080/api/specifications/upload \
  -F "file=@test_images/test_spec.png"

# 목록 조회
curl http://localhost:8080/api/specifications

# 상세 조회
curl http://localhost:8080/api/specifications/1
```

## 🌐 웹 페이지

| URL | 설명 |
|-----|------|
| `/specification/upload` | 명세표 업로드 |
| `/specification` | 명세표 목록 |
| `/specification/{id}` | 명세표 상세 |

## 🛑 서비스 중지

```bash
# Docker 서비스 중지
docker-compose down

# 애플리케이션 중지
# Ctrl + C (터미널에서 ./gradlew bootRun 실행 중인 창)
```

## 🐛 문제 해결

### PaddleOCR 설치 실패
```bash
# 재설치
pip install --upgrade paddleocr pillow
```

### Docker 서비스 시작 실패
```bash
# Docker Desktop 실행 확인
open /Applications/Docker.app

# 또는 상태 확인
docker ps
```

### Ollama 모델 다운로드 실패
```bash
# 로그 확인
docker logs ollama

# 재시도
docker exec ollama ollama pull qwen2.5:7b
```

### 애플리케이션 실행 실패
```bash
# 빌드 확인
./gradlew clean build

# 로그 확인
./gradlew bootRun 2>&1 | tail -50
```

## 📚 상세 가이드

- [DOCKER_SETUP.md](DOCKER_SETUP.md) - Docker 설정
- [PADDLEOCR_SETUP.md](PADDLEOCR_SETUP.md) - PaddleOCR 설정
- [SPECIFICATION_SETUP.md](SPECIFICATION_SETUP.md) - 명세표 시스템 설정
- [QUICK_START.md](QUICK_START.md) - 빠른 시작
- [README.md](README.md) - 전체 문서

## 💡 팁

1. **첫 실행**: 전체 약 20-30분 소요 (모델 다운로드 포함)
2. **메모리**: 최소 8GB RAM 권장
3. **디스크**: 최소 20GB 여유 공간 필요
4. **네트워크**: 안정적인 인터넷 필요

## 🎯 다음 단계

1. ✅ 명세표 이미지 업로드
2. ✅ OCR 텍스트 추출 확인
3. ✅ LLM 파싱 결과 확인
4. 📊 데이터 분석 및 활용

## 🚀 한 줄 명령어

```bash
# 모든 것을 한 번에 시작
pip install paddleocr pillow && \
docker-compose up -d && \
docker exec ollama ollama pull qwen2.5:7b && \
./gradlew bootRun
```
