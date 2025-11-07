package com.example.payflow.stage.application;

import com.example.payflow.stage.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StageSettlementService {

    private final StageRepository stageRepository;
    private final StageParticipantRepository participantRepository;
    private final StagePaymentRepository paymentRepository;
    private final StagePayoutRepository payoutRepository;
    private final StageSettlementRepository settlementRepository;
    private final ParticipantSettlementRepository participantSettlementRepository;

    @Transactional
    public StageSettlement generateSettlement(Long stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스테이지입니다."));

        if (stage.getStatus() != StageStatus.COMPLETED) {
            throw new IllegalStateException("완료된 스테이지만 정산할 수 있습니다.");
        }

        // 이미 정산이 생성되었는지 확인
        if (settlementRepository.existsByStageId(stageId)) {
            log.warn("이미 정산이 생성됨: stageId={}", stageId);
            return settlementRepository.findByStageId(stageId)
                    .orElseThrow(() -> new IllegalStateException("정산 조회 실패"));
        }

        // 총 납입액 계산
        List<StagePayment> allPayments = paymentRepository.findByStageId(stageId);
        BigDecimal totalPayments = allPayments.stream()
                .filter(StagePayment::getIsPaid)
                .map(StagePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 총 지급액 계산
        List<StagePayout> allPayouts = payoutRepository.findByStageId(stageId);
        BigDecimal totalPayouts = allPayouts.stream()
                .filter(StagePayout::getIsCompleted)
                .map(StagePayout::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 정산 생성
        StageSettlement settlement = new StageSettlement(stage, totalPayments, totalPayouts);
        settlementRepository.save(settlement);

        // 참여자별 정산 생성
        List<StageParticipant> participants = participantRepository.findByStageId(stageId);
        for (StageParticipant participant : participants) {
            createParticipantSettlement(settlement, participant, stage);
        }

        // 정산 검증
        settlement.verify();
        settlementRepository.save(settlement);

        log.info("정산 생성 완료: stageId={}, totalPayments={}, totalPayouts={}, interest={}", 
                stageId, totalPayments, totalPayouts, settlement.getTotalInterest());

        return settlement;
    }

    private void createParticipantSettlement(StageSettlement settlement, StageParticipant participant, Stage stage) {
        String username = participant.getUsername();
        Integer turnNumber = participant.getTurnNumber();

        // 참여자의 총 납입액 계산
        List<StagePayment> userPayments = paymentRepository.findByStageIdAndUsername(
                stage.getId(), username);
        BigDecimal totalPaid = userPayments.stream()
                .filter(StagePayment::getIsPaid)
                .map(StagePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 참여자의 수령액 계산
        StagePayout payout = payoutRepository.findByStageIdAndTurnNumber(stage.getId(), turnNumber)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("약정금을 찾을 수 없음: username=%s, turn=%d", username, turnNumber)));
        
        BigDecimal totalReceived = payout.getAmount();

        // 납입 개월 수 (전체 개월)
        Integer paidMonths = stage.getTotalParticipants();

        // 수령 월차 (순번)
        Integer receivedMonth = turnNumber;

        ParticipantSettlement participantSettlement = new ParticipantSettlement(
                settlement,
                username,
                turnNumber,
                totalPaid,
                totalReceived,
                paidMonths,
                receivedMonth
        );

        participantSettlementRepository.save(participantSettlement);
        settlement.addParticipantSettlement(participantSettlement);

        log.info("참여자 정산 생성: username={}, paid={}, received={}, profit={}", 
                username, totalPaid, totalReceived, participantSettlement.getProfitLoss());
    }

    public StageSettlement getSettlement(Long stageId) {
        return settlementRepository.findByStageIdWithParticipants(stageId)
                .orElseThrow(() -> new IllegalArgumentException("정산 내역이 없습니다."));
    }

    public List<ParticipantSettlement> getParticipantSettlements(Long stageId) {
        StageSettlement settlement = settlementRepository.findByStageId(stageId)
                .orElseThrow(() -> new IllegalArgumentException("정산 내역이 없습니다."));
        return participantSettlementRepository.findBySettlementId(settlement.getId());
    }

    public ParticipantSettlement getMySettlement(Long stageId, String username) {
        return participantSettlementRepository.findByStageIdAndUsername(stageId, username)
                .orElseThrow(() -> new IllegalArgumentException("정산 내역이 없습니다."));
    }

    public List<ParticipantSettlement> getMyAllSettlements(String username) {
        return participantSettlementRepository.findByUsername(username);
    }
}
