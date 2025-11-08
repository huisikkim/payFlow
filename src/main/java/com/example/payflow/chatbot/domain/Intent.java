package com.example.payflow.chatbot.domain;

import java.util.Arrays;
import java.util.List;

public enum Intent {
    GREETING(
        "인사",
        Arrays.asList("안녕", "하이", "헬로", "hi", "hello", "반가워", "처음", "시작")
    ),
    ORDER_INQUIRY(
        "주문조회",
        Arrays.asList("주문", "주문조회", "내주문", "주문내역", "주문확인", "order")
    ),
    PAYMENT_INQUIRY(
        "결제조회",
        Arrays.asList("결제", "결제조회", "결제내역", "결제확인", "payment", "pay")
    ),
    DELIVERY_INQUIRY(
        "배송조회",
        Arrays.asList("배송", "배송조회", "언제도착", "배송상태", "언제오나", "delivery")
    ),
    REFUND_REQUEST(
        "환불요청",
        Arrays.asList("환불", "취소", "반품", "refund", "cancel", "환불하고싶어", "취소하고싶어")
    ),
    STAGE_INQUIRY(
        "정산조회",
        Arrays.asList("정산", "정산조회", "정산내역", "settlement")
    ),
    STAGE_GUIDE(
        "스테이지안내",
        Arrays.asList("스테이지", "stage", "계", "스테이지참여", "스테이지시작", "스테이지만들기", 
                     "스테이지가뭐야", "스테이지어떻게", "계모임", "순번", "참여방법")
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
