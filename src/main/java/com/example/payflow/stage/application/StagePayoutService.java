package com.example.payflow.stage.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.stage.domain.*;
import com.example.payflow.stage.domain.event.PayoutReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StagePayoutService {

    private final StageRepository stageRepository;
    private final StageParticipantRepository participantRepository;
    private final StagePayoutRepository payoutRepository;
    private final StagePaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;
    private final StageService stageService;

    @Transactional
    public void generatePayout(Long stageId, Integer turnNumber) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스테이지입니다."));

        if (stage.getStatus() != StageStatus.ACTIVE) {
            throw new IllegalStateException("진행 중인 스테이지만 약정금을 생성할 수 있습니다.");
        }

        // 해당 순번의 참여자 찾기
        StageParticipant participant = participantRepository.findByStageIdAndTurnNumber(stageId, turnNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 순번의 참여자가 없습니다."));

        // 이미 생성된 약정금이 있는지 확인
        if (payoutRepository.findByStageIdAndTurnNumber(stageId, turnNumber).isPresent()) {
            log.warn("이미 생성된 약정금: stageId={}, turnNumber={}", stageId, turnNumber);
            return;
        }

        // 해당 월의 모든 참여자가 납입했는지 확인
        Long paidCount = paymentRepository.countPaidPaymentsByStageAndMonth(stageId, turnNumber);
        if (paidCount < stage.getTotalParticipants()) {
            log.warn("모든 참여자가 납입하지 않음: stageId={}, month={}, paid={}/{}", 
                    stageId, turnNumber, paidCount, stage.getTotalParticipants());
            return;
        }

        // 약정금 계산
        BigDecimal payoutAmount = stage.calculatePayoutAmount(turnNumber);
        LocalDateTime scheduledAt = calculatePayoutDate(stage.getStartDate(), stage.getPaymentDay(), turnNumber);

        StagePayout payout = new StagePayout(
                stage,
                participant.getUsername(),
                turnNumber,
                payoutAmount,
                scheduledAt
        );

        payoutRepository.save(payout);

        // 약정금 준비 이벤트 발행
        PayoutReadyEvent event = new PayoutReadyEvent(
                stageId,
                participant.getUsername(),
                turnNumber,
                payoutAmount
        );
        eventPublisher.publish(event);

        log.info("약정금 생성: stageId={}, username={}, turn={}, amount={}", 
                stageId, participant.getUsername(), turnNumber, payoutAmount);
    }

    @Transactional
    public void completePayout(Long payoutId, String transactionId) {
        StagePayout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약정금입니다."));

        payout.complete(transactionId);
        payoutRepository.save(payout);

        // 참여자의 수령 상태 업데이트
        StageParticipant participant = participantRepository
                .findByStageIdAndTurnNumber(payout.getStage().getId(), payout.getTurnNumber())
                .orElseThrow(() -> new IllegalArgumentException("참여자를 찾을 수 없습니다."));
        
        participant.markPayoutReceived();
        participantRepository.save(participant);

        log.info("약정금 지급 완료: payoutId={}, username={}, turn={}", 
                payoutId, payout.getUsername(), payout.getTurnNumber());

        // 모든 참여자가 수령했는지 확인
        checkAndCompleteStage(payout.getStage().getId());
    }

    private void checkAndCompleteStage(Long stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스테이지입니다."));

        Long completedPayouts = payoutRepository.countCompletedPayoutsByStage(stageId);
        
        if (completedPayouts >= stage.getTotalParticipants()) {
            stageService.completeStage(stageId);
            log.info("스테이지 자동 종료: stageId={}", stageId);
        }
    }

    public List<StagePayout> getPayoutsByStage(Long stageId) {
        return payoutRepository.findByStageId(stageId);
    }

    public List<StagePayout> getMyPayouts(String username) {
        return payoutRepository.findByUsername(username);
    }

    private LocalDateTime calculatePayoutDate(LocalDate startDate, Integer paymentDay, Integer monthNumber) {
        LocalDate payoutDate = startDate.plusMonths(monthNumber - 1).withDayOfMonth(paymentDay).plusDays(1);
        return LocalDateTime.of(payoutDate, LocalTime.of(9, 0, 0));
    }
}
