#!/usr/bin/env python3
"""
PaddleOCR 기반 명세표 텍스트 추출 스크립트
"""

import sys
import json
from paddleocr import PaddleOCR

def extract_text_from_image(image_path):
    """
    이미지에서 텍스트 추출
    
    Args:
        image_path: 이미지 파일 경로
        
    Returns:
        추출된 텍스트
    """
    try:
        # PaddleOCR 초기화 (한글 지원)
        ocr = PaddleOCR(lang='korean')
        
        # 이미지에서 텍스트 추출
        result = ocr.predict(image_path)
        
        # 추출된 텍스트 정렬
        extracted_text = ""
        
        if result and isinstance(result, list) and len(result) > 0:
            # 첫 번째 페이지 결과 가져오기
            page_result = result[0]
            
            if isinstance(page_result, dict):
                # rec_texts와 rec_scores 추출
                texts = page_result.get('rec_texts', [])
                scores = page_result.get('rec_scores', [])
                
                for i, text in enumerate(texts):
                    score = scores[i] if i < len(scores) else 1.0
                    # 신뢰도 60% 이상인 텍스트만 포함
                    if score > 0.6:
                        extracted_text += text + "\n"
        
        return extracted_text.strip() if extracted_text.strip() else "텍스트를 추출할 수 없습니다."
    
    except Exception as e:
        import traceback
        print(f"Error: {str(e)}", file=sys.stderr)
        print(traceback.format_exc(), file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python ocr_service.py <image_path>", file=sys.stderr)
        sys.exit(1)
    
    image_path = sys.argv[1]
    text = extract_text_from_image(image_path)
    print(text)
