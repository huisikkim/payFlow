# 🎯 공동구매 시스템 가이드

## 📋 개요

유통업자가 공동구매 방을 생성하고, 가게들이 참여하여 대량 구매를 통해 할인 혜택을 받는 시스템입니다.

## 🔥 핵심 기능

### 1. 유통업자 기능
- ✅ 공동구매 방 생성 (상품, 할인율, 재고, 마감시간 설정)
- ✅ 방 오픈/마감/취소
- ✅ 실시간 참여 현황 모니터링
- ✅ 자동 주문 생성

### 2. 가게 기능
- ✅ 오픈 중인 공동구매 방 조회
- ✅ 공동구매 참여 (수량 선택)
- ✅ 실시간 진행률 확인
- ✅ 예상 절감액 확인
- ✅ 참여 취소 (마감 전)

### 3. 자동화 기능
- ✅ 마감 시간 도래 시 자동 마감
- ✅ 재고 소진 시 자동 마감
- ✅ 목표 달성 시 자동 주문 생성
- ✅ 배송비 자동 분담 계산

## 🏗️ 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                      공동구매 시스템                          │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  [유통업자]                                                   │
│     │                                                         │
│     ├─ 1. 공동구매 방 생성                                    │
│     │   - 상품 선택                                           │
│     │   - 할인율 설정 (예: 20%)                               │
│     │   - 재고 설정 (예: 500kg)                               │
│     │   - 목표 수량 (예: 300kg)                               │
│     │   - 마감 시간 (예: 24시간)                              │
│     │   - 지역 설정 (예: 서울 강남구)                         │
│     │                                                         │
│     ├─ 2. 방 오픈                                            │
│     │   → 가게들에게 알림 발송                                │
│     │                                                         │
│     └─ 3. 실시간 모니터링                                     │
│         - 현재 참여자 수                                      │
│         - 현재 주문 수량                                      │
│         - 목표 달성률                                         │
│                                                               │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  [가게 사장님]                                                │
│     │                                                         │
│     ├─ 1. 공동구매 방 조회                                    │
│     │   - 지역별 필터                                         │
│     │   - 카테고리별 필터                                     │
│     │   - 마감 임박 방                                        │
│     │   - 추천 방                                             │
│     │                                                         │
│     ├─ 2. 방 상세 확인                                        │
│     │   - 원가: 10,000원/kg                                  │
│     │   - 할인가: 8,000원/kg (20% 할인)                      │
│     │   - 절감액: 2,000원/kg                                 │
│     │   - 진행률: 75% (225kg / 300kg)                        │
│     │   - 참여자: 8명                                         │
│     │   - 남은 시간: 18시간 23분                              │
│     │                                                         │
│     ├─ 3. 참여하기                                            │
│     │   - 수량 입력 (예: 30kg)                                │
│     │   - 예상 절감액 확인 (60,000원)                         │
│     │   - 배송 정보 입력                                      │
│     │   → 참여 완료                                           │
│     │                                                         │
│     └─ 4. 참여 취소 (마감 전)                                 │
│                                                               │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  [자동화 시스템]                                              │
│     │                                                         │
│     ├─ 스케줄러 (매 5분)                                      │
│     │   └─ 마감 시간 지난 방 자동 마감                        │
│     │                                                         │
│     ├─ 스케줄러 (매 10분)                                     │
│     │   └─ 성공 마감된 방 주문 자동 생성                      │
│     │                                                         │
│     └─ 실시간 이벤트                                          │
│         ├─ 재고 소진 시 자동 마감                             │
│         ├─ 참여자 추가 시 배송비 재계산                       │
│         └─ 목표 달성 시 상태 변경                             │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## 📊 데이터 모델

### GroupBuyingRoom (공동구매 방)
```java
- roomId: 방 고유 ID
- roomTitle: 방 제목
- distributorId: 유통업자 ID
- productId: 상품 ID
- originalPrice: 원가
- discountRate: 할인율 (%)
- discountedPrice: 할인가
- availableStock: 준비한 재고
- targetQuantity: 목표 수량
- currentQuantity: 현재 신청 수량
- minParticipants: 최소 참여자 수
- currentParticipants: 현재 참여자 수
- region: 대상 지역
- deliveryFee: 배송비
- deliveryFeeType: 배송비 타입 (FREE/FIXED/SHARED)
- deadline: 마감 시간
- status: 방 상태
```

### GroupBuyingParticipant (참여자)
```java
- roomId: 방 ID
- storeId: 가게 ID
- quantity: 주문 수량
- unitPrice: 단가 (할인가)
- totalAmount: 총액
- savingsAmount: 절감액
- deliveryAddress: 배송 주소
- status: 참여 상태
```

## 🔄 프로세스 흐름

### 1. 방 생성 → 오픈
```
유통업자가 방 생성
  ↓
상태: WAITING
  ↓
유통업자가 방 오픈
  ↓
상태: OPEN
  ↓
가게들에게 알림 발송
```

### 2. 가게 참여
```
가게가 방 조회
  ↓
상세 정보 확인 (할인율, 진행률 등)
  ↓
수량 입력 및 참여
  ↓
참여자 추가 → currentQuantity 증가
  ↓
배송비 재계산 (SHARED인 경우)
  ↓
재고 소진 체크 → 소진 시 자동 마감
```

### 3. 방 마감
```
[자동 마감 조건]
- 마감 시간 도래
- 재고 완전 소진

마감 시 목표 달성 여부 확인
  ↓
목표 달성 → CLOSED_SUCCESS
목표 미달 → CLOSED_FAILED
```

### 4. 주문 생성 (목표 달성 시)
```
CLOSED_SUCCESS 상태의 방
  ↓
모든 참여자 확정 (CONFIRMED)
  ↓
각 참여자별로 DistributorOrder 생성
  ↓
주문 번호: GB-20231129143022-1234
  ↓
상태: ORDER_CREATED
```

## 🚀 API 사용 예시

### 1. 공동구매 방 생성 (유통업자)

```bash
POST /api/group-buying/rooms
Content-Type: application/json

{
  "roomTitle": "🔥 김치 대박 세일! 20% 할인",
  "distributorId": "DIST001",
  "distributorName": "신선식품 유통",
  "productId": 123,
  "discountRate": 20.00,
  "availableStock": 500,
  "targetQuantity": 300,
  "minOrderPerStore": 10,
  "maxOrderPerStore": 100,
  "minParticipants": 5,
  "maxParticipants": 20,
  "region": "서울 강남구,서초구",
  "deliveryFee": 50000,
  "deliveryFeeType": "SHARED",
  "expectedDeliveryDate": "2023-12-01T09:00:00",
  "durationHours": 24,
  "description": "신선한 김치를 특가로 제공합니다!",
  "specialNote": "당일 배송 보장",
  "featured": true
}
```

### 2. 방 오픈 (유통업자)

```bash
POST /api/group-buying/rooms/GBR-20231129143022-1234/open?distributorId=DIST001
```

### 3. 오픈 중인 방 목록 조회 (가게)

```bash
# 전체 조회
GET /api/group-buying/rooms/open

# 지역별 조회
GET /api/group-buying/rooms/open?region=강남구

# 카테고리별 조회
GET /api/group-buying/rooms/open?category=채소
```

### 4. 방 상세 조회

```bash
GET /api/group-buying/rooms/GBR-20231129143022-1234
```

**응답 예시:**
```json
{
  "roomId": "GBR-20231129143022-1234",
  "roomTitle": "🔥 김치 대박 세일! 20% 할인",
  "productName": "포기김치",
  "originalPrice": 10000,
  "discountRate": 20.00,
  "discountedPrice": 8000,
  "savingsPerUnit": 2000,
  "targetQuantity": 300,
  "currentQuantity": 225,
  "achievementRate": 75.00,
  "currentParticipants": 8,
  "deliveryFeePerStore": 6250,
  "remainingMinutes": 1103,
  "status": "OPEN"
}
```

### 5. 공동구매 참여 (가게)

```bash
POST /api/group-buying/participants/join
Content-Type: application/json

{
  "roomId": "GBR-20231129143022-1234",
  "storeId": "STORE001",
  "quantity": 30,
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "deliveryPhone": "010-1234-5678",
  "deliveryRequest": "문 앞에 놓아주세요"
}
```

**응답 예시:**
```json
{
  "id": 1,
  "roomId": 1,
  "storeId": "STORE001",
  "storeName": "맛있는 식당",
  "quantity": 30,
  "unitPrice": 8000,
  "totalProductAmount": 240000,
  "deliveryFee": 6250,
  "totalAmount": 246250,
  "savingsAmount": 60000,
  "status": "JOINED"
}
```

### 6. 참여 취소 (가게)

```bash
POST /api/group-buying/participants/1/cancel?storeId=STORE001&reason=주문 실수
```

### 7. 가게의 참여 내역 조회

```bash
GET /api/group-buying/participants/store/STORE001
```

### 8. 방 수동 마감 (유통업자)

```bash
POST /api/group-buying/rooms/GBR-20231129143022-1234/close?distributorId=DIST001
```

### 9. 추천 방 목록 조회

```bash
GET /api/group-buying/rooms/featured
```

### 10. 마감 임박 방 조회

```bash
GET /api/group-buying/rooms/deadline-soon
```

## 💰 배송비 계산 방식

### 1. FREE (무료 배송)
```
deliveryFeePerStore = 0
```

### 2. FIXED (고정 배송비)
```
deliveryFeePerStore = deliveryFee (가게당 고정)
```

### 3. SHARED (분담 배송비)
```
deliveryFeePerStore = deliveryFee / currentParticipants

예시:
총 배송비: 50,000원
참여자 수: 8명
→ 가게당 배송비: 6,250원

참여자가 추가되면 자동으로 재계산:
참여자 수: 10명
→ 가게당 배송비: 5,000원
```

## 📈 실시간 진행률 표시

```
진행률 = (currentQuantity / targetQuantity) * 100

예시:
목표: 300kg
현재: 225kg
→ 진행률: 75%

UI 표시:
████████░░ 75% (225kg / 300kg)
```

## ⏰ 자동화 스케줄러

### 1. 만료된 방 자동 마감
```java
@Scheduled(cron = "0 */5 * * * *") // 매 5분마다
public void closeExpiredRooms()
```

- 마감 시간이 지난 OPEN 상태의 방 조회
- 목표 달성 여부 확인
- 상태 변경: CLOSED_SUCCESS 또는 CLOSED_FAILED

### 2. 주문 자동 생성
```java
@Scheduled(cron = "0 */10 * * * *") // 매 10분마다
public void createOrdersForSuccessfulRooms()
```

- CLOSED_SUCCESS 상태의 방 조회
- 각 참여자별로 DistributorOrder 생성
- 참여자 상태 변경: ORDER_CREATED

## 🎨 UI/UX 권장 사항

### 가게 사장님 화면

```
┌─────────────────────────────────────────┐
│  🔥 진행 중인 공동구매                   │
├─────────────────────────────────────────┤
│                                          │
│  🎯 김치 대박 세일! 20% 할인             │
│                                          │
│  원가: 10,000원/kg                       │
│  할인가: 8,000원/kg ⭐                   │
│  절감: 2,000원/kg                        │
│                                          │
│  진행률: ████████░░ 75%                  │
│  225kg / 300kg                           │
│                                          │
│  참여자: 8명 / 최소 5명 ✅               │
│  남은 시간: ⏰ 18시간 23분               │
│                                          │
│  배송비: 6,250원 (8명 분담)              │
│  예상 배송일: 12월 1일                   │
│                                          │
│  [지금 참여하기] 버튼                    │
│                                          │
└─────────────────────────────────────────┘
```

### 유통업자 대시보드

```
┌─────────────────────────────────────────┐
│  내 공동구매 방 관리                     │
├─────────────────────────────────────────┤
│                                          │
│  방 ID: GBR-20231129143022-1234          │
│  상품: 포기김치                          │
│  상태: 🟢 진행 중                        │
│                                          │
│  목표 달성률: 75% ✅                     │
│  참여자: 8명 / 최소 5명 ✅               │
│  재고 잔여: 55% (275kg / 500kg)          │
│                                          │
│  예상 매출: 1,800,000원                  │
│  남은 시간: 18시간 23분                  │
│                                          │
│  [수동 마감] [취소] 버튼                 │
│                                          │
└─────────────────────────────────────────┘
```

## 🔔 알림 시나리오

### 1. 방 오픈 시
```
📢 [신선식품 유통] 김치 공동구매 오픈!
   20% 할인, 24시간 한정
   → 지금 확인하기
```

### 2. 목표 달성 임박
```
🎯 목표 80% 달성!
   지금 참여하면 20% 할인 확정
   → 참여하기
```

### 3. 마감 임박
```
⏰ 3시간 후 마감!
   김치 공동구매 마지막 기회
   → 서둘러 참여하기
```

### 4. 목표 달성
```
🎉 목표 달성! 20% 할인 확정
   12월 1일 배송 예정
```

### 5. 주문 생성 완료
```
✅ 주문이 생성되었습니다
   주문번호: GB-20231129143022-1234
   예상 배송일: 12월 1일
```

## 🎯 비즈니스 시나리오

### 시나리오 1: 재고 소진 전략
```
유통업자: "김치 500kg 재고, 유통기한 1주일"
→ 공동구매 방 생성: 20% 할인, 24시간
→ 8개 가게 참여, 300kg 판매
→ 자동 마감 및 주문 생성
→ 재고 처리 성공 + 가게들 만족
```

### 시나리오 2: 대량 입고 예약
```
유통업자: "다음주 쌀 10톤 입고 예정"
→ 공동구매 방 생성: 15% 할인, 선주문, 72시간
→ 20개 가게 참여, 5톤 예약
→ 정확한 수요 파악
→ 재고 리스크 감소
```

### 시나리오 3: 지역별 배송 최적화
```
유통업자: "강남구 배송 예정"
→ 공동구매 방: 강남구 한정, 배송비 무료
→ 강남구 가게들만 참여
→ 한 번에 배송
→ 배송비 절감
```

## 📊 성과 지표

### 유통업자
- 재고 회전율 향상
- 대량 판매를 통한 매출 증대
- 배송 효율성 향상
- 고객(가게) 만족도 향상

### 가게
- 원가 절감 (할인 혜택)
- 배송비 분담으로 비용 절감
- 신선한 상품 확보
- 구매 편의성 향상

## 🚀 향후 확장 가능성

### Phase 2: AI 자동화
- 과거 주문 패턴 분석
- 최적 할인율 자동 제안
- 수요 예측 기반 방 자동 생성
- 가게별 맞춤 추천

### Phase 3: 소셜 기능
- 친구 가게 초대 시 추가 할인
- 공동구매 리뷰 시스템
- 가게 간 커뮤니티

### Phase 4: 게이미피케이션
- 얼리버드 보너스
- 단계별 할인율 업그레이드
- 참여 포인트 적립

## 🎓 개발자 노트

이 시스템의 핵심 차별화 포인트:

1. **유통업자 주도**: 재고 전략을 직접 컨트롤
2. **실시간 투명성**: 진행률, 참여자 수 실시간 공개
3. **자동화**: 마감, 주문 생성 모두 자동
4. **배송비 최적화**: 참여자 증가 시 배송비 자동 감소
5. **확장 가능**: AI, 소셜, 게이미피케이션 추가 가능

이 시스템은 **실제 비즈니스 문제(재고 처리, 원가 절감)를 기술로 해결**한 사례로,
면접에서 강력한 어필 포인트가 될 것입니다!
