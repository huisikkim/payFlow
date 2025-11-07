package com.example.payflow.stage.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class ParticipantSettlementTest {

    @Test
    @DisplayName("참여자 정산 생성 - 이익")
    void createParticipantSettlementWithProfit() {
        // given
        Stage stage = new Stage(
                "테스트 계",
                3,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );

        StageSettlement settlement = new StageSettlement(
                stage,
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(315000)
        );

        BigDecimal totalPaid = BigDecimal.valueOf(300000);
        BigDecimal totalReceived = BigDecimal.valueOf(315000);

        // when
        ParticipantSettlement participantSettlement = new ParticipantSettlement(
                settlement,
                "user1",
                2,
                totalPaid,
                totalReceived,
                3,
                2
        );

        // then
        assertThat(participantSettlement.getProfitLoss()).isEqualByComparingTo(BigDecimal.valueOf(15000));
        assertThat(participantSettlement.getEffectiveRate()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
        assertThat(participantSettlement.isProfitable()).isTrue();
    }

    @Test
    @DisplayName("참여자 정산 생성 - 손익 없음")
    void createParticipantSettlementBreakEven() {
        // given
        Stage stage = new Stage(
                "테스트 계",
                3,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );

        StageSettlement settlement = new StageSettlement(
                stage,
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(300000)
        );

        BigDecimal totalPaid = BigDecimal.valueOf(300000);
        BigDecimal totalReceived = BigDecimal.valueOf(300000);

        // when
        ParticipantSettlement participantSettlement = new ParticipantSettlement(
                settlement,
                "user1",
                1,
                totalPaid,
                totalReceived,
                3,
                1
        );

        // then
        assertThat(participantSettlement.getProfitLoss()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(participantSettlement.getEffectiveRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(participantSettlement.isProfitable()).isFalse();
        assertThat(participantSettlement.isBreakEven()).isTrue();
    }

    @Test
    @DisplayName("실질 이율 계산")
    void calculateEffectiveRate() {
        // given
        Stage stage = new Stage(
                "테스트 계",
                5,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );

        StageSettlement settlement = new StageSettlement(
                stage,
                BigDecimal.valueOf(500000),
                BigDecimal.valueOf(600000)
        );

        // 3번 순번: 100,000 * 5 * (1 + 0.05 * 2) = 550,000
        BigDecimal totalPaid = BigDecimal.valueOf(500000);
        BigDecimal totalReceived = BigDecimal.valueOf(550000);

        // when
        ParticipantSettlement participantSettlement = new ParticipantSettlement(
                settlement,
                "user3",
                3,
                totalPaid,
                totalReceived,
                5,
                3
        );

        // then
        // (550,000 - 500,000) / 500,000 * 100 = 10%
        assertThat(participantSettlement.getEffectiveRate()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
    }
}
