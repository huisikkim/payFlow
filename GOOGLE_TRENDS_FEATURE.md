# 구글 트렌드 기능 가이드

## 📊 개요

YouTube 헤더에 **구글 트렌드** 메뉴가 추가되었습니다. 실시간으로 한국에서 가장 핫한 검색어와 뉴스를 확인하고, 바로 YouTube에서 관련 영상을 검색할 수 있습니다.

## ✨ 주요 기능

### 1. 실시간 트렌드 조회
- 한국에서 가장 핫한 검색어 15개 표시
- 검색량 정보 (예: 500K+ 검색)
- 트렌드 순위 표시

### 2. YouTube 검색 연동
- 트렌드 키워드 클릭 시 바로 YouTube 검색
- 원클릭으로 관련 영상 찾기

### 3. 뉴스 링크
- 관련 뉴스 기사로 바로 이동
- Google 검색 결과 페이지 연결

## 🎯 사용 방법

### 웹 브라우저에서 사용

1. **구글 트렌드 페이지 접속**
   ```
   http://localhost:8080/youtube/trends
   ```
   또는 YouTube 페이지 헤더에서 "구글 트렌드" 메뉴 클릭

2. **트렌드 확인**
   - 실시간 트렌드 목록 자동 로드
   - 순위별로 정렬된 핫한 검색어 확인

3. **YouTube 검색**
   - 트렌드 카드의 "YouTube 검색" 버튼 클릭
   - 자동으로 YouTube 인기 페이지로 이동하여 검색 실행

4. **뉴스 확인**
   - 뉴스 아이콘 클릭 시 관련 기사 페이지로 이동

## 🔧 API 엔드포인트

### 트렌드 조회
```bash
GET /api/youtube/trends
```

**응답 예시:**
```json
{
  "success": true,
  "count": 15,
  "trends": [
    {
      "title": "BTS 신곡 발매",
      "description": "방탄소년단이 새로운 앨범을 발표했습니다",
      "newsUrl": "https://www.google.com/search?q=BTS+신곡+발매",
      "imageUrl": null,
      "traffic": "500K+ 검색",
      "publishedAt": "2025-12-04T17:18:41",
      "rank": 1
    }
  ]
}
```

### 테스트 스크립트
```bash
./test-trends-api.sh
```

## 📁 구현 파일

### 백엔드
- `src/main/java/com/example/payflow/youtube/domain/TrendingTopic.java` - 트렌드 도메인 모델
- `src/main/java/com/example/payflow/youtube/application/GoogleTrendsService.java` - 트렌드 서비스
- `src/main/java/com/example/payflow/youtube/infrastructure/GoogleTrendsClient.java` - 트렌드 데이터 조회
- `src/main/java/com/example/payflow/youtube/presentation/YouTubeController.java` - API 엔드포인트 추가

### 프론트엔드
- `src/main/resources/templates/youtube/trends.html` - 트렌드 전용 페이지
- `src/main/resources/static/js/youtube/youtube-trends.js` - 트렌드 UI 로직
- `src/main/resources/static/css/youtube/youtube-trends.css` - 트렌드 스타일
- `src/main/resources/templates/youtube/fragments/youtube-header.html` - 헤더 메뉴 추가
- `src/main/java/com/example/payflow/youtube/presentation/YouTubeWebController.java` - 페이지 라우팅

## 🎨 UI 특징

- **카드 레이아웃**: 각 트렌드를 카드 형태로 표시
- **순위 배지**: 좌측 상단에 순위 번호 표시
- **검색량 표시**: 트래픽 정보를 아이콘과 함께 표시
- **반응형 디자인**: 모바일/태블릿/데스크톱 모두 지원
- **호버 효과**: 카드에 마우스 올리면 살짝 떠오르는 효과

## ⚡ 성능 최적화

- **캐싱**: 10분간 트렌드 데이터 캐시
- **비동기 로딩**: 페이지 로딩 시 백그라운드에서 데이터 조회
- **에러 처리**: API 실패 시에도 샘플 데이터 제공

## 🔐 보안 설정

트렌드 API는 인증 없이 접근 가능합니다:
```java
.requestMatchers("/api/youtube/trends").permitAll()
```

## 📝 참고사항

### 실제 Google Trends API 사용 시

현재는 샘플 데이터를 사용하고 있습니다. 실제 환경에서는 다음 옵션을 고려하세요:

1. **SerpApi** (유료)
   - https://serpapi.com/google-trends-api
   - 안정적이고 공식 지원

2. **pytrends** (Python 라이브러리)
   - 비공식이지만 무료
   - 별도 Python 서비스 필요

3. **Google Trends RSS** (제한적)
   - 무료이지만 데이터 제한적
   - 가끔 URL 변경됨

### 데이터 업데이트

샘플 데이터를 실제 데이터로 교체하려면:
```java
// GoogleTrendsClient.java의 createSampleTrends() 메서드를
// 실제 API 호출 로직으로 교체
```

## 🚀 향후 개선 사항

- [ ] 실시간 Google Trends API 연동
- [ ] 트렌드 카테고리 필터 (뉴스, 엔터, 스포츠 등)
- [ ] 지역별 트렌드 선택 (서울, 부산 등)
- [ ] 트렌드 히스토리 차트
- [ ] 즐겨찾기 트렌드 키워드

## 🐛 문제 해결

### 트렌드가 표시되지 않을 때
1. 서버 로그 확인: `tail -f boot-run.log`
2. API 테스트: `curl http://localhost:8080/api/youtube/trends`
3. 캐시 초기화: 서버 재시작

### 스타일이 깨질 때
1. 브라우저 캐시 삭제 (Ctrl+Shift+R)
2. CSS 파일 확인: `youtube-trends.css`

## 📞 지원

문제가 발생하면 다음을 확인하세요:
- 서버 로그: `boot-run.log`
- 브라우저 콘솔 (F12)
- API 응답: `/api/youtube/trends`
