package com.example.payflow.stage.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.stage.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StageServiceTest {

    @Mock
    private StageRepository stageRepository;

    @Mock
    private StageParticipantRepository participantRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private StageService stageService;

    private Stage stage;

    @BeforeEach
    void setUp() {
        stage = new Stage(
                "테스트 계",
                3,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );
    }

    @Test
    @DisplayName("스테이지 생성 성공")
    void createStage() {
        // given
        when(stageRepository.save(any(Stage.class))).thenReturn(stage);

        // when
        Stage created = stageService.createStage(
                "테스트 계",
                3,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );

        // then
        assertThat(created.getName()).isEqualTo("테스트 계");
        assertThat(created.getStatus()).isEqualTo(StageStatus.RECRUITING);
        verify(stageRepository, times(1)).save(any(Stage.class));
    }

    @Test
    @DisplayName("스테이지 참여 성공")
    void joinStage() {
        // given
        stage.getParticipants().clear(); // Mock 초기화
        when(stageRepository.findByIdWithParticipants(1L)).thenReturn(Optional.of(stage));
        when(participantRepository.existsByStageIdAndUsername(1L, "user1")).thenReturn(false);
        when(participantRepository.existsByStageIdAndTurnNumber(1L, 1)).thenReturn(false);
        when(participantRepository.save(any(StageParticipant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        StageParticipant participant = stageService.joinStage(1L, "user1", 1);

        // then
        assertThat(participant.getUsername()).isEqualTo("user1");
        assertThat(participant.getTurnNumber()).isEqualTo(1);
        verify(participantRepository, times(1)).save(any(StageParticipant.class));
    }

    @Test
    @DisplayName("이미 참여한 사용자는 재참여 불가")
    void joinStageAlreadyJoined() {
        // given
        when(stageRepository.findByIdWithParticipants(1L)).thenReturn(Optional.of(stage));
        when(participantRepository.existsByStageIdAndUsername(1L, "user1")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> stageService.joinStage(1L, "user1", 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 참여한 스테이지입니다");
    }

    @Test
    @DisplayName("이미 선택된 순번은 선택 불가")
    void joinStageWithTakenTurn() {
        // given
        when(stageRepository.findByIdWithParticipants(1L)).thenReturn(Optional.of(stage));
        when(participantRepository.existsByStageIdAndUsername(1L, "user1")).thenReturn(false);
        when(participantRepository.existsByStageIdAndTurnNumber(1L, 1)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> stageService.joinStage(1L, "user1", 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 선택된 순번입니다");
    }

    @Test
    @DisplayName("스테이지 시작 성공")
    void startStage() {
        // given
        stage.getParticipants().clear();
        stage.addParticipant(new StageParticipant(stage, "user1", 1));
        stage.addParticipant(new StageParticipant(stage, "user2", 2));
        stage.addParticipant(new StageParticipant(stage, "user3", 3));
        
        when(stageRepository.findByIdWithParticipants(1L)).thenReturn(Optional.of(stage));
        when(stageRepository.save(any(Stage.class))).thenReturn(stage);

        // when
        stageService.startStage(1L);

        // then
        assertThat(stage.getStatus()).isEqualTo(StageStatus.ACTIVE);
        verify(eventPublisher, times(1)).publish(any());
    }
}
