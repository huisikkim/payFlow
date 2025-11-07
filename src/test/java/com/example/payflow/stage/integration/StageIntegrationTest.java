package com.example.payflow.stage.integration;

import com.example.payflow.stage.application.StageService;
import com.example.payflow.stage.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class StageIntegrationTest {

    @Autowired
    private StageService stageService;

    @Autowired
    private StageRepository stageRepository;

    @Autowired
    private StageParticipantRepository participantRepository;

    @Test
    @DisplayName("스테이지 생성부터 시작까지 통합 테스트")
    void stageLifecycle() {
        // 1. 스테이지 생성
        Stage stage = stageService.createStage(
                "통합 테스트 계",
                3,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );

        assertThat(stage.getId()).isNotNull();
        assertThat(stage.getStatus()).isEqualTo(StageStatus.RECRUITING);

        // 2. 참여자 모집
        StageParticipant p1 = stageService.joinStage(stage.getId(), "user1", 1);
        StageParticipant p2 = stageService.joinStage(stage.getId(), "user2", 2);
        StageParticipant p3 = stageService.joinStage(stage.getId(), "user3", 3);

        assertThat(p1.getTurnNumber()).isEqualTo(1);
        assertThat(p2.getTurnNumber()).isEqualTo(2);
        assertThat(p3.getTurnNumber()).isEqualTo(3);

        // 3. 참여자 확인
        List<StageParticipant> participants = stageService.getParticipants(stage.getId());
        assertThat(participants).hasSize(3);

        // 4. 스테이지 시작
        stageService.startStage(stage.getId());

        Stage startedStage = stageService.getStage(stage.getId());
        assertThat(startedStage.getStatus()).isEqualTo(StageStatus.ACTIVE);
        assertThat(startedStage.getStartDate()).isNotNull();
    }

    @Test
    @DisplayName("중복 순번 선택 방지")
    void preventDuplicateTurnNumber() {
        // given
        Stage stage = stageService.createStage(
                "중복 방지 테스트",
                3,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );

        stageService.joinStage(stage.getId(), "user1", 1);

        // when & then
        assertThatThrownBy(() -> stageService.joinStage(stage.getId(), "user2", 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 선택된 순번입니다");
    }

    @Test
    @DisplayName("중복 참여 방지")
    void preventDuplicateParticipation() {
        // given
        Stage stage = stageService.createStage(
                "중복 참여 방지 테스트",
                3,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );

        stageService.joinStage(stage.getId(), "user1", 1);

        // when & then
        assertThatThrownBy(() -> stageService.joinStage(stage.getId(), "user1", 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 참여한 스테이지입니다");
    }
}
