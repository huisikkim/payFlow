# 토스페이먼츠 설정 정보

## 🔑 클라이언트 키 (Client Key)

프론트엔드(Flutter)에서 사용하는 공개 키입니다.

```
test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq
```

**사용 위치:**
- Flutter 앱의 토스페이먼츠 위젯 초기화
- JavaScript에서 `TossPayments(clientKey)` 호출 시

**예시:**
```dart
const clientKey = 'test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq';
final tossPayments = TossPayments(clientKey);
```

---

## 🔐 시크릿 키 (Secret Key)

백엔드에서만 사용하는 비밀 키입니다. **절대 프론트엔드에 노출하면 안 됩니다!**

```
test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R
```

**사용 위치:**
- 백엔드 서버에서 결제 승인 API 호출 시
- 서버 간 통신에서만 사용

---

## 📍 백엔드 설정 위치

**파일:** `src/main/resources/application.properties`

```properties
# Toss Payments (테스트용 시크릿 키 - 실제 키로 교체 필요)
toss.payments.secret-key=${TOSS_SECRET_KEY:test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R}
toss.payments.client-key=${TOSS_CLIENT_KEY:test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq}
toss.payments.api-url=https://api.tosspayments.com/v1
```

---

## 🌐 API URL

```
https://api.tosspayments.com/v1
```

---

## 🧪 테스트 환경

현재 설정된 키는 **토스페이먼츠 테스트 환경**용입니다.

### 테스트 카드 정보

결제 테스트 시 아래 정보를 사용할 수 있습니다:

```
카드번호: 아무 16자리 숫자 (예: 1234-5678-9012-3456)
유효기간: 미래 날짜 (예: 12/25)
CVC: 아무 3자리 숫자 (예: 123)
비밀번호: 아무 2자리 숫자 (예: 12)
```

### 테스트 계좌이체

```
은행: 아무 은행 선택
계좌번호: 아무 숫자
예금주: 아무 이름
```

---

## 🚀 프로덕션 환경 전환

실제 서비스 배포 시 **반드시** 실제 키로 교체해야 합니다.

### 1. 토스페이먼츠 가입
1. https://www.tosspayments.com/ 접속
2. 회원가입 및 사업자 등록
3. 개발자센터에서 API 키 발급

### 2. 환경 변수 설정

**방법 1: 환경 변수 사용 (권장)**
```bash
export TOSS_CLIENT_KEY=live_ck_YOUR_REAL_CLIENT_KEY
export TOSS_SECRET_KEY=live_sk_YOUR_REAL_SECRET_KEY
```

**방법 2: application.properties 직접 수정**
```properties
toss.payments.client-key=live_ck_YOUR_REAL_CLIENT_KEY
toss.payments.secret-key=live_sk_YOUR_REAL_SECRET_KEY
```

### 3. Flutter 앱 업데이트
```dart
// 환경별 키 관리
const clientKey = kReleaseMode 
    ? 'live_ck_YOUR_REAL_CLIENT_KEY'  // 프로덕션
    : 'test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq';  // 테스트
```

---

## ⚠️ 보안 주의사항

### ❌ 절대 하지 말아야 할 것

1. **시크릿 키를 프론트엔드에 노출**
   - Flutter 코드에 시크릿 키 하드코딩 금지
   - Git에 시크릿 키 커밋 금지

2. **클라이언트에서 직접 결제 승인**
   - 결제 승인은 반드시 백엔드에서 수행
   - 금액 검증은 서버에서 수행

### ✅ 반드시 해야 할 것

1. **환경 변수 사용**
   - 민감한 키는 환경 변수로 관리
   - `.env` 파일은 `.gitignore`에 추가

2. **서버 검증**
   - 클라이언트에서 받은 금액을 서버에서 재검증
   - 주문 정보와 결제 금액 일치 확인

3. **HTTPS 사용**
   - 프로덕션 환경에서는 반드시 HTTPS 사용
   - API 통신 암호화

---

## 📚 참고 문서

- **토스페이먼츠 개발자 문서**: https://docs.tosspayments.com/
- **API 키 발급**: https://developers.tosspayments.com/
- **테스트 가이드**: https://docs.tosspayments.com/guides/test

---

## 💡 Flutter 팀을 위한 요약

### 프론트엔드에서 사용할 키

```dart
const tossClientKey = 'test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq';
```

### 사용 예시

```dart
import 'package:webview_flutter/webview_flutter.dart';

// 토스페이먼츠 초기화
const clientKey = 'test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq';

// HTML에서 사용
final html = '''
<script src="https://js.tosspayments.com/v1/payment"></script>
<script>
  const tossPayments = TossPayments('$clientKey');
  
  tossPayments.requestPayment('카드', {
    amount: 50000,
    orderId: 'ORDER-123',
    orderName: '상품명',
    customerName: '홍길동',
    successUrl: 'https://your-domain.com/success',
    failUrl: 'https://your-domain.com/fail',
  });
</script>
''';
```

### 주의사항

- ✅ 클라이언트 키는 공개되어도 안전합니다
- ❌ 시크릿 키는 절대 프론트엔드에 포함하지 마세요
- ✅ 결제 승인은 백엔드 API를 통해서만 수행하세요

---

## 🔄 키 관리 플로우

```
[Flutter 앱]
  ↓ 클라이언트 키 사용
[토스페이먼츠 위젯]
  ↓ 결제 진행
[토스페이먼츠 서버]
  ↓ paymentKey 반환
[Flutter 앱]
  ↓ paymentKey 전달
[백엔드 서버]
  ↓ 시크릿 키로 승인
[토스페이먼츠 API]
  ↓ 승인 완료
[백엔드 서버]
  ↓ 결과 반환
[Flutter 앱]
```

---

## 📞 문의

토스페이먼츠 관련 문의:
- 개발자 센터: https://developers.tosspayments.com/
- 고객센터: 1544-7772
