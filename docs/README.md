# PayFlow 아키텍처 문서

## 📚 문서 구조

이 디렉토리는 PayFlow 프로젝트의 **아키텍처 의사결정 과정**과 **기술적 도전 해결 방법**을 담고 있습니다.

## 🎯 빠른 시작

### 채용 담당자님께
1. **[포트폴리오 README](../README_PORTFOLIO.md)** - 전체 개요 및 핵심 내용
2. **[아키텍처 여정](./ARCHITECTURE_JOURNEY.md)** - 모놀리식에서 MSA로의 진화 과정
3. **[기술적 도전과 해결](./TECHNICAL_CHALLENGES.md)** - 주요 문제와 해결 방법

### 기술 리더/아키텍트님께
1. **[ADR 문서들](./adr/)** - 상세한 의사결정 과정 및 트레이드오프
2. **[아키텍처 여정](./ARCHITECTURE_JOURNEY.md)** - 단계별 진화 과정
3. **[기술적 도전과 해결](./TECHNICAL_CHALLENGES.md)** - 구체적인 구현 방법

## 📖 문서 목록

### 1. 아키텍처 여정 (Architecture Journey)
**파일**: [ARCHITECTURE_JOURNEY.md](./ARCHITECTURE_JOURNEY.md)

**내용**:
- Phase 1: 모놀리식 시작 (MVP)
- Phase 2: 모듈러 모놀리스 전환
- Phase 3: 이벤트 기반 통신 도입
- Phase 4: Saga 패턴 적용
- Phase 5: 이벤트 소싱 적용
- Phase 6: MSA로 진화 (진행 중)

**읽는 시간**: 약 10분

### 2. 기술적 도전과 해결 (Technical Challenges)
**파일**: [TECHNICAL_CHALLENGES.md](./TECHNICAL_CHALLENGES.md)

**내용**:
1. 분산 트랜잭션 일관성 문제
   - 문제: 결제 승인 후 포인트 적립 실패
   - 해결: Saga 패턴 + 보상 트랜잭션
   - 결과: 데이터 불일치 0건

2. 이벤트 유실 방지
   - 문제: Kafka 메시지 처리 중 장애
   - 해결: 멱등성 키 + Outbox 패턴
   - 결과: 유실률 0%

3. 분산 시스템 추적
   - 문제: 여러 서비스를 거치는 요청 추적
   - 해결: Correlation ID + 중앙 로깅
   - 결과: 장애 파악 90% 단축

4. 단가 급등 감지 및 자동 발주
   - 문제: 식자재 단가 급등
   - 해결: 통계 기반 급등 감지 + 자동 경고
   - 결과: 과다 청구 방지

5. 메뉴 원가 자동 계산
   - 문제: 메뉴 원가 수동 계산
   - 해결: 단가 학습 시스템 연동
   - 결과: 계산 시간 99.7% 단축

**읽는 시간**: 약 15분

### 3. ADR (Architecture Decision Records)
**디렉토리**: [adr/](./adr/)

#### ADR-001: MSA 아키텍처 선택
**파일**: [adr/001-msa-architecture.md](./adr/001-msa-architecture.md)

**핵심 내용**:
- **컨텍스트**: 모놀리식의 배포 리스크, 확장 제약
- **결정**: DDD 기반 MSA 아키텍처 채택
- **대안**: 모놀리식, 모듈러 모놀리스, 서버리스
- **결과**: 배포 빈도 10배 증가, 장애 영향 90% 감소

**읽는 시간**: 약 8분

#### ADR-002: Kafka 메시지 브로커 선택
**파일**: [adr/002-kafka-message-broker.md](./adr/002-kafka-message-broker.md)

**핵심 내용**:
- **컨텍스트**: 서비스 간 비동기 통신 필요
- **결정**: Apache Kafka 채택
- **대안**: RabbitMQ, AWS SQS/SNS, Redis Pub/Sub, MQTT
- **결과**: 초당 10,000건 처리, 메시지 영속성 보장

**읽는 시간**: 약 7분

#### ADR-003: Saga 패턴 적용
**파일**: [adr/003-saga-pattern.md](./adr/003-saga-pattern.md)

**핵심 내용**:
- **컨텍스트**: 분산 트랜잭션 일관성 문제
- **결정**: Choreography 기반 Saga 패턴 채택
- **대안**: Orchestration Saga, 2PC, 최종 일관성 무시
- **결과**: 성공률 98.5%, 데이터 불일치 0건

**읽는 시간**: 약 10분

#### ADR-004: DDD 전술적 패턴 적용
**파일**: [adr/004-ddd-tactical-patterns.md](./adr/004-ddd-tactical-patterns.md)

**핵심 내용**:
- **컨텍스트**: 비즈니스 로직 분산, 중복 코드
- **결정**: DDD 전술적 패턴 전면 적용
- **대안**: Anemic Domain Model, Transaction Script, Active Record
- **결과**: 테스트 커버리지 85%, 버그 67% 감소

**읽는 시간**: 약 12분

#### ADR-005: 이벤트 소싱 패턴 적용
**파일**: [adr/005-event-sourcing.md](./adr/005-event-sourcing.md)

**핵심 내용**:
- **컨텍스트**: 금융 거래 이력 7년 보관 의무
- **결정**: 결제 도메인에 이벤트 소싱 적용
- **대안**: 감사 로그 테이블, 데이터베이스 트리거, CDC
- **결과**: 완전한 감사 추적, 과거 시점 재구성 가능

**읽는 시간**: 약 10분

## 📊 문서 읽기 순서 추천

### 시간이 10분 있다면
1. [포트폴리오 README](../README_PORTFOLIO.md) (10분)

### 시간이 30분 있다면
1. [포트폴리오 README](../README_PORTFOLIO.md) (10분)
2. [아키텍처 여정](./ARCHITECTURE_JOURNEY.md) (10분)
3. [기술적 도전과 해결](./TECHNICAL_CHALLENGES.md) (10분)

### 시간이 1시간 있다면
1. [포트폴리오 README](../README_PORTFOLIO.md) (10분)
2. [아키텍처 여정](./ARCHITECTURE_JOURNEY.md) (10분)
3. [기술적 도전과 해결](./TECHNICAL_CHALLENGES.md) (15분)
4. [ADR-001: MSA 아키텍처](./adr/001-msa-architecture.md) (8분)
5. [ADR-002: Kafka 선택](./adr/002-kafka-message-broker.md) (7분)
6. [ADR-003: Saga 패턴](./adr/003-saga-pattern.md) (10분)

### 시간이 충분하다면
모든 ADR 문서를 순서대로 읽어보세요. 각 문서는 독립적으로 읽을 수 있지만, 순서대로 읽으면 아키텍처 진화 과정을 더 잘 이해할 수 있습니다.

## 🎯 핵심 메시지

### 1. 점진적 진화
처음부터 MSA로 시작하지 않았습니다. 모놀리식 → 모듈러 모놀리스 → MSA 순서로 진화하며 각 단계에서 문제를 해결했습니다.

### 2. 트레이드오프 인식
모든 아키텍처 결정에는 장단점이 있습니다. 비즈니스 요구사항에 맞는 선택이 중요합니다.

### 3. 측정 가능한 개선
"느낌"이 아닌 "숫자"로 개선을 증명합니다.
- 배포 빈도: 주 1회 → 일 1-2회 (10배)
- 장애 영향: 전체 시스템 → 단일 서비스 (90% 감소)
- 데이터 불일치: 월 10건 → 0건 (100% 해결)

### 4. 문서화의 중요성
6개월 후에도 "왜 이렇게 만들었는지" 알 수 있도록 ADR 문서를 작성했습니다.

## 피드백 : k1988522@naver.com

이 문서에 대한 피드백이나 질문이 있으시면 언제든지 연락주세요!

---

**"좋은 아키텍처는 결정을 미루는 것이 아니라, 올바른 결정을 내리고 그 이유를 기록하는 것이다."**
