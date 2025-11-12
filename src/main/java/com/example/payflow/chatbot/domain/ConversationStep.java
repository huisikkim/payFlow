package com.example.payflow.chatbot.domain;

public enum ConversationStep {
    INITIAL,           // 초기 상태
    ASKING_REGION,     // 지역 질문 중
    ASKING_INDUSTRY,   // 업종 질문 중
    ASKING_SALARY,     // 연봉 질문 중
    SHOWING_RESULTS,   // 결과 표시 중
    COMPLETED          // 완료
}
