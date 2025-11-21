package com.example.payflow.crypto.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 거래량 급증률 계산기
 * 현재 거래량 / 평균 거래량 × 100
 */
public class VolumeSurgeCalculator {
    
    /**
     * 거래량 급증률 계산
     * @param volumes 거래량 리스트 (최근 데이터가 마지막)
     * @param currentVolume 현재 거래량
     * @return 급증률 (%) - 100이 기준, 200이면 2배
     */
    public static BigDecimal calculate(List<BigDecimal> volumes, BigDecimal currentVolume) {
        if (volumes == null || volumes.isEmpty() || currentVolume == null) {
            return BigDecimal.valueOf(100); // 기준값
        }
        
        // 평균 거래량 계산
        BigDecimal sum = volumes.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgVolume = sum.divide(
                BigDecimal.valueOf(volumes.size()), 
                10, 
                RoundingMode.HALF_UP
        );
        
        // 평균이 0이면 기준값 반환
        if (avgVolume.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        
        // 급증률 계산: (현재 / 평균) × 100
        BigDecimal surge = currentVolume
                .divide(avgVolume, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return surge;
    }
    
    /**
     * 거래량 상태 판단
     */
    public static String getVolumeStatus(BigDecimal surge) {
        if (surge.compareTo(BigDecimal.valueOf(200)) >= 0) {
            return "SURGE"; // 급증 (2배 이상)
        } else if (surge.compareTo(BigDecimal.valueOf(50)) <= 0) {
            return "LOW"; // 저조 (50% 이하)
        } else {
            return "NORMAL"; // 보통
        }
    }
    
    /**
     * RSI와 거래량을 조합한 신호 강도 판단
     */
    public static String getSignalStrength(BigDecimal rsi, BigDecimal volumeSurge) {
        boolean isOverbought = rsi.compareTo(BigDecimal.valueOf(70)) >= 0;
        boolean isOversold = rsi.compareTo(BigDecimal.valueOf(30)) <= 0;
        boolean isVolumeSurge = volumeSurge.compareTo(BigDecimal.valueOf(200)) >= 0;
        
        if (isOverbought && isVolumeSurge) {
            return "STRONG_SELL"; // 강력한 매도 신호
        } else if (isOversold && isVolumeSurge) {
            return "STRONG_BUY"; // 강력한 매수 신호
        } else if (isOverbought) {
            return "WEAK_SELL"; // 약한 매도 신호
        } else if (isOversold) {
            return "WEAK_BUY"; // 약한 매수 신호
        } else {
            return "NEUTRAL"; // 중립
        }
    }
}
