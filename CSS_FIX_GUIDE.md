# 🎨 CSS 적용 문제 해결 가이드

## 문제 상황
`/youtube/analysis` 페이지에서 CSS가 적용되지 않는 문제

## ✅ 해결 완료

### 1. 수정된 파일

**src/main/resources/templates/youtube/analysis.html**
```html
<!-- 변경 전 -->
<link rel="stylesheet" th:href="@{/css/youtube/youtube-common.css}">

<!-- 변경 후 -->
<link rel="stylesheet" th:href="@{/css/youtube/youtube-header.css}">
```

**src/main/resources/static/css/youtube/youtube-analysis.css**
```css
/* 추가된 기본 스타일 */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    background: #f9f9f9;
    color: #0f0f0f;
    line-height: 1.6;
}
```

### 2. CSS 파일 확인

```bash
# CSS 파일이 제대로 서빙되는지 확인
curl -I http://localhost:8080/css/youtube/youtube-header.css
curl -I http://localhost:8080/css/youtube/youtube-analysis.css
```

**예상 결과:**
```
HTTP/1.1 200 
```

### 3. 브라우저에서 확인

#### 방법 1: 직접 접속
```
http://localhost:8080/youtube/analysis
```

#### 방법 2: 개발자 도구로 확인
1. 브라우저에서 `F12` 또는 `Cmd+Option+I` (Mac)
2. **Network** 탭 선택
3. 페이지 새로고침 (`Cmd+Shift+R` 또는 `Ctrl+Shift+R`)
4. CSS 파일 확인:
   - `youtube-header.css` - Status: 200
   - `youtube-analysis.css` - Status: 200

#### 방법 3: 콘솔에서 확인
```javascript
// 브라우저 콘솔에서 실행
const header = document.querySelector('.youtube-header');
const styles = window.getComputedStyle(header);
console.log('배경색:', styles.backgroundColor);
// 예상 결과: rgb(17, 24, 39) - 어두운 회색
```

### 4. 캐시 문제 해결

CSS가 여전히 적용되지 않는다면:

#### 브라우저 캐시 클리어
- **Chrome/Edge**: `Cmd+Shift+Delete` (Mac) / `Ctrl+Shift+Delete` (Windows)
- **Firefox**: `Cmd+Shift+Delete` (Mac) / `Ctrl+Shift+Delete` (Windows)
- **Safari**: `Cmd+Option+E`

#### 강력 새로고침
- **Chrome/Firefox**: `Cmd+Shift+R` (Mac) / `Ctrl+Shift+R` (Windows)
- **Safari**: `Cmd+Option+R`

#### 시크릿/프라이빗 모드
- **Chrome**: `Cmd+Shift+N` (Mac) / `Ctrl+Shift+N` (Windows)
- **Firefox**: `Cmd+Shift+P` (Mac) / `Ctrl+Shift+P` (Windows)
- **Safari**: `Cmd+Shift+N`

### 5. 서버 재시작

```bash
# 기존 프로세스 종료
pkill -f 'java.*payflow'

# 서버 재시작
./gradlew bootRun
```

## 🎨 예상 결과

### 헤더 스타일
- 배경색: 어두운 회색 (#111827)
- 로고: 빨간색 YouTube 아이콘
- 네비게이션: 회색 버튼들
- 활성 메뉴: 흰색 텍스트 + 빨간색 하단 바

### 페이지 스타일
- 배경: 밝은 회색 (#f9f9f9)
- 제목: 큰 폰트 (2.5rem)
- 입력창: 둥근 모서리, 회색 테두리
- 버튼: 빨간색 그라데이션

## 🔍 문제 진단

### CSS가 로드되지 않는 경우

1. **404 에러**
   ```bash
   curl -I http://localhost:8080/css/youtube/youtube-header.css
   # HTTP/1.1 404 Not Found
   ```
   → 파일 경로 확인: `src/main/resources/static/css/youtube/`

2. **500 에러**
   ```bash
   # 서버 로그 확인
   tail -f boot-run.log
   ```
   → Thymeleaf 템플릿 오류 확인

3. **CORS 에러**
   - 브라우저 콘솔에서 확인
   → SecurityConfig 설정 확인

### CSS가 로드되지만 적용되지 않는 경우

1. **CSS 선택자 문제**
   ```javascript
   // 브라우저 콘솔에서 확인
   document.querySelector('.youtube-header'); // null이면 HTML 구조 문제
   ```

2. **CSS 우선순위 문제**
   ```javascript
   // 브라우저 콘솔에서 확인
   const header = document.querySelector('.youtube-header');
   console.log(window.getComputedStyle(header).backgroundColor);
   ```

3. **캐시 문제**
   - 시크릿 모드에서 테스트
   - 브라우저 캐시 클리어

## 📝 체크리스트

- [x] `youtube-common.css` → `youtube-header.css` 변경
- [x] `youtube-analysis.css`에 기본 스타일 추가
- [x] 서버 재시작
- [ ] 브라우저 캐시 클리어
- [ ] 강력 새로고침 (`Cmd+Shift+R`)
- [ ] 개발자 도구에서 CSS 로딩 확인
- [ ] 시크릿 모드에서 테스트

## 🚀 빠른 테스트

```bash
# 1. CSS 파일 확인
curl -s http://localhost:8080/css/youtube/youtube-header.css | head -10
curl -s http://localhost:8080/css/youtube/youtube-analysis.css | head -10

# 2. HTML 확인
curl -s http://localhost:8080/youtube/analysis | grep "stylesheet"

# 3. 테스트 HTML 열기
open test-css-loading.html  # Mac
start test-css-loading.html # Windows
```

## 💡 추가 팁

### 개발 중 CSS 변경 시
1. CSS 파일 수정
2. 브라우저에서 `Cmd+Shift+R` (강력 새로고침)
3. 변경사항 즉시 반영

### Spring Boot DevTools 사용
```gradle
// build.gradle에 추가
developmentOnly 'org.springframework.boot:spring-boot-devtools'
```
→ CSS 변경 시 자동 리로드

## 📞 여전히 문제가 있다면

1. 브라우저 개발자 도구 스크린샷
2. 콘솔 에러 메시지
3. Network 탭 스크린샷
4. 서버 로그

위 정보를 공유해주세요!
