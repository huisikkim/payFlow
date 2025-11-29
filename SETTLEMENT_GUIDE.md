# 정산 시스템 가이드

## 개요

가게사장님과 유통업자 간의 거래에 대한 정산 기능을 제공합니다.

### 주요 기능

1. **자동 정산 생성**: 주문 결제 완료 시 자동으로 정산 생성
2. **일일 정산 집계**: 실시간 일일 정산 집계 및 통계
3. **정산 대시보드**: 가게/유통업자별 정산 현황 조회
4. **정산 리포트**: 기간별 매출/수수료/정산금액 통계

---

## 아키텍처

### 이벤트 기반 정산 흐름

```
[주문 결제 완료]
    ↓
[Kafka: CatalogOrderPaymentCompleted 이벤트 발행]
    ↓
[SettlementEventListener: 이벤트 수신]
    ↓
[정산 생성 (IngredientSettlement)]
    ↓
[일일 정산 집계 (DailySettlement)]
```

### 정산 타입

- **CATALOG**: 카탈로그 상품 주문 정산
- **INGREDIENT**: 식자재 주문 정산

---

## API 엔드포인트

### 1. 개별 정산 조회

#### 가게별 정산 목록
```bash
GET /api/settlements/store/{storeId}
```

**응답 예시:**
```json
[
  {
    "settlementId": "SETTLE_abc123",
    "storeId": "store1",
    "distributorId": "dist1",
    "orderId": "ORD-20241129-143022-123",
    "settlementAmount": 150000,
    "outstandingAmount": 150000,
    "paidAmount": 0,
    "status": "PENDING",
    "settlementDate": "2024-11-29T14:30:22"
  }
]
```

#### 유통업자별 정산 목록
```bash
GET /api/settlements/distributor/{distributorId}
```

#### 정산 상세 조회
```bash
GET /api/settlements/{settlementId}
```

#### 정산 완료 처리
```bash
POST /api/settlements/{settlementId}/complete
Content-Type: application/json

{
  "paidAmount": 150000
}
```

#### 총 미수금 조회
```bash
GET /api/settlements/store/{storeId}/outstanding
```

**응답 예시:**
```json
{
  "totalOutstanding": 450000
}
```

---

### 2. 일일 정산 조회

#### 가게별 일일 정산
```bash
GET /api/daily-settlements/store/{storeId}?startDate=2024-11-01&endDate=2024-11-30
```

**응답 예시:**
```json
[
  {
    "id": 1,
    "settlementDate": "2024-11-29",
    "storeId": "store1",
    "distributorId": "dist1",
    "orderCount": 5,
    "totalSalesAmount": 750000,
    "totalSettlementAmount": 750000,
    "totalPaidAmount": 300000,
    "totalOutstandingAmount": 450000,
    "catalogOrderCount": 3,
    "catalogSalesAmount": 450000,
    "ingredientOrderCount": 2,
    "ingredientSalesAmount": 300000,
    "paymentRate": 40.0,
    "createdAt": "2024-11-29T14:30:22",
    "updatedAt": "2024-11-29T18:45:10"
  }
]
```

#### 유통업자별 일일 정산
```bash
GET /api/daily-settlements/distributor/{distributorId}?startDate=2024-11-01&endDate=2024-11-30
```

---

### 3. 정산 통계

#### 가게별 정산 통계
```bash
GET /api/daily-settlements/store/{storeId}/statistics?startDate=2024-11-01&endDate=2024-11-30
```

**응답 예시:**
```json
{
  "type": "STORE",
  "id": "store1",
  "totalOrderCount": 45,
  "totalSalesAmount": 6750000,
  "totalPaidAmount": 4500000,
  "totalOutstandingAmount": 2250000,
  "catalogOrderCount": 30,
  "catalogSalesAmount": 4500000,
  "ingredientOrderCount": 15,
  "ingredientSalesAmount": 2250000,
  "paymentRate": 66.67
}
```

#### 유통업자별 정산 통계
```bash
GET /api/daily-settlements/distributor/{distributorId}/statistics?startDate=2024-11-01&endDate=2024-11-30
```

---

### 4. 관리자 기능

#### 특정 날짜 정산 재집계
```bash
POST /api/daily-settlements/recalculate?targetDate=2024-11-29
```

---

## 자동 스케줄러

### 일일 정산 재집계
- **실행 시간**: 매일 새벽 1시
- **대상**: 전날 정산 데이터
- **목적**: 실시간 집계에서 누락된 데이터 보정

```java
@Scheduled(cron = "0 0 1 * * *")
public void recalculateYesterdaySettlement()
```

### 주간 정산 재집계
- **실행 시간**: 매주 월요일 새벽 2시
- **대상**: 지난주 월요일 ~ 일요일
- **목적**: 주간 데이터 정합성 확인

```java
@Scheduled(cron = "0 0 2 * * MON")
public void recalculateLastWeekSettlement()
```

---

## 데이터베이스 스키마

### ingredient_settlements (개별 정산)
```sql
CREATE TABLE ingredient_settlements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    settlement_id VARCHAR(255) UNIQUE NOT NULL,
    store_id VARCHAR(255) NOT NULL,
    distributor_id VARCHAR(255) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    order_type VARCHAR(50) NOT NULL,  -- 'CATALOG' or 'INGREDIENT'
    settlement_amount BIGINT NOT NULL,
    outstanding_amount BIGINT NOT NULL,
    paid_amount BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    settlement_date TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### daily_settlements (일일 정산)
```sql
CREATE TABLE daily_settlements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    settlement_date DATE NOT NULL,
    store_id VARCHAR(255) NOT NULL,
    distributor_id VARCHAR(255) NOT NULL,
    order_count INT NOT NULL,
    total_sales_amount BIGINT NOT NULL,
    total_settlement_amount BIGINT NOT NULL,
    total_paid_amount BIGINT NOT NULL,
    total_outstanding_amount BIGINT NOT NULL,
    catalog_order_count INT NOT NULL,
    catalog_sales_amount BIGINT NOT NULL,
    ingredient_order_count INT NOT NULL,
    ingredient_sales_amount BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE KEY uk_daily_settlement (settlement_date, store_id, distributor_id)
);
```

---

## Kafka 이벤트

### CatalogOrderPaymentCompleted
카탈로그 주문 결제 완료 시 발행

```json
{
  "eventId": "uuid",
  "orderNumber": "ORD-20241129-143022-123",
  "orderId": 1,
  "storeId": "store1",
  "distributorId": "dist1",
  "totalAmount": 150000,
  "occurredOn": "2024-11-29T14:30:22"
}
```

### SettlementCreated
정산 생성 시 발행

```json
{
  "eventId": "uuid",
  "settlementId": "SETTLE_abc123",
  "orderId": "ORD-20241129-143022-123",
  "storeId": "store1",
  "distributorId": "dist1",
  "settlementAmount": 150000,
  "occurredOn": "2024-11-29T14:30:22"
}
```

### SettlementCompleted
정산 완료 시 발행

```json
{
  "eventId": "uuid",
  "settlementId": "SETTLE_abc123",
  "orderId": "ORD-20241129-143022-123",
  "storeId": "store1",
  "paidAmount": 150000,
  "outstandingAmount": 0,
  "occurredOn": "2024-11-29T18:45:10"
}
```

---

## 테스트 방법

### 1. 통합 테스트 스크립트 실행
```bash
./test-settlement-flow.sh
```

### 2. 수동 테스트

#### Step 1: 카탈로그 주문 생성
```bash
# 장바구니에 상품 추가
curl -X POST "http://localhost:8080/api/catalog/cart/store1/add" \
  -H "Content-Type: application/json" \
  -d '{
    "distributorId": "dist1",
    "productId": 1,
    "quantity": 10
  }'

# 주문 생성
curl -X POST "http://localhost:8080/api/catalog/orders/store1" \
  -H "Content-Type: application/json" \
  -d '{
    "distributorId": "dist1",
    "deliveryAddress": "서울시 강남구",
    "deliveryPhone": "010-1234-5678"
  }'
```

#### Step 2: 결제 승인 (주문 확정)
```bash
curl -X POST "http://localhost:8080/api/catalog/orders/{orderId}/confirm?storeId=store1"
```

#### Step 3: 정산 확인
```bash
# 개별 정산 조회
curl "http://localhost:8080/api/settlements/store/store1"

# 일일 정산 조회
curl "http://localhost:8080/api/daily-settlements/store/store1"

# 정산 통계 조회
curl "http://localhost:8080/api/daily-settlements/store/store1/statistics"
```

---

## 모니터링

### 로그 확인
```bash
# 정산 생성 로그
grep "💰 정산 생성" logs/application.log

# 일일 정산 집계 로그
grep "📊 일일 정산 집계" logs/application.log

# 스케줄러 로그
grep "⏰ \[스케줄러\]" logs/application.log

# Kafka 이벤트 로그
grep "🎧 \[Kafka\]" logs/application.log
```

### Kafka 토픽 확인
```bash
# 토픽 메시지 확인
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic CatalogOrderPaymentCompleted --from-beginning

kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic SettlementCreated --from-beginning
```

---

## 주의사항

1. **정산 생성 시점**: 주문 확정(결제 완료) 시점에 자동 생성됩니다.
2. **일일 정산 집계**: 실시간으로 집계되며, 매일 새벽 1시에 재집계됩니다.
3. **정산 완료 처리**: 실제 입금 확인 후 수동으로 처리해야 합니다.
4. **미수금 관리**: 정산 완료 시 미수금이 자동으로 계산됩니다.

---

## 향후 개선 사항

- [ ] 정산 알림 기능 (미수금 알림, 정산 완료 알림)
- [ ] 정산 엑셀 다운로드 기능
- [ ] 정산 대시보드 UI
- [ ] 자동 정산 완료 처리 (PG사 연동)
- [ ] 정산 분쟁 처리 기능
- [ ] 정산 수수료 계산 기능
