# YouTube 영상 분석 API - 강화 버전

## 개요
`/api/youtube/analysis` API가 대폭 강화되어 더욱 상세한 분석 기능을 제공합니다.

## 새로 추가된 기능

### 1. 제목 분석 (Title Analysis)
- ✅ 제목 길이 분석 (최적 길이: 40-70자)
- ✅ 단어 수 계산
- ✅ 숫자 포함 여부
- ✅ 감정 단어 포함 여부
- ✅ 질문형 제목 여부
- ✅ 핵심 키워드 추출
- ✅ 제목 최적화 점수 (0-100)
- ✅ 개선 제안

### 2. SEO 분석 (SEO Analysis)
- ✅ 태그 목록 자동 추출 (설명문에서)
- ✅ 해시태그 추출 및 개수
- ✅ 경쟁 영상 태그 분석
- ✅ 추천 태그 제안
- ✅ 누락된 인기 태그 찾기
- ✅ 중복 태그 감지
- ✅ 태그 다양성 점수
- ✅ 설명문 길이 분석 (최소 250자 권장)
- ✅ 링크 포함 여부
- ✅ 타임스탬프 포함 여부
- ✅ 설명문 최적화 체크리스트
- ✅ 전체 SEO 점수

### 3. 경쟁도 분석 강화 (Competitor Analysis)
- ✅ Top 20 영상 수집 및 분석
- ✅ 조회수 중앙값/평균/최소/최대
- ✅ 참여도 중앙값/평균
- ✅ 업로드 시간 대비 조회수 증가 패턴
- ✅ 일일 조회수 성장률
- ✅ 성장 속도 카테고리화 (매우 빠름/빠름/보통/느림)
- ✅ 상위 경쟁 영상 상세 정보

### 4. CTR 추정 (Click-Through Rate Estimate)
- ✅ 예상 CTR 계산 (%)
- ✅ CTR 레벨 분류 (매우 높음/높음/보통/낮음)
- ✅ CTR 영향 요소 분석:
  - 제목 길이
  - 숫자 포함
  - 감정 단어
  - 해시태그 수
  - 썸네일 품질 (간접)
  - 채널 신뢰도
  - 설명문 품질
- ✅ 개선 제안

### 5. 영상 품질 세부 점수 (Quality Score)
- ✅ 제목 최적화 점수
- ✅ 태그 다양성 점수
- ✅ 설명문 길이 점수
- ✅ 참여도 점수 (좋아요 비율)
- ✅ 종합 품질 점수 및 등급 (S/A/B/C/D/F)
- ✅ 각 항목별 상세 피드백

## API 엔드포인트

### GET /api/youtube/analysis/{videoId}
영상 ID로 종합 분석 리포트 생성

### POST /api/youtube/analysis/url
YouTube URL로 직접 분석

**Request Body:**
```json
{
  "url": "https://www.youtube.com/watch?v=VIDEO_ID"
}
```

## 응답 예시

```json
{
  "success": true,
  "report": {
    // 기본 정보
    "videoId": "dQw4w9WgXcQ",
    "videoTitle": "완벽한 Spring Boot 입문 가이드 - 2024년 최신판",
    "channel": "DevMaster",
    "channelId": "UC...",
    "channelSubscribers": 150000,
    "thumbnailUrl": "https://...",
    "publishedAt": "2024-01-15T10:00:00Z",
    "categoryName": "교육",
    
    // 현재 통계
    "currentViews": 45000,
    "currentLikes": 2100,
    "currentComments": 350,
    "engagementRate": 5.44,
    
    // 예측 데이터
    "predictedViews": 120000,
    "predictedGrowth": 75000,
    "dailyGrowthRate": 2.5,
    
    // 수익 예측
    "minRevenue": 45000,
    "maxRevenue": 180000,
    "avgRevenue": 112500,
    "predictedRevenue": 300000,
    "revenuePotentialScore": 85,
    
    // 경쟁 분석 (기존)
    "competitionScore": 65,
    "competitionLevel": "보통",
    "recentCompetitors": 15,
    "avgCompetitorViews": 38000,
    "competitionRecommendation": "경쟁이 적당합니다...",
    
    // 태그 & 키워드
    "tags": [],
    "extractedKeywords": ["spring", "boot", "입문", "가이드", "2024"],
    
    // 종합 점수
    "overallScore": 82,
    "overallGrade": "A",
    
    // 추천 사항
    "recommendations": [
      "✅ 매우 우수한 영상입니다! 이 스타일을 유지하세요.",
      "🔥 참여율이 매우 높습니다! 알고리즘이 선호하는 콘텐츠입니다.",
      "💰 수익 잠재력이 높습니다. 광고 최적화와 스폰서십을 고려하세요."
    ],
    "recommendedTitles": [
      "완벽한 Spring Boot 입문 가이드 - 2024년 최신판 (10분 완성)",
      "Spring Boot 입문 가이드 - 2025년 최신 가이드",
      "완벽한 완벽한 Spring Boot 입문 가이드 - 2024년 최신판"
    ],
    
    // 채널 연락처
    "channelEmail": "contact@devmaster.com",
    "channelInstagram": "@devmaster",
    "channelTwitter": "@devmaster",
    "channelWebsite": "https://devmaster.com",
    
    // ===== 새로운 분석 결과 =====
    
    // 제목 분석
    "titleAnalysis": {
      "title": "완벽한 Spring Boot 입문 가이드 - 2024년 최신판",
      "length": 32,
      "optimalLength": 70,
      "isOptimalLength": false,
      "wordCount": 7,
      "hasNumbers": true,
      "hasEmotionalWords": true,
      "hasQuestionMark": false,
      "keywords": ["spring", "boot", "입문", "가이드", "2024"],
      "score": 75,
      "suggestions": [
        "제목이 너무 짧습니다. 40자 이상을 권장합니다.",
        "질문형 제목은 호기심을 유발합니다. (예: '~하는 방법은?')",
        "상위 경쟁 영상의 평균 제목 길이는 52자입니다."
      ]
    },
    
    // SEO 분석
    "seoAnalysis": {
      "currentTags": ["springboot", "java", "tutorial", "backend"],
      "tagCount": 4,
      "recommendedTags": ["programming", "coding", "developer", "webdev", "framework"],
      "missingTags": ["programming", "coding"],
      "duplicateTags": [],
      "tagDiversityScore": 65,
      "descriptionLength": 320,
      "optimalDescriptionLength": 250,
      "hasLinks": true,
      "hasTimestamps": true,
      "hasHashtags": true,
      "hashtagCount": 5,
      "hashtags": ["#SpringBoot", "#Java", "#Tutorial", "#Backend", "#Programming"],
      "descriptionScore": 85,
      "descriptionChecklist": [
        {
          "item": "설명문 길이 250자 이상",
          "checked": true,
          "recommendation": "적절한 길이입니다."
        },
        {
          "item": "관련 링크 포함 (SNS, 웹사이트 등)",
          "checked": true,
          "recommendation": "링크가 포함되어 있습니다."
        },
        {
          "item": "타임스탬프 추가",
          "checked": true,
          "recommendation": "타임스탬프가 있습니다."
        },
        {
          "item": "해시태그 사용",
          "checked": true,
          "recommendation": "해시태그를 사용하고 있습니다."
        },
        {
          "item": "태그 5개 이상",
          "checked": false,
          "recommendation": "현재 4개입니다. 최소 5개 이상의 태그를 추가하세요."
        }
      ],
      "overallSeoScore": 75
    },
    
    // 경쟁 영상 상세 분석
    "competitorAnalysis": {
      "totalCompetitors": 50,
      "top20Count": 20,
      "viewsMedian": 42000,
      "viewsAverage": 48500,
      "viewsMin": 15000,
      "viewsMax": 250000,
      "engagementMedian": 4.2,
      "engagementAverage": 4.8,
      "growthPatterns": [
        {
          "videoId": "abc123",
          "title": "Spring Boot 완전 정복",
          "views": 250000,
          "daysOld": 45,
          "viewsPerDay": 5555,
          "growthRate": "빠름"
        },
        {
          "videoId": "def456",
          "title": "Spring Boot 기초부터",
          "views": 180000,
          "daysOld": 60,
          "viewsPerDay": 3000,
          "growthRate": "보통"
        }
      ],
      "topCompetitors": [
        {
          "videoId": "abc123",
          "title": "Spring Boot 완전 정복",
          "channel": "코딩마스터",
          "views": 250000,
          "likes": 12000,
          "daysOld": 45,
          "engagementRate": 4.8
        }
      ]
    },
    
    // CTR 추정
    "ctrEstimate": {
      "estimatedCtr": 6.8,
      "ctrLevel": "높음",
      "factors": [
        {
          "factor": "제목 길이",
          "score": 15,
          "impact": "중립",
          "description": "적절한 제목 길이입니다 (32자)"
        },
        {
          "factor": "숫자 포함",
          "score": 15,
          "impact": "긍정적",
          "description": "제목에 숫자가 포함되어 있습니다"
        },
        {
          "factor": "감정 단어",
          "score": 15,
          "impact": "긍정적",
          "description": "감정을 자극하는 단어가 포함되어 있습니다"
        },
        {
          "factor": "해시태그 수",
          "score": 10,
          "impact": "긍정적",
          "description": "적절한 해시태그를 사용하고 있습니다 (5개)"
        },
        {
          "factor": "썸네일 품질 (간접)",
          "score": 15,
          "impact": "긍정적",
          "description": "조회수가 구독자 수를 초과하여 썸네일이 효과적입니다"
        },
        {
          "factor": "채널 신뢰도",
          "score": 12,
          "impact": "긍정적",
          "description": "중견 채널로 적절한 신뢰도를 가지고 있습니다"
        },
        {
          "factor": "설명문 품질",
          "score": 8,
          "impact": "긍정적",
          "description": "설명문 품질 점수: 85/100"
        }
      ],
      "improvements": []
    },
    
    // 품질 세부 점수
    "qualityScore": {
      "titleOptimizationScore": 75,
      "tagDiversityScore": 65,
      "descriptionLengthScore": 85,
      "engagementScore": 100,
      "overallScore": 82,
      "grade": "A",
      "titleDetail": {
        "score": 75,
        "status": "양호",
        "feedback": "제목이 적절하지만 개선의 여지가 있습니다. 제목이 너무 짧습니다. 40자 이상을 권장합니다."
      },
      "tagDetail": {
        "score": 65,
        "status": "양호",
        "feedback": "태그를 더 추가하면 검색 노출이 향상됩니다. 추천 태그: programming, coding, developer"
      },
      "descriptionDetail": {
        "score": 85,
        "status": "우수",
        "feedback": "설명문이 충분히 상세하고 최적화되어 있습니다."
      },
      "engagementDetail": {
        "score": 100,
        "status": "우수",
        "feedback": "참여율이 매우 높습니다 (5.44%). 시청자들이 적극적으로 반응하고 있습니다."
      }
    }
  }
}
```

## 사용 예시

### 1. Video ID로 분석
```bash
curl -X GET "http://localhost:8080/api/youtube/analysis/dQw4w9WgXcQ"
```

### 2. URL로 분석
```bash
curl -X POST "http://localhost:8080/api/youtube/analysis/url" \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.youtube.com/watch?v=dQw4w9WgXcQ"}'
```

## 주요 개선 사항

### 제목 최적화
- 제목 길이, 키워드, 감정 단어 등을 종합 분석
- 경쟁 영상과 비교하여 개선 제안 제공
- 최적화 점수로 정량적 평가

### SEO 강화
- 태그와 해시태그 자동 추출
- 경쟁 영상의 인기 태그 분석
- 누락된 태그 추천
- 설명문 최적화 체크리스트

### 경쟁 분석 고도화
- Top 20 영상의 통계 분석 (중앙값, 평균)
- 업로드 시간 대비 성장 패턴 분석
- 일일 조회수 증가율 계산
- 성장 속도 카테고리화

### CTR 예측
- 7가지 요소를 종합하여 CTR 추정
- 각 요소별 점수와 영향도 분석
- 구체적인 개선 방안 제시

### 품질 점수 세분화
- 4가지 핵심 지표별 점수
- 각 항목별 상세 피드백
- 종합 등급 (S/A/B/C/D/F)

## 제외된 기능

### Thumbnail OCR
- 썸네일 텍스트 분석은 외부 OCR 서비스(Google Vision API 등)가 필요하여 제외
- 필요시 별도로 구현 가능

## 기술 스택
- Spring Boot
- YouTube Data API v3
- Lombok
- 규칙 기반 분석 알고리즘

## 참고사항
- 모든 분석은 규칙 기반으로 구현되어 외부 AI API 없이 동작
- YouTube API 할당량을 고려하여 사용
- 경쟁 영상 검색은 최대 50개까지 수집
