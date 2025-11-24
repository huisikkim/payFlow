# 🎯 PaddleOCR 설치 가이드

PaddleOCR은 Python 패키지로 직접 설치합니다 (Docker 이미지 문제 회피).

## 📋 사전 요구사항

- Python 3.7 이상
- pip 또는 conda

## 🚀 설치 방법

### 방법 1: pip 사용 (권장)

```bash
# 1. PaddleOCR 설치
pip install paddleocr pillow

# 2. 설치 확인
python3 -c "from paddleocr import PaddleOCR; print('OK')"
```

### 방법 2: conda 사용

```bash
# 1. PaddleOCR 설치
conda install -c conda-forge paddleocr

# 2. 설치 확인
python3 -c "from paddleocr import PaddleOCR; print('OK')"
```

### 방법 3: 소스에서 설치

```bash
# 1. 저장소 클론
git clone https://github.com/PaddlePaddle/PaddleOCR.git
cd PaddleOCR

# 2. 설치
pip install -r requirements.txt
python setup.py install
```

## ✅ 설치 확인

```bash
# Python 스크립트로 테스트
python3 << 'EOF'
from paddleocr import PaddleOCR

# OCR 초기화 (첫 실행 시 모델 다운로드)
ocr = PaddleOCR(use_angle_cls=True, lang='korean')

# 테스트 이미지로 테스트
# result = ocr.ocr('test_image.png', cls=True)
# print(result)

print("✅ PaddleOCR 설치 완료!")
EOF
```

## 🔧 설정

### application.properties

```properties
# OCR 설정
ocr.upload-dir=uploads/specifications
ocr.python-script=ocr_service.py
```

### ocr_service.py

```python
#!/usr/bin/env python3
import sys
from paddleocr import PaddleOCR

def extract_text_from_image(image_path):
    # OCR 초기화
    ocr = PaddleOCR(use_angle_cls=True, lang='korean')
    
    # 텍스트 추출
    result = ocr.ocr(image_path, cls=True)
    
    # 결과 정렬
    extracted_text = ""
    if result:
        for line in result:
            if line:
                for word_info in line:
                    text = word_info[1]
                    confidence = word_info[2]
                    if confidence > 0.7:
                        extracted_text += text + " "
                extracted_text += "\n"
    
    return extracted_text.strip()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python ocr_service.py <image_path>", file=sys.stderr)
        sys.exit(1)
    
    image_path = sys.argv[1]
    text = extract_text_from_image(image_path)
    print(text)
```

## 🧪 테스트

### 1. 간단한 테스트

```bash
# Python 스크립트 테스트
python3 ocr_service.py test_images/test_spec.png
```

### 2. 이미지 생성 및 테스트

```bash
python3 << 'EOF'
from PIL import Image, ImageDraw
import os

# 테스트 이미지 생성
img = Image.new('RGB', (400, 300), color='white')
draw = ImageDraw.Draw(img)

text = """상품명: 테스트 명세표
카테고리: 전자제품
가격: 50000
수량: 10

명세:
- 크기: 100x100mm
- 무게: 500g
- 색상: 검정색"""

draw.text((20, 20), text, fill='black')

os.makedirs('test_images', exist_ok=True)
img.save('test_images/test_spec.png')
print("✅ 테스트 이미지 생성 완료")
EOF

# OCR 테스트
python3 ocr_service.py test_images/test_spec.png
```

## 📊 성능

| 항목 | 시간 |
|------|------|
| 첫 실행 (모델 다운로드) | 5-10분 |
| 일반 이미지 처리 | 1-3초 |
| 메모리 사용 | ~500MB |

## 🐛 문제 해결

### 모듈을 찾을 수 없음

```bash
# PaddleOCR 재설치
pip install --upgrade paddleocr

# 또는 특정 버전 설치
pip install paddleocr==2.7.0.3
```

### 모델 다운로드 실패

```bash
# 수동으로 모델 다운로드
python3 << 'EOF'
from paddleocr import PaddleOCR

# 모델 다운로드 (첫 실행 시)
ocr = PaddleOCR(use_angle_cls=True, lang='korean')
print("✅ 모델 다운로드 완료")
EOF
```

### 메모리 부족

```bash
# 더 가벼운 설정 사용
python3 << 'EOF'
from paddleocr import PaddleOCR

# CPU 모드 사용
ocr = PaddleOCR(use_angle_cls=True, lang='korean', use_gpu=False)
EOF
```

### 한글 인식 안 됨

```bash
# 한글 모델 명시적 지정
python3 << 'EOF'
from paddleocr import PaddleOCR

ocr = PaddleOCR(
    use_angle_cls=True,
    lang='korean',  # 한글 지정
    det_model_dir='./inference/ch_PP-OCRv3_det_infer',
    rec_model_dir='./inference/ch_PP-OCRv3_rec_infer'
)
EOF
```

## 💡 팁

1. **첫 실행**: 모델 다운로드에 시간이 걸립니다
2. **메모리**: 최소 2GB RAM 권장
3. **네트워크**: 안정적인 인터넷 필요
4. **GPU**: NVIDIA GPU 있으면 더 빠름

## 🎯 다음 단계

1. ✅ PaddleOCR 설치
2. ✅ Docker 서비스 시작 (Kafka, Ollama)
3. ✅ 애플리케이션 실행
4. 🌐 웹 브라우저에서 접속
