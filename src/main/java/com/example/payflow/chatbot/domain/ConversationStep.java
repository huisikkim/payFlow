package com.example.payflow.chatbot.domain;

public enum ConversationStep {
    INITIAL,           // 초기 상태
    ASKING_REGION,     // 지역 질문 중
    ASKING_INDUSTRY,   // 업종 질문 중
    ASKING_SALARY,     // 연봉 질문 중
    SHOWING_RESULTS,   // 결과 표시 중
    ASKING_JOB_SELECTION, // 채용 공고 선택 중
    ASKING_TECH_STACK, // 기술 스택 질문 중
    CONDUCTING_INTERVIEW, // 면접 진행 중
    SHOWING_INTERVIEW_RESULT, // 면접 결과 표시 중
    COMPLETED          // 완료
}
