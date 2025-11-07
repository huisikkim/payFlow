package com.example.payflow.stage.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.stage.domain.*;
import com.example.payflow.stage.domain.event.PaymentDueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StagePaymentService {

    private final StageRepository stageRepository;
    private final StageParticipantRepository participantRepository;
    private final StagePaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public void generateMonthlyPayments(Long stageId, Integer monthNumber) {
        Stage stage = stageRepository.findByIdWithParticipants(stageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스테이지입니다."));

        if (stage.getStatus() != StageStatus.ACTIVE) {
            throw new IllegalStateException("진행 중인 스테이지만 결제를 생성할 수 있습니다.");
        }

        List<StageParticipant> participants = participantRepository.findByStageId(stageId);
        LocalDateTime dueDate = calculateDueDate(stage.getStartDate(), stage.getPaymentDay(), monthNumber);

        List<StagePayment> payments = new ArrayList<>();
        for (StageParticipant participant : participants) {
            // 이미 생성된 결제가 있는지 확인
            if (paymentRepository.findByStageIdAndUsernameAndMonthNumber(
                    stageId, participant.getUsername(), monthNumber).isEmpty()) {
                
                StagePayment payment = new StagePayment(
                        stage,
                        participant.getUsername(),
                        monthNumber,
                        stage.getMonthlyPayment(),
                        dueDate
                );
                payments.add(payment);
            }
        }

        if (!payments.isEmpty()) {
            paymentRepository.saveAll(payments);
            log.info("월별 결제 생성: stageId={}, month={}, count={}", stageId, monthNumber, payments.size());

            // 결제 알림 이벤트 발행
            for (StagePayment payment : payments) {
                PaymentDueEvent event = new PaymentDueEvent(
                        stageId,
                        payment.getUsername(),
                        monthNumber,
                        payment.getAmount(),
                        dueDate
                );
                eventPublisher.publish(event);
            }
        }
    }

    @Transactional
    public void processPayment(Long paymentId, String paymentKey) {
        StagePayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다."));

        payment.markAsPaid(paymentKey);
        paymentRepository.save(payment);

        log.info("결제 완료: paymentId={}, username={}, month={}", 
                paymentId, payment.getUsername(), payment.getMonthNumber());
    }

    public List<StagePayment> getPaymentsByStage(Long stageId) {
        return paymentRepository.findByStageId(stageId);
    }

    public List<StagePayment> getMyPayments(String username) {
        return paymentRepository.findByUsername(username);
    }

    public List<StagePayment> getUnpaidPayments(String username) {
        return paymentRepository.findByUsername(username).stream()
                .filter(p -> !p.getIsPaid())
                .toList();
    }

    private LocalDateTime calculateDueDate(LocalDate startDate, Integer paymentDay, Integer monthNumber) {
        LocalDate dueDate = startDate.plusMonths(monthNumber - 1).withDayOfMonth(paymentDay);
        return LocalDateTime.of(dueDate, LocalTime.of(23, 59, 59));
    }
}
