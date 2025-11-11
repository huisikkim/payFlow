package com.example.payflow.sessionreplay.domain;

/**
 * 사용자 상호작용 이벤트 타입
 */
public enum EventType {
    CLICK,          // 클릭 이벤트
    SCROLL,         // 스크롤 이벤트
    INPUT,          // 입력 이벤트
    NAVIGATION,     // 페이지 이동
    MOUSE_MOVE,     // 마우스 이동 (선택적)
    RESIZE,         // 윈도우 리사이즈
    FOCUS,          // 포커스 이벤트
    BLUR            // 블러 이벤트
}
