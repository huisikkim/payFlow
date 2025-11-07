package com.example.payflow.stage.infrastructure;

import com.example.payflow.stage.application.StagePaymentService;
import com.example.payflow.stage.application.StagePayoutService;
import com.example.payflow.stage.domain.Stage;
import com.example.payflow.stage.domain.StageRepository;
import com.example.payflow.stage.domain.StageStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StageScheduler {

    private final StageRepository stageRepository;
    private final StagePaymentService paymentService;
    private final StagePayoutService payoutService;

    /**
     * 매일 자정에 실행되어 당일이 결제일인 스테이지들의 월별 결제를 생성
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00:00
    public void generateDailyPayments() {
        log.info("일일 결제 생성 스케줄러 시작");
        
        int today = LocalDate.now().getDayOfMonth();
        List<Stage> activeStages = stageRepository.findByStatusAndPaymentDay(StageStatus.ACTIVE, today);

        for (Stage stage : activeStages) {
            try {
                Integer currentMonth = calculateCurrentMonth(stage);
                if (currentMonth <= stage.getTotalParticipants()) {
                    paymentService.generateMonthlyPayments(stage.getId(), currentMonth);
                    log.info("결제 생성 완료: stageId={}, month={}", stage.getId(), currentMonth);
                }
            } catch (Exception e) {
                log.error("결제 생성 실패: stageId={}, error={}", stage.getId(), e.getMessage(), e);
            }
        }
        
        log.info("일일 결제 생성 스케줄러 종료");
    }

    /**
     * 매일 오전 9시에 실행되어 약정금 지급 대상을 확인하고 생성
     */
    @Scheduled(cron = "0 0 9 * * *") // 매일 09:00:00
    public void generateDailyPayouts() {
        log.info("일일 약정금 생성 스케줄러 시작");
        
        int today = LocalDate.now().getDayOfMonth();
        List<Stage> activeStages = stageRepository.findByStatusAndPaymentDay(StageStatus.ACTIVE, today);

        for (Stage stage : activeStages) {
            try {
                Integer currentMonth = calculateCurrentMonth(stage);
                if (currentMonth > 1 && currentMonth <= stage.getTotalParticipants()) {
                    // 전월 납입이 완료되었으면 약정금 생성
                    payoutService.generatePayout(stage.getId(), currentMonth - 1);
                    log.info("약정금 생성 완료: stageId={}, turn={}", stage.getId(), currentMonth - 1);
                }
            } catch (Exception e) {
                log.error("약정금 생성 실패: stageId={}, error={}", stage.getId(), e.getMessage(), e);
            }
        }
        
        log.info("일일 약정금 생성 스케줄러 종료");
    }

    /**
     * 스테이지 시작일로부터 현재가 몇 번째 달인지 계산
     */
    private Integer calculateCurrentMonth(Stage stage) {
        LocalDate startDate = stage.getStartDate();
        LocalDate now = LocalDate.now();
        long monthsBetween = ChronoUnit.MONTHS.between(startDate, now);
        return (int) monthsBetween + 1;
    }
}
