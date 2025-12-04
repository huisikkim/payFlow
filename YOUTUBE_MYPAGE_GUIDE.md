# YouTube 마이페이지 가이드

## 개요
로그인한 사용자의 YouTube 검색 기록을 확인하고 관리할 수 있는 마이페이지 기능입니다.

## 주요 기능

### 1. 검색 기록 조회
- 사용자가 검색한 모든 키워드 목록 표시
- 검색 시간 (몇 분 전, 몇 시간 전 등)
- 검색 결과 개수
- 최신순으로 정렬

### 2. 검색 기록 관리
- **다시 검색**: 이전 검색어로 다시 검색 실행
- **개별 삭제**: 특정 검색 기록 삭제
- **전체 삭제**: 모든 검색 기록 일괄 삭제

### 3. 자동 검색 기록 저장
- 로그인한 사용자가 검색하면 자동으로 기록 저장
- 비로그인 사용자는 검색 기록이 저장되지 않음

## 접근 방법

### 1. 헤더에서 접근
```
로그인 후 → 헤더의 "마이페이지" 버튼 클릭
```

### 2. 직접 URL 접근
```
http://localhost:8080/youtube/mypage
```

### 3. 검색 기록에서 다시 검색
```
마이페이지 → 검색 기록 → "다시 검색" 버튼 클릭
→ 자동으로 검색 페이지로 이동하여 검색 실행
```

## API 엔드포인트

### 검색 기록 조회
```http
GET /api/youtube/search-history
Authorization: Bearer {JWT_TOKEN}
```

**응답 예시:**
```json
{
  "success": true,
  "count": 5,
  "history": [
    {
      "id": 1,
      "username": "user123",
      "searchQuery": "코딩 튜토리얼",
      "searchedAt": "2024-12-04T10:30:00",
      "resultCount": 25
    }
  ]
}
```

### 검색 기록 삭제
```http
DELETE /api/youtube/search-history/{id}
Authorization: Bearer {JWT_TOKEN}
```

### 전체 검색 기록 삭제
```http
DELETE /api/youtube/search-history
Authorization: Bearer {JWT_TOKEN}
```

## 데이터베이스 스키마

```sql
CREATE TABLE youtube_search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    search_query VARCHAR(500) NOT NULL,
    searched_at TIMESTAMP NOT NULL,
    result_count INT DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_searched_at (searched_at)
);
```

## 파일 구조

```
src/main/java/com/example/payflow/youtube/
├── domain/
│   └── SearchHistory.java                    # 검색 기록 도메인 모델
├── infrastructure/
│   └── SearchHistoryRepository.java          # 검색 기록 저장소
├── application/
│   └── SearchHistoryService.java             # 검색 기록 서비스
└── presentation/
    ├── SearchHistoryController.java          # 검색 기록 API 컨트롤러
    └── YouTubeWebController.java             # 마이페이지 웹 컨트롤러

src/main/resources/
├── templates/youtube/
│   └── mypage.html                           # 마이페이지 HTML
├── static/
│   ├── css/youtube/
│   │   └── youtube-mypage.css                # 마이페이지 스타일
│   └── js/youtube/
│       └── youtube-mypage.js                 # 마이페이지 스크립트
└── db/migration/
    └── V999__create_youtube_search_history.sql  # DB 마이그레이션

```

## 보안 설정

SecurityConfig.java에 다음 설정이 추가되었습니다:

```java
.requestMatchers("/api/youtube/search-history/**").authenticated()
```

- 검색 기록 API는 인증된 사용자만 접근 가능
- JWT 토큰이 필요함

## 사용 시나리오

### 시나리오 1: 검색 기록 확인
1. YouTube 인기 페이지에서 로그인
2. 헤더의 "마이페이지" 버튼 클릭
3. 검색 기록 목록 확인

### 시나리오 2: 이전 검색어로 다시 검색
1. 마이페이지에서 검색 기록 확인
2. 원하는 검색 기록의 "다시 검색" 버튼 클릭
3. 자동으로 검색 페이지로 이동하여 검색 실행

### 시나리오 3: 검색 기록 삭제
1. 마이페이지에서 검색 기록 확인
2. 개별 삭제: 휴지통 아이콘 클릭
3. 전체 삭제: "전체 삭제" 버튼 클릭

## 주의사항

1. **로그인 필수**: 마이페이지는 로그인한 사용자만 접근 가능
2. **자동 저장**: 검색 시 자동으로 기록이 저장됨
3. **개인정보**: 검색 기록은 사용자별로 분리되어 저장됨
4. **제한**: 최대 50개의 최근 검색 기록만 조회됨

## 향후 개선 사항

- [ ] 검색 기록 필터링 (날짜별, 키워드별)
- [ ] 검색 기록 통계 (가장 많이 검색한 키워드)
- [ ] 검색 기록 내보내기 (CSV, JSON)
- [ ] 즐겨찾기한 영상 목록
- [ ] 시청 기록 (추후 추가 예정)
