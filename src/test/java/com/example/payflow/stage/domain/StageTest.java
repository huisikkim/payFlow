package com.example.payflow.stage.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class StageTest {

    @Test
    @DisplayName("스테이지 생성 성공")
    void createStage() {
        // given & when
        Stage stage = new Stage(
                "테스트 계",
                5,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );

        // then
        assertThat(stage.getName()).isEqualTo("테스트 계");
        assertThat(stage.getTotalParticipants()).isEqualTo(5);
        assertThat(stage.getMonthlyPayment()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(stage.getStatus()).isEqualTo(StageStatus.RECRUITING);
    }

    @Test
    @DisplayName("참여 인원이 2명 미만이면 예외 발생")
    void createStageWithInvalidParticipants() {
        // when & then
        assertThatThrownBy(() -> new Stage(
                "테스트 계",
                1,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("참여 인원은 최소 2명 이상이어야 합니다");
    }

    @Test
    @DisplayName("약정금 계산 - 이율 적용")
    void calculatePayoutAmount() {
        // given
        Stage stage = new Stage(
                "테스트 계",
                5,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );

        // when
        BigDecimal firstPayout = stage.calculatePayoutAmount(1);
        BigDecimal thirdPayout = stage.calculatePayoutAmount(3);
        BigDecimal fifthPayout = stage.calculatePayoutAmount(5);

        // then
        assertThat(firstPayout).isEqualByComparingTo(BigDecimal.valueOf(500000)); // 100000 * 5 * (1 + 0.05 * 0)
        assertThat(thirdPayout).isEqualByComparingTo(BigDecimal.valueOf(550000)); // 100000 * 5 * (1 + 0.05 * 2)
        assertThat(fifthPayout).isEqualByComparingTo(BigDecimal.valueOf(600000)); // 100000 * 5 * (1 + 0.05 * 4)
    }

    @Test
    @DisplayName("스테이지 시작 성공")
    void startStage() {
        // given
        Stage stage = new Stage(
                "테스트 계",
                2,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );
        
        StageParticipant p1 = new StageParticipant(stage, "user1", 1);
        StageParticipant p2 = new StageParticipant(stage, "user2", 2);
        stage.addParticipant(p1);
        stage.addParticipant(p2);

        // when
        stage.start();

        // then
        assertThat(stage.getStatus()).isEqualTo(StageStatus.ACTIVE);
        assertThat(stage.getStartDate()).isNotNull();
        assertThat(stage.getExpectedEndDate()).isNotNull();
    }

    @Test
    @DisplayName("참여자가 모두 모집되지 않으면 시작 불가")
    void startStageWithoutFullParticipants() {
        // given
        Stage stage = new Stage(
                "테스트 계",
                3,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );
        
        StageParticipant p1 = new StageParticipant(stage, "user1", 1);
        stage.addParticipant(p1);

        // when & then
        assertThatThrownBy(() -> stage.start())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("모든 참여자가 모집되어야 시작할 수 있습니다");
    }

    @Test
    @DisplayName("스테이지 종료 성공")
    void completeStage() {
        // given
        Stage stage = new Stage(
                "테스트 계",
                2,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );
        
        StageParticipant p1 = new StageParticipant(stage, "user1", 1);
        StageParticipant p2 = new StageParticipant(stage, "user2", 2);
        stage.addParticipant(p1);
        stage.addParticipant(p2);
        stage.start();

        // when
        stage.complete();

        // then
        assertThat(stage.getStatus()).isEqualTo(StageStatus.COMPLETED);
    }
}
