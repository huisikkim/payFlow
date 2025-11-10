package com.example.payflow.stage.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.logging.application.EventLoggingService;
import com.example.payflow.stage.domain.*;
import com.example.payflow.stage.domain.event.StageCompletedEvent;
import com.example.payflow.stage.domain.event.StageStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StageService {

    private final StageRepository stageRepository;
    private final StageParticipantRepository participantRepository;
    private final EventPublisher eventPublisher;
    private final EventLoggingService eventLoggingService;

    @Transactional
    public Stage createStage(String name, Integer totalParticipants, BigDecimal monthlyPayment,
                            BigDecimal interestRate, Integer paymentDay) {
        Stage stage = new Stage(name, totalParticipants, monthlyPayment, interestRate, paymentDay);
        Stage savedStage = stageRepository.save(stage);
        
        log.info("스테이지 생성: id={}, name={}, participants={}", 
                savedStage.getId(), savedStage.getName(), savedStage.getTotalParticipants());
        
        return savedStage;
    }

    @Transactional
    public StageParticipant joinStage(Long stageId, String username, Integer turnNumber) {
        Stage stage = stageRepository.findByIdWithParticipants(stageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스테이지입니다."));

        // 이미 참여했는지 확인
        if (participantRepository.existsByStageIdAndUsername(stageId, username)) {
            throw new IllegalStateException("이미 참여한 스테이지입니다.");
        }

        // 순번이 이미 선택되었는지 확인
        if (participantRepository.existsByStageIdAndTurnNumber(stageId, turnNumber)) {
            throw new IllegalStateException("이미 선택된 순번입니다.");
        }

        StageParticipant participant = new StageParticipant(stage, username, turnNumber);
        stage.addParticipant(participant);
        
        StageParticipant savedParticipant = participantRepository.save(participant);
        
        log.info("스테이지 참여: stageId={}, username={}, turnNumber={}", 
                stageId, username, turnNumber);
        
        return savedParticipant;
    }

    @Transactional
    public void startStage(Long stageId) {
        Stage stage = stageRepository.findByIdWithParticipants(stageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스테이지입니다."));

        stage.start();
        stageRepository.save(stage);

        // 스테이지 시작 이벤트 발행
        StageStartedEvent event = new StageStartedEvent(
                stage.getId(),
                stage.getName(),
                stage.getTotalParticipants(),
                stage.getStartDate(),
                stage.getPaymentDay()
        );
        eventPublisher.publish(event);
        
        // 이벤트 로그 저장
        eventLoggingService.logEvent(
            "STAGE_" + stageId,
            "StageStarted",
            "stage",
            Map.of(
                "stageId", stageId,
                "stageName", stage.getName(),
                "totalParticipants", stage.getTotalParticipants(),
                "startDate", stage.getStartDate().toString()
            )
        );

        log.info("스테이지 시작: id={}, startDate={}", stageId, stage.getStartDate());
    }

    @Transactional
    public void completeStage(Long stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스테이지입니다."));

        stage.complete();
        stageRepository.save(stage);

        // 스테이지 완료 이벤트 발행
        StageCompletedEvent event = new StageCompletedEvent(
                stage.getId(),
                stage.getName(),
                LocalDate.now()
        );
        eventPublisher.publish(event);
        
        // 이벤트 로그 저장
        eventLoggingService.logEvent(
            "STAGE_" + stageId,
            "StageCompleted",
            "stage",
            Map.of(
                "stageId", stageId,
                "stageName", stage.getName(),
                "completedDate", LocalDate.now().toString()
            )
        );

        log.info("스테이지 종료: id={}", stageId);
        
        // 참고: 정산은 별도로 POST /api/stages/{id}/settlement 호출하여 생성
    }

    public Stage getStage(Long stageId) {
        return stageRepository.findByIdWithParticipants(stageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스테이지입니다."));
    }

    public List<Stage> getStagesByStatus(StageStatus status) {
        if (status == null) {
            return stageRepository.findAll();
        }
        return stageRepository.findByStatus(status);
    }

    public List<StageParticipant> getParticipants(Long stageId) {
        return participantRepository.findByStageId(stageId);
    }

    public List<Stage> getMyStages(String username) {
        List<StageParticipant> myParticipations = participantRepository.findByUsername(username);
        return myParticipations.stream()
                .map(StageParticipant::getStage)
                .distinct()
                .toList();
    }
}
