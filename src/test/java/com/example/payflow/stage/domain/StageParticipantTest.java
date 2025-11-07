package com.example.payflow.stage.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class StageParticipantTest {

    @Test
    @DisplayName("참여자 생성 성공")
    void createParticipant() {
        // given
        Stage stage = new Stage(
                "테스트 계",
                5,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );

        // when
        StageParticipant participant = new StageParticipant(stage, "user1", 3);

        // then
        assertThat(participant.getUsername()).isEqualTo("user1");
        assertThat(participant.getTurnNumber()).isEqualTo(3);
        assertThat(participant.getHasReceivedPayout()).isFalse();
    }

    @Test
    @DisplayName("순번이 범위를 벗어나면 예외 발생")
    void createParticipantWithInvalidTurn() {
        // given
        Stage stage = new Stage(
                "테스트 계",
                5,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );

        // when & then
        assertThatThrownBy(() -> new StageParticipant(stage, "user1", 6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("순번은 1부터 5 사이여야 합니다");
    }

    @Test
    @DisplayName("약정금 수령 처리")
    void markPayoutReceived() {
        // given
        Stage stage = new Stage(
                "테스트 계",
                5,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );
        StageParticipant participant = new StageParticipant(stage, "user1", 3);

        // when
        participant.markPayoutReceived();

        // then
        assertThat(participant.getHasReceivedPayout()).isTrue();
        assertThat(participant.getPayoutReceivedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 수령한 약정금은 다시 수령 불가")
    void markPayoutReceivedTwice() {
        // given
        Stage stage = new Stage(
                "테스트 계",
                5,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );
        StageParticipant participant = new StageParticipant(stage, "user1", 3);
        participant.markPayoutReceived();

        // when & then
        assertThatThrownBy(() -> participant.markPayoutReceived())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 약정금을 수령했습니다");
    }
}
