# 🔨 PickSwap 경매/입찰 기능 추가 태스크

## 📋 개요

PickSwap 중고 플랫폼에 **실시간 경매 및 입찰 시스템**을 추가하여 차별화된 거래 방식을 제공합니다.

### 핵심 가치
- 🎯 **가격 투명성**: 공개 입찰로 시장 가격 형성
- ⏰ **긴박감 조성**: 시간 제한으로 빠른 거래 유도
- 💰 **최고가 판매**: 판매자에게 유리한 가격 형성
- 🎮 **재미 요소**: 경매 참여의 게임화

---

## 🎯 Phase 1: 도메인 모델 설계 (우선순위: 높음)

### Task 1.1: 경매 도메인 엔티티 생성
**파일**: `src/main/java/com/example/payflow/auction/domain/`

#### 생성할 엔티티:

1. **Auction.java** (경매 집합 루트)
   - `id`: 경매 ID
   - `productId`: 상품 ID (Product 참조)
   - `sellerId`: 판매자 ID
   - `startPrice`: 시작가
   - `currentPrice`: 현재가
   - `buyNowPrice`: 즉시 구매가 (선택)
   - `minBidIncrement`: 최소 입찰 단위 (예: 1,000원)
   - `startTime`: 경매 시작 시간
   - `endTime`: 경매 종료 시간
   - `status`: 경매 상태 (SCHEDULED, ACTIVE, ENDED, CANCELLED)
   - `winnerId`: 낙찰자 ID
   - `winningBidId`: 낙찰 입찰 ID
   - `bidCount`: 총 입찰 횟수
   - `viewCount`: 조회수

2. **Bid.java** (입찰)
   - `id`: 입찰 ID
   - `auctionId`: 경매 ID
   - `bidderId`: 입찰자 ID
   - `bidderName`: 입찰자 이름
   - `amount`: 입찰 금액
   - `bidTime`: 입찰 시간
   - `isWinning`: 현재 최고가 여부
   - `isAutoBid`: 자동 입찰 여부

3. **AutoBid.java** (자동 입찰 설정)
   - `id`: 자동 입찰 ID
   - `auctionId`: 경매 ID
   - `bidderId`: 입찰자 ID
   - `maxAmount`: 최대 입찰 금액
   - `isActive`: 활성화 여부

4. **AuctionStatus.java** (Enum)
   - `SCHEDULED`: 예정
   - `ACTIVE`: 진행 중
   - `ENDED`: 종료
   - `CANCELLED`: 취소

5. **BidHistory.java** (입찰 이력 - 이벤트 소싱용)
   - `id`: 이력 ID
   - `auctionId`: 경매 ID
   - `bidderId`: 입찰자 ID
   - `amount`: 입찰 금액
   - `timestamp`: 시간
   - `eventType`: 이벤트 타입 (BID_PLACED, BID_OUTBID, AUCTION_WON)

### Task 1.2: Repository 인터페이스 생성
- `AuctionRepository.java`
- `BidRepository.java`
- `AutoBidRepository.java`
- `BidHistoryRepository.java`

**주요 쿼리 메서드**:
```java
// AuctionRepository
List<Auction> findByStatus(AuctionStatus status);
List<Auction> findByStatusAndEndTimeBefore(AuctionStatus status, LocalDateTime time);
List<Auction> findByProductId(Long productId);
List<Auction> findBySellerId(String sellerId);

// BidRepository
List<Bid> findByAuctionIdOrderByAmountDesc(Long auctionId);
Optional<Bid> findTopByAuctionIdOrderByAmountDesc(Long auctionId);
List<Bid> findByBidderId(String bidderId);
int countByAuctionId(Long auctionId);

// AutoBidRepository
Optional<AutoBid> findByAuctionIdAndBidderIdAndIsActiveTrue(Long auctionId, String bidderId);
List<AutoBid> findByAuctionIdAndIsActiveTrue(Long auctionId);
```

---

## 🔧 Phase 2: 비즈니스 로직 구현 (우선순위: 높음)

### Task 2.1: 경매 서비스 생성
**파일**: `src/main/java/com/example/payflow/auction/application/AuctionService.java`

**주요 기능**:
1. **경매 생성**
   - 상품 검증 (존재 여부, 판매자 권한)
   - 시작가, 종료 시간 검증
   - 경매 생성 및 상품 상태 변경 (AUCTION_ACTIVE)

2. **경매 조회**
   - 진행 중인 경매 목록
   - 예정된 경매 목록
   - 종료된 경매 목록
   - 카테고리별 경매
   - 인기 경매 (입찰 많은 순)

3. **경매 종료 처리**
   - 스케줄러로 자동 종료
   - 낙찰자 결정
   - 상품 상태 변경 (SOLD)
   - 낙찰 알림 발송

4. **경매 취소**
   - 입찰 없을 때만 취소 가능
   - 상품 상태 복원

### Task 2.2: 입찰 서비스 생성
**파일**: `src/main/java/com/example/payflow/auction/application/BidService.java`

**주요 기능**:
1. **입찰하기**
   - 경매 상태 검증 (ACTIVE)
   - 입찰 금액 검증 (현재가 + 최소 단위)
   - 본인 입찰 방지
   - 입찰 생성 및 경매 현재가 업데이트
   - 이전 최고 입찰자에게 알림
   - 입찰 이력 저장 (이벤트 소싱)

2. **즉시 구매**
   - 즉시 구매가 설정 확인
   - 경매 즉시 종료
   - 낙찰 처리

3. **입찰 내역 조회**
   - 경매별 입찰 내역
   - 사용자별 입찰 내역
   - 입찰 순위 조회

### Task 2.3: 자동 입찰 서비스 생성
**파일**: `src/main/java/com/example/payflow/auction/application/AutoBidService.java`

**주요 기능**:
1. **자동 입찰 설정**
   - 최대 금액 설정
   - 활성화/비활성화

2. **자동 입찰 실행**
   - 다른 사용자 입찰 시 자동으로 재입찰
   - 최대 금액 범위 내에서만 입찰
   - 최소 단위만큼 증가

3. **자동 입찰 중지**
   - 최대 금액 도달 시 자동 중지
   - 사용자 수동 중지

---

## 🎨 Phase 3: API 및 웹 UI 구현 (우선순위: 중간)

### Task 3.1: REST API 컨트롤러
**파일**: `src/main/java/com/example/payflow/auction/presentation/AuctionController.java`

**엔드포인트**:
```
POST   /api/auctions                    # 경매 생성
GET    /api/auctions                    # 경매 목록
GET    /api/auctions/active             # 진행 중 경매
GET    /api/auctions/scheduled          # 예정 경매
GET    /api/auctions/ended              # 종료 경매
GET    /api/auctions/{id}               # 경매 상세
DELETE /api/auctions/{id}               # 경매 취소
POST   /api/auctions/{id}/end           # 경매 강제 종료 (관리자)

POST   /api/auctions/{id}/bids          # 입찰하기
POST   /api/auctions/{id}/buy-now       # 즉시 구매
GET    /api/auctions/{id}/bids          # 입찰 내역
GET    /api/auctions/{id}/top-bid       # 현재 최고가

POST   /api/auctions/{id}/auto-bid      # 자동 입찰 설정
DELETE /api/auctions/{id}/auto-bid      # 자동 입찰 취소
GET    /api/auctions/{id}/auto-bid      # 자동 입찰 조회

GET    /api/bids/my                     # 내 입찰 내역
GET    /api/auctions/my-selling         # 내가 판매 중인 경매
GET    /api/auctions/my-winning         # 내가 낙찰 중인 경매
```

### Task 3.2: 웹 컨트롤러
**파일**: `src/main/java/com/example/payflow/auction/presentation/AuctionWebController.java`

**페이지**:
```
GET /pickswap/auctions                  # 경매 목록 페이지
GET /pickswap/auctions/{id}             # 경매 상세 페이지
GET /pickswap/auctions/create           # 경매 생성 페이지
GET /pickswap/auctions/my               # 내 경매 관리 페이지
```

### Task 3.3: Thymeleaf 템플릿 생성
**파일**: `src/main/resources/templates/auction/`

1. **auction-list.html** (경매 목록)
   - 진행 중 / 예정 / 종료 탭
   - 카테고리 필터
   - 정렬 (마감 임박순, 입찰 많은 순, 최신순)
   - 실시간 남은 시간 표시 (JavaScript)

2. **auction-detail.html** (경매 상세)
   - 상품 정보
   - 현재가, 입찰 횟수
   - 남은 시간 (실시간 카운트다운)
   - 입찰 폼
   - 즉시 구매 버튼
   - 자동 입찰 설정
   - 입찰 내역 (실시간 업데이트)

3. **auction-create.html** (경매 생성)
   - 상품 선택 (내 상품 중)
   - 시작가 입력
   - 즉시 구매가 입력 (선택)
   - 경매 기간 설정
   - 최소 입찰 단위 설정

4. **my-auctions.html** (내 경매 관리)
   - 판매 중인 경매
   - 입찰 중인 경매
   - 낙찰된 경매
   - 종료된 경매

---

## ⚡ Phase 4: 실시간 기능 구현 (우선순위: 중간)

### Task 4.1: 경매 종료 스케줄러
**파일**: `src/main/java/com/example/payflow/auction/infrastructure/AuctionScheduler.java`

**기능**:
- 1분마다 종료 시간 도달한 경매 확인
- 자동 종료 처리
- 낙찰자 결정
- 알림 발송

```java
@Scheduled(fixedRate = 60000) // 1분마다
public void closeExpiredAuctions() {
    // 종료 시간 지난 ACTIVE 경매 조회
    // 낙찰 처리
    // 이벤트 발행
}
```

### Task 4.2: 실시간 업데이트 (선택 사항)
**방법 1: Polling (간단)**
- JavaScript로 5초마다 API 호출
- 현재가, 입찰 내역 업데이트

**방법 2: WebSocket (고급)**
- Spring WebSocket 사용
- 입찰 시 실시간 브로드캐스트
- 모든 참여자에게 즉시 업데이트

### Task 4.3: 자동 입찰 트리거
**파일**: `src/main/java/com/example/payflow/auction/application/AutoBidTrigger.java`

**기능**:
- 새 입찰 발생 시 자동 입찰 설정 확인
- 조건 충족 시 자동으로 재입찰
- 최대 금액 범위 내에서만 실행

---

## 📊 Phase 5: 이벤트 및 알림 (우선순위: 낮음)

### Task 5.1: 경매 이벤트 정의
**파일**: `src/main/java/com/example/payflow/auction/domain/events/`

**이벤트 종류**:
- `AuctionCreated`: 경매 생성
- `BidPlaced`: 입찰 발생
- `BidOutbid`: 입찰 밀림
- `AuctionEnded`: 경매 종료
- `AuctionWon`: 낙찰
- `AutoBidTriggered`: 자동 입찰 실행

### Task 5.2: Kafka 이벤트 발행
**파일**: `src/main/java/com/example/payflow/auction/infrastructure/AuctionEventPublisher.java`

**기능**:
- 경매 관련 이벤트를 Kafka로 발행
- 로깅 시스템과 연동
- 알림 서비스 트리거

### Task 5.3: 알림 서비스 (선택 사항)
**기능**:
- 입찰 밀렸을 때 알림
- 경매 종료 임박 알림 (5분 전)
- 낙찰 알림
- 경매 시작 알림 (관심 상품)

---

## 🧪 Phase 6: 테스트 및 검증 (우선순위: 중간)

### Task 6.1: 단위 테스트
**파일**: `src/test/java/com/example/payflow/auction/`

**테스트 대상**:
- 입찰 금액 검증 로직
- 경매 종료 로직
- 자동 입찰 로직
- 낙찰자 결정 로직

### Task 6.2: 통합 테스트
**테스트 시나리오**:
1. 경매 생성 → 입찰 → 종료 → 낙찰
2. 자동 입찰 설정 → 다른 사용자 입찰 → 자동 재입찰
3. 즉시 구매 → 경매 즉시 종료
4. 동시 입찰 처리 (동시성 테스트)

### Task 6.3: API 테스트 스크립트
**파일**: `test-auction-api.ps1` (Windows) / `test-auction-api.sh` (Linux/Mac)

**테스트 내용**:
- 경매 생성
- 입찰하기
- 자동 입찰 설정
- 경매 조회
- 입찰 내역 조회

---

## 🎯 Phase 7: 추가 기능 (우선순위: 낮음)

### Task 7.1: 경매 통계 대시보드
**기능**:
- 총 경매 수
- 평균 입찰 횟수
- 평균 낙찰가
- 인기 카테고리
- 시간대별 경매 활동

### Task 7.2: 관심 경매 (Watchlist)
**기능**:
- 경매 즐겨찾기
- 관심 경매 알림
- 관심 경매 목록

### Task 7.3: 경매 히스토리
**기능**:
- 과거 경매 내역
- 낙찰가 통계
- 가격 추이 그래프

### Task 7.4: 경매 규칙 설정
**기능**:
- 최소 경매 기간 설정
- 최대 경매 기간 설정
- 입찰 수수료 설정
- 낙찰 수수료 설정

---

## 📝 데이터베이스 스키마

### auctions 테이블
```sql
CREATE TABLE auctions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    seller_id VARCHAR(255) NOT NULL,
    start_price DECIMAL(15,2) NOT NULL,
    current_price DECIMAL(15,2) NOT NULL,
    buy_now_price DECIMAL(15,2),
    min_bid_increment DECIMAL(15,2) NOT NULL DEFAULT 1000,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    winner_id VARCHAR(255),
    winning_bid_id BIGINT,
    bid_count INT DEFAULT 0,
    view_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

### bids 테이블
```sql
CREATE TABLE bids (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    auction_id BIGINT NOT NULL,
    bidder_id VARCHAR(255) NOT NULL,
    bidder_name VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_winning BOOLEAN DEFAULT FALSE,
    is_auto_bid BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (auction_id) REFERENCES auctions(id)
);
```

### auto_bids 테이블
```sql
CREATE TABLE auto_bids (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    auction_id BIGINT NOT NULL,
    bidder_id VARCHAR(255) NOT NULL,
    max_amount DECIMAL(15,2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auctions(id),
    UNIQUE KEY unique_active_autobid (auction_id, bidder_id, is_active)
);
```

### bid_history 테이블 (이벤트 소싱)
```sql
CREATE TABLE bid_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    auction_id BIGINT NOT NULL,
    bidder_id VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auctions(id)
);
```

---

## 🚀 구현 순서 권장

1. **Phase 1 (1-2일)**: 도메인 모델 및 Repository
2. **Phase 2 (2-3일)**: 비즈니스 로직 (경매, 입찰 서비스)
3. **Phase 3 (2-3일)**: API 및 기본 웹 UI
4. **Phase 4 (1-2일)**: 스케줄러 및 자동 입찰
5. **Phase 6 (1일)**: 테스트 작성
6. **Phase 5 (1일)**: 이벤트 및 알림 (선택)
7. **Phase 7 (1-2일)**: 추가 기능 (선택)

**총 예상 기간**: 8-14일 (핵심 기능만: 6-8일)

---

## 🎨 UI/UX 고려사항

### 경매 목록 페이지
- 마감 임박 경매 강조 (빨간색 타이머)
- 입찰 많은 경매 배지 표시
- 실시간 남은 시간 표시

### 경매 상세 페이지
- 큰 카운트다운 타이머
- 현재가 강조 표시
- 입찰 버튼 눈에 띄게
- 입찰 내역 실시간 업데이트
- 자동 입찰 설정 토글

### 모바일 최적화
- 터치 친화적 버튼 크기
- 빠른 입찰 버튼 (현재가 + 최소 단위)
- 알림 권한 요청

---

## 🔒 보안 고려사항

1. **입찰 검증**
   - 본인 경매 입찰 방지
   - 최소 금액 검증
   - 경매 상태 검증

2. **동시성 제어**
   - 낙관적 락 (Optimistic Locking)
   - 트랜잭션 격리 수준 설정

3. **Rate Limiting**
   - 입찰 요청 제한 (1초에 1회)
   - API 호출 제한

4. **권한 검증**
   - 경매 생성: 상품 소유자만
   - 경매 취소: 판매자만
   - 입찰: 로그인 사용자만

---

## 📈 성능 최적화

1. **인덱스 추가**
   - `auctions(status, end_time)`
   - `bids(auction_id, amount DESC)`
   - `auto_bids(auction_id, bidder_id, is_active)`

2. **캐싱**
   - 진행 중인 경매 목록 (5분)
   - 경매 상세 정보 (1분)

3. **페이징**
   - 경매 목록 페이징 (20개씩)
   - 입찰 내역 페이징 (50개씩)

---

## 🎉 완료 후 기대 효과

1. **차별화된 거래 방식**: 일반 판매와 경매 선택 가능
2. **사용자 참여 증가**: 경매의 게임화 요소
3. **거래 활성화**: 긴박감으로 빠른 거래 유도
4. **가격 투명성**: 공개 입찰로 시장 가격 형성
5. **플랫폼 수익**: 낙찰 수수료 모델 가능

---

## 📚 참고 자료

- **유사 플랫폼**: 번개장터, 당근마켓, eBay
- **경매 알고리즘**: Vickrey Auction, English Auction
- **실시간 통신**: WebSocket, Server-Sent Events
- **동시성 제어**: Optimistic Locking, Pessimistic Locking
