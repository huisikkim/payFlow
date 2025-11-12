package com.example.payflow.chatbot.application;

import com.example.payflow.chatbot.domain.Intent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class ResponseGenerator {

    private final Random random = new Random();

    private final Map<Intent, List<String>> responses = createResponses();

    private Map<Intent, List<String>> createResponses() {
        Map<Intent, List<String>> map = new HashMap<>();
        
        map.put(Intent.GREETING, List.of(
            "안녕하세요! 👋\n궁금하신 내용을 선택하거나 직접 질문해주세요!"
        ));
        
        map.put(Intent.ABOUT_ME, List.of(
            "💼 자기소개\n\n" +
            "백엔드 개발자로, 레거시 시스템을 현대화하고 확장 가능한 아키텍처로 전환하는 데 강점을 가지고 있습니다.\n\n" +
            "주요 경험:\n" +
            "• 블록체인 거래소 구축\n" +
            "• IoT 플랫폼 개발\n" +
            "• 주문/결제/재고/티켓 MSA 시스템 구축\n" +
            "• 0→1 구축 경험 다수\n\n" +
            "빠르게 변화하는 환경에서 기술적 의사결정과 실행을 병행하는 실무형 아키텍트입니다."
        ));

        
        map.put(Intent.ARCHITECTURE_EXPERIENCE, List.of(
            "🏗️ 아키텍처 설계 경험\n\n" +
            "핵심 역량:\n\n" +
            "• MSA (Microservices Architecture)\n" +
            "  - 모놀리스에서 MSA로 점진적 전환 경험\n" +
            "  - 도메인별 서비스 분리 (주문/결제/재고/티켓)\n" +
            "  - 서비스 간 독립 배포 및 장애 격리\n\n" +
            "• EDA (Event-Driven Architecture)\n" +
            "  - Kafka 기반 이벤트 스트림 구축\n" +
            "  - 도메인 이벤트 발행/구독 패턴\n" +
            "  - 실시간 비즈니스 로직 처리\n\n" +
            "• DDD (Domain-Driven Design)\n" +
            "  - Aggregate, Entity, Value Object 패턴 적용\n" +
            "  - 도메인 로직 캡슐화 및 일관성 보장\n\n" +
            "• Saga Pattern\n" +
            "  - 분산 트랜잭션 없는 데이터 일관성 확보\n" +
            "  - 보상 트랜잭션 설계 및 구현"
        ));
        
        map.put(Intent.LEGACY_MODERNIZATION, List.of(
            "🔧 레거시 현대화 전략\n\n" +
            "실전 경험을 바탕으로 한 접근법:\n\n" +
            "1️⃣ 모듈러 모놀리스 단계\n" +
            "   - 기존 모놀리스를 도메인별 모듈로 분리\n" +
            "   - 도메인 간 의존도 70% 이상 감소\n" +
            "   - MSA 전환 용이성 확보\n\n" +
            "2️⃣ 이벤트 기반 통신 도입\n" +
            "   - Kafka로 모듈 간 비동기 통신\n" +
            "   - 메시지 브로커 추상화 (교체 가능성 대비)\n\n" +
            "3️⃣ DDD 패턴 적용\n" +
            "   - 단일 DB 내에서도 트랜잭션 분리\n" +
            "   - 도메인 경계 명확화\n\n" +
            "4️⃣ 점진적 리팩토링\n" +
            "   - Big Bang 방식 지양\n" +
            "   - Strangler Fig 패턴 활용\n" +
            "   - 비즈니스 영향 최소화"
        ));
        
        map.put(Intent.TECH_STACK, List.of(
            "💻 기술 스택\n\n" +
            "Backend:\n" +
            "• Java 17, Spring Boot\n" +
            "• JPA/Hibernate, QueryDSL, MyBatis\n" +
            "• PHP (CodeIgniter), Node.js\n\n" +
            "Architecture:\n" +
            "• MSA, EDA, DDD, Saga Pattern\n" +
            "• Modular Monolith, RESTful API\n\n" +
            "Message & Event:\n" +
            "• Apache Kafka, RabbitMQ\n" +
            "• MQTT (IoT)\n\n" +
            "Database:\n" +
            "• MySQL/MariaDB, PostgreSQL, Oracle\n" +
            "• MongoDB, Redis\n\n" +
            "Blockchain:\n" +
            "• Solidity, Web3j, Ethers.js\n" +
            "• ERC-20, 메타마스크 연동\n\n" +
            "DevOps:\n" +
            "• AWS (S3), Docker\n" +
            "• Jenkins, GitHub Actions, ArgoCD\n" +
            "• Grafana 모니터링"
        ));
        
        map.put(Intent.PROJECT_EXPERIENCE, List.of(
            "📂 주요 프로젝트 경험\n\n" +
            "1️⃣ 981파크 예약/결제 시스템 (2022~현재)\n" +
            "   - MSA 아키텍처 전환 및 운영\n" +
            "   - Kafka 기반 EDA 구현\n" +
            "   - DDD 기반 도메인 모델링\n" +
            "   - GitHub Actions CI/CD 구축\n\n" +
            "2️⃣ 블록체인 거래소 (2018~2020)\n" +
            "   - 국내 거래소 OPEN API 활용\n" +
            "   - 24시간 자동거래 봇 개발\n" +
            "   - 메인넷 코인 발행 및 배포\n" +
            "   - Web3J 지갑 연동\n\n" +
            "3️⃣ 금융/공공/제조 다수 프로젝트 (2007~2018)\n" +
            "   - 시티카드 콜센터 시스템\n" +
            "   - 전자금융계산서 시스템\n" +
            "   - 외국인정보 공동이용 시스템\n" +
            "   - SK Innovation 배터리 견적 시스템"
        ));
        
        map.put(Intent.INTERVIEW_TIP, List.of(
            "🎤 면접 준비 팁\n\n" +
            "시니어 개발자 면접에서 중요한 것:\n\n" +
            "1️⃣ 기술적 의사결정 과정 설명\n" +
            "   - \"왜 MSA를 선택했나요?\"\n" +
            "   - \"어떤 기준으로 기술을 선택하나요?\"\n" +
            "   - 트레이드오프를 명확히 설명\n\n" +
            "2️⃣ 문제 해결 경험 구체화\n" +
            "   - STAR 기법 활용\n" +
            "   - 정량적 성과 제시 (성능 개선 %, 장애 감소율)\n\n" +
            "3️⃣ 아키텍처 설계 능력 증명\n" +
            "   - 화이트보드 코딩보다 시스템 설계 중요\n" +
            "   - 확장성, 가용성, 일관성 고려사항 설명\n\n" +
            "4️⃣ 리더십과 협업 경험\n" +
            "   - 주니어 멘토링 경험\n" +
            "   - 크로스팀 협업 사례\n\n" +
            "5️⃣ 역질문 준비\n" +
            "   - 기술 스택 선택 이유\n" +
            "   - 팀 구조와 개발 프로세스\n" +
            "   - 기술 부채 해결 방식"
        ));
        
        map.put(Intent.RESUME_TIP, List.of(
            "📝 이력서 작성 팁\n\n" +
            "시니어 개발자 이력서 핵심:\n\n" +
            "1️⃣ 임팩트 중심으로 작성\n" +
            "   ❌ \"Spring Boot로 API 개발\"\n" +
            "   ✅ \"모놀리스를 MSA로 전환하여 배포 시간 70% 단축\"\n\n" +
            "2️⃣ 기술 스택은 맥락과 함께\n" +
            "   - 단순 나열 X\n" +
            "   - 어떤 문제를 해결하기 위해 사용했는지\n\n" +
            "3️⃣ 아키텍처 설계 경험 강조\n" +
            "   - MSA, EDA, DDD 등 패턴 적용 경험\n" +
            "   - 기술적 의사결정 과정\n\n" +
            "4️⃣ 정량적 성과 포함\n" +
            "   - 성능 개선: \"응답시간 500ms → 50ms\"\n" +
            "   - 비용 절감: \"인프라 비용 30% 절감\"\n" +
            "   - 생산성: \"배포 주기 월 1회 → 주 2회\"\n\n" +
            "5️⃣ GitHub 링크 필수\n" +
            "   - 실무 수준의 코드 공개\n" +
            "   - README에 아키텍처 설명"
        ));

        
        map.put(Intent.COMPANY_CULTURE, List.of(
            "🏢 회사 선택 기준\n\n" +
            "회사 선택 기준:\n\n" +
            "1️⃣ 기술 부채 관리 방식\n" +
            "   - 레거시 개선에 투자하는가?\n" +
            "   - 기술 부채 해결 시간을 주는가?\n\n" +
            "2️⃣ 개발 문화\n" +
            "   - 코드 리뷰 문화\n" +
            "   - 테스트 코드 작성 여부\n" +
            "   - CI/CD 자동화 수준\n\n" +
            "3️⃣ 성장 기회\n" +
            "   - 새로운 기술 도입 가능성\n" +
            "   - 컨퍼런스 참여 지원\n" +
            "   - 교육비 지원\n\n" +
            "4️⃣ 의사결정 구조\n" +
            "   - 기술적 의사결정 권한\n" +
            "   - 상향식 vs 하향식\n\n" +
            "5️⃣ 워라밸\n" +
            "   - 야근 문화\n" +
            "   - 재택근무 가능 여부\n" +
            "   - 휴가 사용 자유도\n\n"
        ));
        
        map.put(Intent.HELP, List.of(
            "❓ 질문 가이드\n\n" +
            "다음 주제에 대해 질문하실 수 있습니다:\n\n" +
            "• 자기소개 - 경력과 강점\n" +
            "• 아키텍처 경험 - MSA, EDA, DDD\n" +
            "• 레거시 현대화 - 실전 리팩토링 전략\n" +
            "• 기술 스택 - 사용 기술 및 도구\n" +
            "• 프로젝트 경험 - 주요 프로젝트 소개\n" +
            "• 회사 문화 - 회사 선택 기준\n\n" +
            "버튼을 클릭하거나 직접 질문해주세요!"
        ));
        
        map.put(Intent.RESTART, List.of(
            "처음으로 돌아갑니다. 🔄\n\n궁금하신 내용을 선택하거나 질문해주세요!"
        ));
        
        map.put(Intent.UNKNOWN, List.of(
            "죄송합니다. 잘 이해하지 못했어요. 😅\n\n" +
            "'도움말'을 입력하시면 질문 가능한 주제를 확인하실 수 있습니다."
        ));
        
        return map;
    }

    public String generate(Intent intent) {
        List<String> intentResponses = responses.get(intent);
        if (intentResponses == null || intentResponses.isEmpty()) {
            return responses.get(Intent.UNKNOWN).get(0);
        }
        return intentResponses.get(random.nextInt(intentResponses.size()));
    }

    public String generateWithContext(Intent intent, String context) {
        String baseResponse = generate(intent);
        if (context != null && !context.isEmpty()) {
            return baseResponse + "\n\n" + context;
        }
        return baseResponse;
    }
}
