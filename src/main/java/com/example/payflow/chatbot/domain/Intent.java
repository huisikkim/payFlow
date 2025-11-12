package com.example.payflow.chatbot.domain;

import java.util.Arrays;
import java.util.List;

public enum Intent {
    GREETING(
        "인사",
        Arrays.asList("안녕", "하이", "헬로", "hi", "hello", "반가워", "처음", "시작")
    ),
    ABOUT_ME(
        "자기소개",
        Arrays.asList("소개", "누구", "경력", "프로필", "about", "who")
    ),
    CAREER_TRANSITION(
        "이직전략",
        Arrays.asList("이직", "전략", "준비", "어떻게", "방법", "transition")
    ),
    ARCHITECTURE_EXPERIENCE(
        "아키텍처경험",
        Arrays.asList("아키텍처", "설계", "MSA", "마이크로서비스", "architecture", "설계경험")
    ),
    LEGACY_MODERNIZATION(
        "레거시현대화",
        Arrays.asList("레거시", "현대화", "리팩토링", "모놀리스", "legacy", "refactoring")
    ),
    TECH_STACK(
        "기술스택",
        Arrays.asList("기술", "스택", "사용", "언어", "프레임워크", "tech", "stack")
    ),
    PROJECT_EXPERIENCE(
        "프로젝트경험",
        Arrays.asList("프로젝트", "경험", "구축", "개발", "project", "experience")
    ),
    INTERVIEW_TIP(
        "면접팁",
        Arrays.asList("면접", "팁", "조언", "준비", "interview", "tip")
    ),
    RESUME_TIP(
        "이력서팁",
        Arrays.asList("이력서", "포트폴리오", "resume", "cv", "작성")
    ),
    SALARY_NEGOTIATION(
        "연봉협상",
        Arrays.asList("연봉", "협상", "salary", "연봉협상", "급여")
    ),
    COMPANY_CULTURE(
        "회사문화",
        Arrays.asList("문화", "분위기", "환경", "culture", "회사선택")
    ),
    HELP(
        "도움말",
        Arrays.asList("도움", "도움말", "help", "뭐할수있어", "기능", "무엇", "뭐해줘", "질문")
    ),
    RESTART(
        "재시작",
        Arrays.asList("다시", "처음", "새로", "리셋", "reset", "restart")
    ),
    UNKNOWN(
        "알수없음",
        Arrays.asList()
    );

    private final String description;
    private final List<String> keywords;

    Intent(String description, List<String> keywords) {
        this.description = description;
        this.keywords = keywords;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public boolean matchesKeywords(String message) {
        if (this == UNKNOWN) {
            return false;
        }
        String normalized = message.toLowerCase().trim();
        return keywords.stream().anyMatch(normalized::contains);
    }

    public static Intent detectIntent(String message) {
        return Arrays.stream(values())
            .filter(intent -> intent.matchesKeywords(message))
            .findFirst()
            .orElse(UNKNOWN);
    }
}
