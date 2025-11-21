package com.example.payflow.crypto.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * RSI (Relative Strength Index) 계산기
 * RSI = 100 - (100 / (1 + RS))
 * RS = 평균 상승폭 / 평균 하락폭
 */
public class RSICalculator {
    
    private static final int DEFAULT_PERIOD = 14;
    
    /**
     * RSI 계산 (기본 14 기간)
     * @param closePrices 종가 리스트 (최소 15개 필요 - 14개 기간 + 현재가)
     * @return RSI 값 (0-100)
     */
    public static BigDecimal calculate(List<BigDecimal> closePrices) {
        return calculate(closePrices, DEFAULT_PERIOD);
    }
    
    /**
     * RSI 계산
     * @param closePrices 종가 리스트
     * @param period RSI 기간 (일반적으로 14)
     * @return RSI 값 (0-100)
     */
    public static BigDecimal calculate(List<BigDecimal> closePrices, int period) {
        if (closePrices == null || closePrices.size() < period + 1) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;
        
        // 첫 번째 기간의 평균 계산
        for (int i = 1; i <= period; i++) {
            BigDecimal change = closePrices.get(i).subtract(closePrices.get(i - 1));
            
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                avgGain = avgGain.add(change);
            } else {
                avgLoss = avgLoss.add(change.abs());
            }
        }
        
        avgGain = avgGain.divide(BigDecimal.valueOf(period), 10, RoundingMode.HALF_UP);
        avgLoss = avgLoss.divide(BigDecimal.valueOf(period), 10, RoundingMode.HALF_UP);
        
        // 이후 기간들에 대해 smoothing 적용
        for (int i = period + 1; i < closePrices.size(); i++) {
            BigDecimal change = closePrices.get(i).subtract(closePrices.get(i - 1));
            
            BigDecimal currentGain = change.compareTo(BigDecimal.ZERO) > 0 ? change : BigDecimal.ZERO;
            BigDecimal currentLoss = change.compareTo(BigDecimal.ZERO) < 0 ? change.abs() : BigDecimal.ZERO;
            
            avgGain = avgGain.multiply(BigDecimal.valueOf(period - 1))
                    .add(currentGain)
                    .divide(BigDecimal.valueOf(period), 10, RoundingMode.HALF_UP);
            
            avgLoss = avgLoss.multiply(BigDecimal.valueOf(period - 1))
                    .add(currentLoss)
                    .divide(BigDecimal.valueOf(period), 10, RoundingMode.HALF_UP);
        }
        
        // RSI 계산
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        
        BigDecimal rs = avgGain.divide(avgLoss, 10, RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100)
                .subtract(BigDecimal.valueOf(100)
                        .divide(BigDecimal.ONE.add(rs), 2, RoundingMode.HALF_UP));
        
        return rsi;
    }
    
    /**
     * RSI 상태 판단
     */
    public static String getRSIStatus(BigDecimal rsi) {
        if (rsi.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return "OVERBOUGHT"; // 과매수
        } else if (rsi.compareTo(BigDecimal.valueOf(30)) <= 0) {
            return "OVERSOLD"; // 과매도
        } else {
            return "NEUTRAL"; // 중립
        }
    }
}
