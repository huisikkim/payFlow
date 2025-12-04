# YouTube 인기 페이지 로그인 기능 가이드

## 개요
YouTube 인기 페이지에 JWT 기반 로그인 기능이 추가되었습니다. 기존 JWT 인증 구조를 활용하여 YouTube 페이지 디자인에 맞는 로그인 UI를 제공합니다.

## 주요 기능

### 1. 로그인 페이지
- **URL**: `/youtube/login`
- **디자인**: YouTube 페이지 스타일에 맞춘 다크 테마
- **기능**:
  - 아이디/비밀번호 입력
  - 빠른 로그인 버튼 (테스트용)
  - 로그인 후 원래 페이지로 리다이렉트

### 2. 헤더 로그인 상태 표시
- 로그인 전: "로그인" 버튼 표시
- 로그인 후: 사용자명과 "로그아웃" 버튼 표시
- localStorage에 JWT 토큰 저장

### 3. 인증이 필요한 기능
다음 기능들은 로그인이 필요하며, 미로그인 시 로그인 페이지로 리다이렉트됩니다:
- 즐겨찾기 탭 보기
- 폴더 생성
- 영상을 폴더에 추가
- 폴더 삭제
- 영상 삭제

## 테스트 계정

### 일반 사용자
- **아이디**: `user`
- **비밀번호**: `password`
- **권한**: ROLE_USER

### 관리자
- **아이디**: `admin`
- **비밀번호**: `admin`
- **권한**: ROLE_USER, ROLE_ADMIN

## API 엔드포인트

### 인증 API
- `POST /api/auth/login` - 로그인
  ```json
  {
    "username": "user",
    "password": "password"
  }
  ```
  응답:
  ```json
  {
    "success": true,
    "accessToken": "eyJhbGc...",
    "username": "user"
  }
  ```

### YouTube API 권한
- **공개 API** (인증 불필요):
  - `/api/youtube/popular/**` - 인기 영상 조회
  - `/api/youtube/statistics/**` - 영상 통계
  - `/api/youtube/search` - 영상 검색
  - `/api/youtube/revenue/**` - 수익 예측

- **인증 필요 API**:
  - `/api/youtube/folders/**` - 즐겨찾기 폴더 관리

## 사용 방법

### 1. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 2. YouTube 페이지 접속
```
http://localhost:8080/youtube/popular
```

### 3. 로그인
- 헤더의 "로그인" 버튼 클릭
- 또는 즐겨찾기 기능 사용 시 자동으로 로그인 페이지로 이동
- 테스트 계정으로 로그인 (user/password 또는 admin/admin)

### 4. 즐겨찾기 사용
- 로그인 후 "즐겨찾기" 탭 클릭
- 폴더 생성 및 영상 저장 가능

## 기술 스택

### Backend
- Spring Security
- JWT (JSON Web Token)
- JPA/Hibernate

### Frontend
- Vanilla JavaScript
- localStorage for token storage
- Fetch API with Authorization header

## 파일 구조

### Backend
```
src/main/java/com/example/payflow/
├── security/
│   ├── config/SecurityConfig.java (보안 설정)
│   ├── infrastructure/jwt/
│   │   ├── JwtTokenProvider.java (JWT 생성/검증)
│   │   └── JwtAuthenticationFilter.java (JWT 필터)
│   ├── presentation/AuthController.java (로그인 API)
│   └── infrastructure/DataInitializer.java (테스트 계정 생성)
└── youtube/
    └── presentation/YouTubeWebController.java (로그인 페이지 라우트)
```

### Frontend
```
src/main/resources/
├── templates/youtube/
│   ├── login.html (로그인 페이지)
│   └── fragments/youtube-header.html (헤더 with 로그인 상태)
├── static/
│   ├── css/youtube/youtube-header.css (헤더 스타일)
│   └── js/
│       ├── youtube/youtube-utils.js (로그인 체크 유틸)
│       └── youtube-favorites.js (즐겨찾기 with 인증)
```

## 보안 설정

### JWT 설정 (application.yml)
```yaml
jwt:
  secret: your-secret-key-here-make-it-long-and-secure
  expiration: 86400000  # 24시간
```

### CORS 설정
현재는 개발 환경으로 모든 origin 허용. 프로덕션에서는 제한 필요.

## 주의사항

1. **테스트 계정**: 현재 테스트 계정은 애플리케이션 시작 시 자동 생성됩니다.
2. **토큰 저장**: JWT 토큰은 localStorage에 저장되므로 XSS 공격에 주의가 필요합니다.
3. **프로덕션 배포**: 
   - JWT secret을 환경변수로 관리
   - HTTPS 사용 필수
   - CORS 설정 제한
   - 토큰 만료 시간 조정

## 향후 개선 사항

- [ ] 회원가입 기능
- [ ] 비밀번호 찾기
- [ ] 토큰 갱신 (Refresh Token)
- [ ] 소셜 로그인 (Google, GitHub 등)
- [ ] 사용자 프로필 관리
- [ ] HttpOnly 쿠키로 토큰 저장 (보안 강화)
