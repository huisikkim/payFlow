# YouTube 영상 분석 API - 강화 버전 🚀

## 빠른 시작

### 1. 서버 실행
```bash
./gradlew bootRun
```

### 2. API 호출
```bash
# Video ID로 분석
curl http://localhost:8080/api/youtube/analysis/dQw4w9WgXcQ

# URL로 분석
curl -X POST http://localhost:8080/api/youtube/analysis/url \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.youtube.com/watch?v=dQw4w9WgXcQ"}'
```

### 3. 테스트 스크립트
```bash
./test-enhanced-analysis.sh
```

## 새로운 기능 ✨

### 📝 제목 분석
- 길이, 키워드, 감정 단어 분석
- 최적화 점수 및 개선 제안

### 🔍 SEO 분석
- 태그/해시태그 자동 추출
- 경쟁 영상 태그 분석
- 설명문 최적화 체크리스트

### 📊 경쟁도 분석
- Top 20 영상 통계 (중앙값/평균)
- 업로드 시간 대비 성장 패턴
- 일일 조회수 증가율

### 👆 CTR 추정
- 7가지 요소 종합 분석
- 예상 클릭률 계산
- 개선 방안 제시

### ⭐ 품질 점수
- 제목/태그/설명문/참여도 점수
- 종합 등급 (S/A/B/C/D/F)
- 항목별 상세 피드백

## 응답 예시

```json
{
  "success": true,
  "report": {
    "videoTitle": "완벽한 Spring Boot 입문 가이드",
    "currentViews": 45000,
    "overallScore": 82,
    "overallGrade": "A",
    
    "titleAnalysis": {
      "score": 75,
      "suggestions": ["제목을 40자 이상으로 늘리세요"]
    },
    
    "seoAnalysis": {
      "tagCount": 4,
      "recommendedTags": ["programming", "coding"],
      "overallSeoScore": 75
    },
    
    "competitorAnalysis": {
      "viewsMedian": 42000,
      "viewsAverage": 48500,
      "top20Count": 20
    },
    
    "ctrEstimate": {
      "estimatedCtr": 6.8,
      "ctrLevel": "높음"
    },
    
    "qualityScore": {
      "overallScore": 82,
      "grade": "A"
    }
  }
}
```

## 문서

- **상세 API 문서**: `YOUTUBE_ANALYSIS_ENHANCED.md`
- **구현 상세**: `YOUTUBE_ANALYSIS_IMPLEMENTATION.md`
- **테스트 스크립트**: `test-enhanced-analysis.sh`

## 기술 스택

- Spring Boot
- YouTube Data API v3
- Lombok
- 규칙 기반 분석 알고리즘

## 주의사항

⚠️ YouTube API 할당량 관리 필요
⚠️ 경쟁 영상 검색 시 응답 시간 고려

## 제외된 기능

❌ **Thumbnail OCR** - 외부 OCR 서비스 필요

## 문의

구현 관련 문의사항은 문서를 참고하세요.
