package com.example.payflow.pricelearning.domain;

public enum PriceAlertType {
    MODERATE_SURGE,  // 중간 급등 (20-50%)
    HIGH_SURGE,      // 높은 급등 (50-100%)
    EXTREME_SURGE    // 극심한 급등 (100% 이상)
}
