package com.example.payflow.stage.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class StageSettlementTest {

    @Test
    @DisplayName("정산 생성 성공")
    void createSettlement() {
        // given
        Stage stage = new Stage(
                "테스트 계",
                3,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );

        BigDecimal totalPayments = BigDecimal.valueOf(300000);
        BigDecimal totalPayouts = BigDecimal.valueOf(315000);

        // when
        StageSettlement settlement = new StageSettlement(stage, totalPayments, totalPayouts);

        // then
        assertThat(settlement.getTotalPayments()).isEqualByComparingTo(totalPayments);
        assertThat(settlement.getTotalPayouts()).isEqualByComparingTo(totalPayouts);
        assertThat(settlement.getTotalInterest()).isEqualByComparingTo(BigDecimal.valueOf(15000));
        assertThat(settlement.getIsVerified()).isFalse();
    }

    @Test
    @DisplayName("평균 수익률 계산")
    void calculateAverageReturn() {
        // given
        Stage stage = new Stage(
                "테스트 계",
                3,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );

        BigDecimal totalPayments = BigDecimal.valueOf(300000);
        BigDecimal totalPayouts = BigDecimal.valueOf(315000);

        StageSettlement settlement = new StageSettlement(stage, totalPayments, totalPayouts);

        // when
        BigDecimal averageReturn = settlement.getAverageReturn();

        // then
        // (315000 - 300000) / 300000 * 100 = 5%
        assertThat(averageReturn).isEqualByComparingTo(BigDecimal.valueOf(5.0));
    }
}
