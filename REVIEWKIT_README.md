# 🌟 ReviewKit - 고객 리뷰 수집 & 표시 SaaS

고객 리뷰를 쉽게 수집하고, 웹사이트에 자동으로 예쁘게 표시해주는 SaaS 플랫폼

## 📋 목차

- [핵심 가치](#핵심-가치)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [시작하기](#시작하기)
- [사용 가이드](#사용-가이드)
- [API 문서](#api-문서)
- [위젯 SDK](#위젯-sdk)

## 🚀 핵심 가치

- **🚀 빠름**: 코드 1줄로 5분 안에 설치
- **💰 저렴**: 월 $19 (경쟁사 $299 대비 94% 저렴)
- **🎨 예쁨**: 자동으로 반응형 디자인 적용
- **🤖 자동**: 리뷰 요청부터 표시까지 자동화

## ✨ 주요 기능

### 1. 리뷰 수집
- ✅ QR 코드 생성 (테이블/카운터 부착용)
- ✅ 공개 리뷰 작성 페이지
- ✅ 이메일 자동 발송 (예정)
- ✅ 별점 (1-5점) + 텍스트 리뷰
- ✅ 사진/비디오 첨부 (예정)

### 2. 리뷰 관리
- ✅ 대시보드에서 승인/거부
- ✅ 실시간 알림 (예정)
- ✅ 리뷰 통계 및 분석 (예정)
- ✅ 스팸 필터링 (예정)

### 3. 위젯 표시
- ✅ Shadow DOM 스타일 격리
- ✅ 라이트/다크 테마
- ✅ 그리드/리스트 레이아웃
- ✅ 반응형 디자인
- ✅ 다국어 지원 (한국어/영어)
- ✅ 커스터마이징 옵션

## 🛠 기술 스택

### Backend
- **Spring Boot 3.5.7** - 웹 프레임워크
- **Spring Data JPA** - ORM
- **H2 Database** - 개발용 DB
- **Spring Security + JWT** - 인증/인가
- **Thymeleaf** - 서버 사이드 템플릿

### Frontend
- **Vanilla JavaScript** - 위젯 SDK
- **Shadow DOM** - 스타일 격리
- **QRCode.js** - QR 코드 생성

### Architecture
- **DDD (Domain-Driven Design)** - 도메인 중심 설계
- **Layered Architecture** - 계층형 아키텍처
  - Domain Layer (엔티티, 값 객체)
  - Application Layer (서비스 로직)
  - Infrastructure Layer (리포지토리)
  - Presentation Layer (컨트롤러)

## 📁 프로젝트 구조

```
src/main/java/com/example/payflow/reviewkit/
├── domain/                      # 도메인 계층
│   ├── Business.java           # 비즈니스 엔티티
│   ├── Review.java             # 리뷰 엔티티
│   ├── Widget.java             # 위젯 엔티티
│   └── ReviewStatus.java       # 리뷰 상태 (PENDING/APPROVED/REJECTED)
│
├── infrastructure/              # 인프라 계층
│   ├── BusinessRepository.java
│   ├── ReviewRepository.java
│   └── WidgetRepository.java
│
├── application/                 # 애플리케이션 계층
│   ├── BusinessService.java
│   ├── ReviewService.java
│   ├── WidgetService.java
│   └── dto/                    # DTO 객체
│       ├── BusinessCreateRequest.java
│       ├── ReviewSubmitRequest.java
│       ├── ReviewResponse.java
│       └── WidgetCreateRequest.java
│
└── presentation/                # 프레젠테이션 계층
    ├── ReviewKitController.java      # 대시보드 UI
    ├── ReviewApiController.java      # 리뷰 관리 API
    └── WidgetApiController.java      # 위젯 공개 API

src/main/resources/
├── templates/reviewkit/         # Thymeleaf 템플릿
│   ├── landing.html            # 랜딩 페이지
│   ├── dashboard.html          # 대시보드
│   ├── business-form.html      # 비즈니스 생성
│   ├── business-detail.html    # 비즈니스 상세
│   ├── review-form.html        # 리뷰 작성 (공개)
│   ├── widget-form.html        # 위젯 생성
│   ├── widget-demo.html        # 위젯 데모
│   └── qr-code.html           # QR 코드 생성
│
└── static/js/
    └── reviewkit-widget.js     # 위젯 SDK
```

## 🚀 시작하기

### 1. 애플리케이션 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 JAR 빌드 후 실행
./gradlew bootJar
java -jar build/libs/payflow.jar
```

### 2. 테스트 데이터 생성

```bash
# 테스트 스크립트 실행
./test-reviewkit-complete.sh
```

이 스크립트는 다음을 자동으로 수행합니다:
- 비즈니스 생성 (홍대 지민 카페)
- 6개의 샘플 리뷰 제출
- 모든 리뷰 자동 승인

### 3. 접속 URL

- **랜딩 페이지**: http://localhost:8080/reviewkit
- **대시보드**: http://localhost:8080/reviewkit/dashboard
- **비즈니스 상세**: http://localhost:8080/reviewkit/businesses/1
- **리뷰 작성 (공개)**: http://localhost:8080/reviewkit/r/hongdae-jimin-cafe
- **위젯 데모**: http://localhost:8080/reviewkit/widget-demo
- **QR 코드**: http://localhost:8080/reviewkit/businesses/1/qr-code

## 📖 사용 가이드

### 비즈니스 생성

1. 대시보드 접속
2. "새 비즈니스 만들기" 클릭
3. 비즈니스 정보 입력
   - 이름 (예: 홍대 지민 카페)
   - 설명
   - 웹사이트 URL
4. 자동으로 slug 생성 (예: `hongdae-jimin-cafe`)

### 리뷰 수집

#### 방법 1: QR 코드
1. 비즈니스 상세 페이지에서 "QR 코드 생성" 클릭
2. QR 코드 다운로드 또는 인쇄
3. 테이블, 카운터, 포스터에 부착
4. 고객이 스캔하면 리뷰 작성 페이지로 이동

#### 방법 2: 직접 링크
- 리뷰 작성 URL: `http://yoursite.com/reviewkit/r/{slug}`
- 이메일, SNS, 웹사이트에 공유

### 리뷰 관리

1. 비즈니스 상세 페이지 접속
2. "대기중 리뷰" 탭에서 새 리뷰 확인
3. 각 리뷰에 대해:
   - ✓ 승인: 공개적으로 표시
   - ✗ 거부: 비공개 처리

### 위젯 설치

1. 비즈니스 상세 페이지에서 "위젯 관리" 탭
2. "새 위젯 만들기" 클릭
3. 위젯 설정:
   - 이름 (내부 관리용)
   - 테마 (light/dark)
   - 레이아웃 (grid/list)
   - 표시 개수 (1-20)
   - 언어 (ko/en)
4. 생성된 임베드 코드 복사
5. 웹사이트 HTML에 붙여넣기

## 🔌 API 문서

### 공개 API (인증 불필요)

#### 리뷰 제출
```http
POST /api/reviewkit/businesses/{slug}/reviews
Content-Type: application/json

{
  "reviewerName": "김철수",
  "reviewerEmail": "chulsoo@gmail.com",
  "reviewerCompany": "ABC 스타트업",  // 선택
  "rating": 5,
  "content": "정말 좋았어요!"
}
```

#### 위젯 리뷰 조회
```http
GET /api/widgets/{widgetId}/reviews?limit=6
```

### 대시보드 API (인증 필요)

#### 리뷰 목록 조회
```http
GET /api/reviewkit/businesses/{businessId}/reviews
```

#### 리뷰 승인
```http
POST /api/reviewkit/reviews/{reviewId}/approve
```

#### 리뷰 거부
```http
POST /api/reviewkit/reviews/{reviewId}/reject
```

## 🎨 위젯 SDK

### 기본 사용법

```html
<!-- 1. SDK 스크립트 추가 -->
<script src="https://yoursite.com/js/reviewkit-widget.js"></script>

<!-- 2. 위젯 컨테이너 추가 -->
<div data-reviewkit-widget="your-widget-id"></div>
```

### 옵션 설정

```html
<!-- 데이터 속성으로 설정 -->
<div 
  data-reviewkit-widget="your-widget-id"
  data-theme="dark"
  data-layout="list"
  data-limit="10"
  data-lang="en"
></div>
```

### JavaScript API

```javascript
// 수동 초기화
const element = document.getElementById('my-reviews');
ReviewKit.init(element, 'your-widget-id', {
  theme: 'dark',
  layout: 'grid',
  limit: 6,
  lang: 'ko'
});
```

### 위젯 옵션

| 옵션 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `theme` | string | `'light'` | 테마 (`light` 또는 `dark`) |
| `layout` | string | `'grid'` | 레이아웃 (`grid` 또는 `list`) |
| `limit` | number | `6` | 표시할 리뷰 개수 (1-20) |
| `lang` | string | `'ko'` | 언어 (`ko` 또는 `en`) |

### 위젯 특징

- ✅ **Shadow DOM**: 고객 사이트 CSS와 충돌 없음
- ✅ **반응형**: 모바일/태블릿/데스크톱 자동 대응
- ✅ **XSS 방지**: HTML 이스케이프 처리
- ✅ **에러 핸들링**: 네트워크 오류 시 graceful degradation
- ✅ **성능 최적화**: 최소한의 HTTP 요청

## 🎯 사용 시나리오

### Persona 1: 카페 주인 "지민"

**문제**: 손님들이 "맛있어요!" 말은 하는데 온라인 리뷰는 안 써줌

**해결**:
1. ReviewKit에서 "홍대 지민 카페" 비즈니스 생성
2. QR 코드 생성 → 테이블에 부착
3. 손님이 QR 스캔 → 30초 만에 리뷰 작성
4. 대시보드에서 승인 → 웹사이트에 자동 표시

**결과**: 1주일 후 리뷰 23개 수집, 신규 고객 전환율 37% 증가

### Persona 2: 프리랜서 디자이너 "수진"

**문제**: 클라이언트들이 "정말 좋았어요!" 말만 하고 추천서 안 써줌

**해결**:
1. ReviewKit에서 "수진 디자인 스튜디오" 생성
2. 프로젝트 완료 시 리뷰 요청 링크 발송
3. 클라이언트가 2분 만에 추천사 작성
4. 포트폴리오 사이트에 위젯 설치 → 자동 표시

**결과**: 3개월 후 추천사 12개, 신규 프로젝트 문의 2배 증가

## 🔮 향후 계획

### Phase 4 (예정)
- [ ] 이메일 자동 발송 (리뷰 요청)
- [ ] 사진/비디오 첨부 기능
- [ ] 리뷰 통계 대시보드
- [ ] 스팸 필터링 (AI)
- [ ] 다국어 확장 (일본어, 중국어)

### Phase 5 (예정)
- [ ] 소셜 미디어 연동 (Instagram, Facebook)
- [ ] 리뷰 위젯 A/B 테스팅
- [ ] 리뷰 인센티브 시스템 (쿠폰, 할인)
- [ ] 모바일 앱 (iOS/Android)

## 📝 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

## 🤝 기여

버그 리포트, 기능 제안, Pull Request 환영합니다!

## 📧 문의

- 이메일: support@reviewkit.com
- 웹사이트: https://reviewkit.com

---

**Made with ❤️ by ReviewKit Team**
