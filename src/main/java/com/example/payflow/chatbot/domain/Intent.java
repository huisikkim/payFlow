package com.example.payflow.chatbot.domain;

import java.util.Arrays;
import java.util.List;

public enum Intent {
    GREETING(
        "인사",
        Arrays.asList("안녕", "하이", "헬로", "hi", "hello", "반가워", "처음", "시작")
    ),
    JOB_SEARCH_START(
        "채용검색시작",
        Arrays.asList("채용", "구인", "일자리", "취업", "job", "채용공고", "구직", "채용정보", "회사찾기", "일찾기")
    ),
    REGION_SELECTION(
        "지역선택",
        Arrays.asList("서울", "경기", "인천", "부산", "대구", "광주", "대전", "울산", "세종", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주")
    ),
    INDUSTRY_SELECTION(
        "업종선택",
        Arrays.asList("IT", "금융", "제조", "유통", "서비스", "교육", "의료", "건설", "미디어", "게임", "스타트업")
    ),
    SALARY_INPUT(
        "연봉입력",
        Arrays.asList("만원", "천만원", "억", "연봉", "salary")
    ),
    RESTART_SEARCH(
        "검색재시작",
        Arrays.asList("다시", "재검색", "처음부터", "새로", "리셋", "reset")
    ),
    HELP(
        "도움말",
        Arrays.asList("도움", "도움말", "help", "뭐할수있어", "기능", "무엇", "뭐해줘")
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
