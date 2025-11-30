# 품질 이슈 신고 시스템 - 사용 예시

## 시나리오: 양파 품질 불량으로 환불 요청

### 1단계: 가게사장님이 품질 이슈 신고

**상황**: 홍길동 식당 사장님이 신선식자재에서 주문한 양파 10kg가 절반 이상 썩은 상태로 배송됨

```bash
curl -X POST "http://localhost:8080/api/quality-issues" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "orderId": 123,
    "itemId": 456,
    "itemName": "양파 10kg",
    "storeId": "STORE_001",
    "storeName": "홍길동 식당",
    "distributorId": "DIST_001",
    "issueType": "POOR_QUALITY",
    "photoUrls": [
      "https://example.com/rotten-onion-1.jpg",
      "https://example.com/rotten-onion-2.jpg"
    ],
    "description": "양파가 썩었습니다. 절반 이상이 사용 불가능한 상태입니다. 오늘 저녁 영업에 차질이 생길 것 같습니다.",
    "requestAction": "REFUND"
  }'
```

**응답**:
```json
{
  "id": 1,
  "status": "SUBMITTED",
  "statusDescription": "접수됨",
  "submittedAt": "2025-11-30T10:30:00"
}
```

### 2단계: 유통업자가 대기 중인 이슈 확인

**상황**: 신선식자재 담당자가 새로운 품질 이슈 알림을 받고 확인

```bash
curl -X GET "http://localhost:8080/api/quality-issues/distributor/DIST_001/pending" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**응답**:
```json
[
  {
    "id": 1,
    "itemName": "양파 10kg",
    "storeName": "홍길동 식당",
    "issueType": "POOR_QUALITY",
    "issueTypeDescription": "품질 불량",
    "photoUrls": [
      "https://example.com/rotten-onion-1.jpg",
      "https://example.com/rotten-onion-2.jpg"
    ],
    "description": "양파가 썩었습니다. 절반 이상이 사용 불가능한 상태입니다.",
    "requestAction": "REFUND",
    "status": "SUBMITTED",
    "submittedAt": "2025-11-30T10:30:00"
  }
]
```

### 3단계: 유통업자가 사진 확인 후 검토 시작

```bash
curl -X POST "http://localhost:8080/api/quality-issues/1/review" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4단계: 유통업자가 품질 이슈 승인

**상황**: 사진을 확인한 결과 실제로 품질 문제가 맞다고 판단

```bash
curl -X POST "http://localhost:8080/api/quality-issues/1/approve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "comment": "사진 확인했습니다. 품질 문제가 맞습니다. 전액 환불 처리하겠습니다. 불편을 드려 죄송합니다."
  }'
```

**응답**:
```json
{
  "id": 1,
  "status": "APPROVED",
  "statusDescription": "승인됨",
  "reviewerComment": "사진 확인했습니다. 품질 문제가 맞습니다. 전액 환불 처리하겠습니다.",
  "reviewedAt": "2025-11-30T10:45:00"
}
```

### 5단계: 유통업자가 당일 수거 예약

**상황**: 오늘 오후 2시에 수거 예약

```bash
curl -X POST "http://localhost:8080/api/quality-issues/1/schedule-pickup" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "pickupTime": "2025-11-30T14:00:00"
  }'
```

**응답**:
```json
{
  "id": 1,
  "status": "PICKUP_SCHEDULED",
  "statusDescription": "수거 예정",
  "pickupScheduledAt": "2025-11-30T14:00:00"
}
```

### 6단계: 수거 완료

**상황**: 오후 2시에 배송 기사가 방문하여 불량 양파 수거 완료

```bash
curl -X POST "http://localhost:8080/api/quality-issues/1/complete-pickup" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**응답**:
```json
{
  "id": 1,
  "status": "PICKED_UP",
  "statusDescription": "수거 완료"
}
```

### 7단계: 환불 완료

**상황**: 수거 확인 후 즉시 환불 처리

```bash
curl -X POST "http://localhost:8080/api/quality-issues/1/complete-resolution" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "note": "환불 처리 완료했습니다. 다음 주문 시 10% 할인 쿠폰을 제공하겠습니다. 다시 한번 불편을 드려 죄송합니다."
  }'
```

**응답**:
```json
{
  "id": 1,
  "status": "REFUNDED",
  "statusDescription": "환불 완료",
  "resolutionNote": "환불 처리 완료했습니다. 다음 주문 시 10% 할인 쿠폰을 제공하겠습니다.",
  "resolvedAt": "2025-11-30T14:30:00"
}
```

---

## 시나리오 2: 감자 포장 파손으로 교환 요청

### 1단계: 가게사장님이 교환 신청

**상황**: 감자 20kg 포장이 파손되어 흙투성이

```bash
curl -X POST "http://localhost:8080/api/quality-issues" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "orderId": 124,
    "itemId": 457,
    "itemName": "감자 20kg",
    "storeId": "STORE_001",
    "storeName": "홍길동 식당",
    "distributorId": "DIST_001",
    "issueType": "DAMAGED",
    "photoUrls": [
      "https://example.com/damaged-potato-1.jpg"
    ],
    "description": "포장이 파손되어 감자가 흙투성이입니다. 세척하기 어려운 상태입니다.",
    "requestAction": "EXCHANGE"
  }'
```

### 2단계: 유통업자가 교환 승인

```bash
curl -X POST "http://localhost:8080/api/quality-issues/2/approve" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "comment": "포장 파손 확인했습니다. 새 제품으로 교환해드리겠습니다."
  }'
```

### 3단계: 수거 예약 및 완료

```bash
# 수거 예약
curl -X POST "http://localhost:8080/api/quality-issues/2/schedule-pickup" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "pickupTime": "2025-11-30T14:00:00"
  }'

# 수거 완료
curl -X POST "http://localhost:8080/api/quality-issues/2/complete-pickup" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4단계: 교환 완료

**상황**: 새 제품 재배송 완료

```bash
curl -X POST "http://localhost:8080/api/quality-issues/2/complete-resolution" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "note": "새 제품으로 교환 배송 완료했습니다. 포장을 더욱 견고하게 했습니다."
  }'
```

**응답**:
```json
{
  "id": 2,
  "status": "EXCHANGED",
  "statusDescription": "교환 완료",
  "resolutionNote": "새 제품으로 교환 배송 완료했습니다. 포장을 더욱 견고하게 했습니다.",
  "resolvedAt": "2025-11-30T16:00:00"
}
```

---

## 시나리오 3: 유통업자가 품질 이슈 거절

### 상황: 사진 확인 결과 정상 제품으로 판단

```bash
curl -X POST "http://localhost:8080/api/quality-issues/3/reject" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "comment": "사진 확인 결과 정상 제품으로 보입니다. 양파 겉껍질이 약간 변색된 것은 자연스러운 현상입니다. 속은 정상이니 사용하셔도 됩니다."
  }'
```

**응답**:
```json
{
  "id": 3,
  "status": "REJECTED",
  "statusDescription": "거절됨",
  "reviewerComment": "사진 확인 결과 정상 제품으로 보입니다. 양파 겉껍질이 약간 변색된 것은 자연스러운 현상입니다.",
  "reviewedAt": "2025-11-30T11:00:00",
  "resolvedAt": "2025-11-30T11:00:00"
}
```

---

## 가게사장님이 내 품질 이슈 목록 확인

```bash
curl -X GET "http://localhost:8080/api/quality-issues/store/STORE_001" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**응답**:
```json
[
  {
    "id": 1,
    "itemName": "양파 10kg",
    "issueType": "POOR_QUALITY",
    "requestAction": "REFUND",
    "status": "REFUNDED",
    "submittedAt": "2025-11-30T10:30:00",
    "resolvedAt": "2025-11-30T14:30:00"
  },
  {
    "id": 2,
    "itemName": "감자 20kg",
    "issueType": "DAMAGED",
    "requestAction": "EXCHANGE",
    "status": "EXCHANGED",
    "submittedAt": "2025-11-30T11:00:00",
    "resolvedAt": "2025-11-30T16:00:00"
  }
]
```

---

## 타임라인 요약

### 환불 케이스 (양파)
- **10:30** - 가게사장님 품질 이슈 신고
- **10:45** - 유통업자 검토 및 승인
- **10:50** - 당일 오후 2시 수거 예약
- **14:00** - 수거 완료
- **14:30** - 환불 처리 완료

**총 소요 시간: 4시간**

### 교환 케이스 (감자)
- **11:00** - 가게사장님 교환 신청
- **11:15** - 유통업자 승인
- **11:20** - 당일 오후 2시 수거 예약
- **14:00** - 수거 완료
- **16:00** - 새 제품 재배송 완료

**총 소요 시간: 5시간**

---

## 핵심 포인트

1. **빠른 신고**: 사진과 간단한 설명만으로 즉시 신고 가능
2. **투명한 프로세스**: 모든 단계가 명확하게 추적됨
3. **당일 처리**: 승인 후 당일 수거 및 처리
4. **100% 보장**: 품질 문제 시 전액 환불 또는 교환
5. **고객 만족**: 빠른 대응으로 고객 신뢰 향상
