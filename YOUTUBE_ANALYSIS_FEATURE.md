# 🎯 YouTube 영상 분석 기능

## 개요

YouTube 영상 URL을 입력하면 AI가 **수익, 조회수, 경쟁도**를 자동으로 분석하여 종합 리포트를 제공합니다.

### 주요 기능

1. **조회수 예측** - 미래 30일 후 예상 조회수 계산
2. **수익 예측** - 카테고리별 CPM 기반 광고 수익 추정
3. **경쟁도 분석** - 같은 키워드의 경쟁 영상 분석
4. **참여율 분석** - 좋아요/댓글 비율 계산
5. **제목 추천** - 더 나은 제목 제안
6. **채널 연락처** - 이메일, SNS 자동 추출

---

## 🚀 사용 방법

### 1. 웹 UI 사용

```
http://localhost:8080/youtube/analysis
```

1. YouTube URL 또는 Video ID 입력
2. "분석 시작" 버튼 클릭
3. 종합 리포트 확인

### 2. API 사용

#### VideoId 추출
```bash
GET /api/youtube/analysis/extract?url={youtube_url}
```

**예시:**
```bash
curl "http://localhost:8080/api/youtube/analysis/extract?url=https://youtube.com/watch?v=dQw4w9WgXcQ"
```

**응답:**
```json
{
  "success": true,
  "videoId": "dQw4w9WgXcQ",
  "url": "https://youtube.com/watch?v=dQw4w9WgXcQ"
}
```

#### 영상 분석 (VideoId)
```bash
GET /api/youtube/analysis/{videoId}
```

**예시:**
```bash
curl "http://localhost:8080/api/youtube/analysis/dQw4w9WgXcQ"
```

#### 영상 분석 (URL)
```bash
POST /api/youtube/analysis/url
Content-Type: application/json

{
  "url": "https://youtube.com/watch?v=dQw4w9WgXcQ"
}
```

**예시:**
```bash
curl -X POST "http://localhost:8080/api/youtube/analysis/url" \
  -H "Content-Type: application/json" \
  -d '{"url": "https://youtube.com/watch?v=dQw4w9WgXcQ"}'
```

---

## 📊 리포트 구조

```json
{
  "success": true,
  "report": {
    // 기본 정보
    "videoId": "dQw4w9WgXcQ",
    "videoTitle": "Rick Astley - Never Gonna Give You Up",
    "channel": "Rick Astley",
    "channelSubscribers": 4430000,
    "categoryName": "음악",
    
    // 현재 통계
    "currentViews": 1719456347,
    "currentLikes": 18663844,
    "currentComments": 2408399,
    "engagementRate": 1.23,
    
    // 예측 데이터
    "predictedViews": 1719456347,
    "predictedGrowth": 0,
    "dailyGrowthRate": 292176.0,
    
    // 수익 예측
    "minRevenue": 6877824000,
    "maxRevenue": 17194560000,
    "avgRevenue": 12036192000,
    "predictedRevenue": 12036192000,
    "revenuePotentialScore": 47,
    
    // 경쟁 분석
    "competitionScore": 30,
    "competitionLevel": "낮음 📉",
    "recentCompetitors": 4,
    "avgCompetitorViews": 21581,
    "competitionRecommendation": "경쟁이 낮은 편입니다...",
    
    // 종합 점수
    "overallScore": 69,
    "overallGrade": "C",
    
    // 추천 사항
    "recommendations": [
      "👍 좋은 성과를 내고 있습니다...",
      "🎯 경쟁이 낮은 블루오션입니다..."
    ],
    "recommendedTitles": [
      "완벽한 Rick Astley - Never Gonna Give You Up",
      "Rick Astley - Never Gonna Give You Up (10분 완성)"
    ],
    "extractedKeywords": ["rick", "astley", "never", "gonna", "give"],
    
    // 채널 연락처
    "channelEmail": "contact@example.com",
    "channelInstagram": "@rickastley",
    "channelTwitter": "@rickastley",
    "channelWebsite": "https://rickastley.com"
  }
}
```

---

## 🧮 분석 알고리즘

### 1. 조회수 예측 모델

```
예상 조회수 = 현재 조회수 + (일평균 조회수 × 예측 일수 × 감쇠 계수 × 채널 파워 × 참여 보너스)
```

**요소:**
- **일평균 조회수**: 현재 조회수 / 업로드 후 경과 일수
- **감쇠 계수**: `e^(-0.03 × 경과일수)` (시간이 지날수록 성장 둔화)
- **채널 파워**: 조회수 / 구독자 비율 (바이럴 지수)
- **참여 보너스**: 참여율이 높을수록 알고리즘 추천 증가

### 2. 수익 예측

```
예상 수익 = (조회수 / 1000) × CPM
```

**카테고리별 CPM (원화 기준):**

| 카테고리 | 최소 CPM | 최대 CPM |
|---------|---------|---------|
| 과학/기술 | ₩8,000 | ₩25,000 |
| 교육 | ₩6,000 | ₩20,000 |
| 하우투/스타일 | ₩5,000 | ₩15,000 |
| 브이로그 | ₩4,000 | ₩12,000 |
| 음악 | ₩4,000 | ₩10,000 |
| 게임 | ₩3,000 | ₩7,000 |
| 일반 | ₩3,000 | ₩8,000 |

### 3. 경쟁도 분석

**점수 계산 (0-100):**
- 최근 30일 경쟁자 수 (40점)
- 평균 조회수 (30점)
- 평균 참여율 (20점)
- 타겟 영상의 상대적 위치 (10점)

**경쟁 수준:**
- 80-100: 매우 높음 🔥
- 60-79: 높음 📈
- 40-59: 보통 ➡️
- 20-39: 낮음 📉
- 0-19: 매우 낮음 ✅

### 4. 종합 점수

```
종합 점수 = (수익 잠재력 × 0.4) + (경쟁 우위 × 0.3) + (현재 성과 × 0.3)
```

**등급:**
- 90-100: S (최상급)
- 80-89: A (우수)
- 70-79: B (좋음)
- 60-69: C (평균 이상)
- 50-59: D (평균)
- 0-49: F (개선 필요)

---

## 🛠️ 기술 스택

### Backend
- **Spring Boot 3.5.7** - REST API
- **YouTube Data API v3** - 영상/채널 정보 조회
- **Java 17** - 예측 알고리즘 구현

### Frontend
- **Thymeleaf** - 서버 사이드 렌더링
- **Vanilla JavaScript** - 동적 UI
- **Material Symbols** - 아이콘

### 핵심 클래스
```
youtube/
├── domain/
│   ├── ViewPredictionModel.java      # 조회수 예측
│   ├── CompetitionAnalyzer.java      # 경쟁도 분석
│   ├── RevenueEstimator.java         # 수익 예측
│   └── VideoAnalysisReport.java      # 리포트 모델
├── application/
│   └── VideoAnalysisService.java     # 종합 분석 서비스
└── presentation/
    └── VideoAnalysisController.java  # REST API
```

---

## 🧪 테스트

### 자동 테스트 스크립트
```bash
./test-video-analysis.sh
```

### 수동 테스트
```bash
# 1. VideoId 추출
curl "http://localhost:8080/api/youtube/analysis/extract?url=https://youtube.com/watch?v=dQw4w9WgXcQ"

# 2. 영상 분석
curl "http://localhost:8080/api/youtube/analysis/dQw4w9WgXcQ" | jq '.'

# 3. URL로 분석
curl -X POST "http://localhost:8080/api/youtube/analysis/url" \
  -H "Content-Type: application/json" \
  -d '{"url": "https://youtube.com/watch?v=dQw4w9WgXcQ"}' | jq '.'
```

---

## 📈 활용 사례

### 1. 크리에이터
- 영상 업로드 전 수익성 예측
- 경쟁 키워드 분석
- 제목 최적화

### 2. 마케터
- 인플루언서 발굴
- 채널 연락처 자동 수집
- ROI 예측

### 3. 투자자
- 채널 가치 평가
- 성장 가능성 분석
- 수익 잠재력 계산

---

## 🔮 향후 개선 계획

### Phase 1 (완료 ✅)
- [x] 조회수 예측 모델
- [x] 수익 예측
- [x] 경쟁도 분석
- [x] 웹 UI

### Phase 2 (예정)
- [ ] OpenAI 기반 제목 추천
- [ ] 썸네일 분석
- [ ] 업로드 최적 시간 추천
- [ ] 태그 추천

### Phase 3 (예정)
- [ ] 사용자 인증 & 분석 횟수 제한
- [ ] Stripe 결제 연동
- [ ] 구독 플랜 (무료 1회/일, 유료 무제한)
- [ ] PDF 리포트 다운로드

---

## 🔐 보안 & 제한

### API 제한
- 현재: 인증 없이 무제한 사용 가능
- 프로덕션: 사용자별 일일 제한 필요

### YouTube API Quota
- 일일 할당량: 10,000 units
- 영상 분석 1회당: ~10 units
- 예상 일일 분석 가능 횟수: ~1,000회

---

## 📞 문의

- GitHub Issues: [프로젝트 이슈 페이지]
- Email: [이메일 주소]

---

## 📄 라이선스

MIT License

---

**Made with ❤️ by PayFlow Team**
