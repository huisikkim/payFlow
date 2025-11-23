package com.example.payflow.ingredientorder.domain;

public enum IngredientOrderStatus {
    PENDING,      // 발주 대기
    CONFIRMED,    // 유통사 확인 완료
    REJECTED,     // 유통사 거절
    COMPLETED,    // 발주 완료
    CANCELLED     // 발주 취소
}
