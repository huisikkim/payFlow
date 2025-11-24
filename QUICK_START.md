# 🚀 명세표 OCR + LLM 파싱 시스템 - 빠른 시작 가이드

## 5분 안에 시작하기

### 1️⃣ Docker 서비스 시작 (2분)

```bash
# 전체 서비스 시작
docker-compose up -d

# 모델 다운로드 (첫 실행 시만, 약 5-10분)
docker exec ollama ollama pull qwen2.5:7b

# 상태 확인
docker ps
```

### 2️⃣ 애플리케이션 실행 (2분)

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

### 3️⃣ 웹 브라우저에서 접속 (1분)

```
http://localhost:8080/specification/upload
```

## 🎯 사용 방법

### 1. 명세표 업로드
- 웹 페이지에서 이미지 파일 선택 또는 드래그 앤 드롭
- "업로드 및 처리" 버튼 클릭
- 3-8초 후 결과 표시

### 2. 결과 확인
- 추출된 텍스트 (OCR)
- 파싱된 JSON
- 정규화된 명세 항목
- 원본 이미지

### 3. 목록 조회
```
http://localhost:8080/specification
```

## 📊 API 사용 예시

### 업로드
```bash
curl -X POST http://localhost:8080/api/specifications/upload \
  -F "file=@명세표.png"
```

### 조회
```bash
# 상세 조회
curl http://localhost:8080/api/specifications/1

# 목록 조회
curl http://localhost:8080/api/specifications

# 검색
curl "http://localhost:8080/api/specifications/search?productName=테스트"
```

## 🧪 테스트

```bash
./test-specification-api.sh
```

## 📁 주요 파일

| 파일 | 설명 |
|------|------|
| `src/main/java/com/example/specification/` | 명세표 모듈 |
| `src/main/resources/templates/specification/` | 웹 UI |
| `ocr_service.py` | OCR 스크립트 |
| `docker-compose.yml` | Docker 설정 |
| `SPECIFICATION_SETUP.md` | 상세 설정 가이드 |

## ⚙️ 설정

### application.properties
```properties
ocr.upload-dir=uploads/specifications
ocr.python-script=ocr_service.py
ollama.url=http://localhost:11434
ollama.model=qwen2.5:7b
```

## 🔧 문제 해결

### Ollama 연결 실패
```bash
curl http://localhost:11434/api/tags
docker restart ollama
```

### 메모리 부족
```bash
# 더 가벼운 모델 사용
ollama pull phi:3
# application.properties에서 ollama.model=phi:3 로 변경
```

### PaddleOCR 오류
```bash
python3 ocr_service.py test_images/test_spec.png
```

## 📈 성능

| 항목 | 시간 |
|------|------|
| OCR 추출 | 1-3초 |
| LLM 파싱 | 2-5초 |
| 총 처리 | 3-8초 |

## 💡 팁

1. **첫 실행**: 모델 다운로드에 5-10분 소요
2. **메모리**: 최소 8GB RAM 권장
3. **디스크**: 최소 20GB 여유 공간 필요
4. **GPU**: NVIDIA GPU 있으면 더 빠름

## 📚 더 알아보기

- [SPECIFICATION_SETUP.md](SPECIFICATION_SETUP.md) - 상세 설정
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - 구현 상세
- [README.md](README.md) - 전체 프로젝트 문서

## 🎉 완료!

이제 명세표 OCR + LLM 파싱 시스템을 사용할 준비가 되었습니다!

```bash
# 한 줄로 시작하기
docker-compose up -d && docker exec ollama ollama pull qwen2.5:7b && ./gradlew bootRun
```