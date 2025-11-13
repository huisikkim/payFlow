package com.example.payflow.escrow.infrastructure;

import com.example.payflow.escrow.application.EscrowService;
import com.example.payflow.escrow.application.SettlementService;
import com.example.payflow.escrow.application.dto.EscrowResponse;
import com.example.payflow.escrow.domain.EscrowStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 에스크로 스케줄러
 * 주기적으로 에스크로 거래를 모니터링하고 자동 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EscrowScheduler {
    
    private final EscrowService escrowService;
    private final SettlementService settlementService;
    
    // 타임아웃 설정 (일 단위)
    private static final int INITIATED_TIMEOUT_DAYS = 7;  // 거래 시작 후 7일
    private static final int DEPOSITED_TIMEOUT_DAYS = 30; // 입금 후 30일
    private static final int DELIVERED_TIMEOUT_DAYS = 14; // 인도 후 14일
    
    /**
     * 타임아웃 거래 자동 취소
     * 매일 자정에 실행
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cancelTimeoutTransactions() {
        log.info("⏰ 타임아웃 거래 자동 취소 작업 시작");
        
        try {
            int cancelledCount = 0;
            
            // INITIATED 상태에서 오래 대기 중인 거래 취소
            List<EscrowResponse> initiatedTransactions = escrowService.getEscrowsByStatus(EscrowStatus.INITIATED);
            for (EscrowResponse transaction : initiatedTransactions) {
                if (isTimeout(transaction.getCreatedAt(), INITIATED_TIMEOUT_DAYS)) {
                    try {
                        escrowService.cancelEscrow(
                            transaction.getTransactionId(),
                            "자동 취소: 입금 기한 초과 (" + INITIATED_TIMEOUT_DAYS + "일)"
                        );
                        cancelledCount++;
                        log.info("타임아웃 거래 취소: transactionId={}, status=INITIATED", 
                            transaction.getTransactionId());
                    } catch (Exception e) {
                        log.error("거래 취소 실패: transactionId={}", transaction.getTransactionId(), e);
                    }
                }
            }
            
            // DEPOSITED 상태에서 오래 대기 중인 거래 취소
            List<EscrowResponse> depositedTransactions = escrowService.getEscrowsByStatus(EscrowStatus.DEPOSITED);
            for (EscrowResponse transaction : depositedTransactions) {
                if (isTimeout(transaction.getUpdatedAt(), DEPOSITED_TIMEOUT_DAYS)) {
                    try {
                        escrowService.cancelEscrow(
                            transaction.getTransactionId(),
                            "자동 취소: 차량 인도 기한 초과 (" + DEPOSITED_TIMEOUT_DAYS + "일)"
                        );
                        cancelledCount++;
                        log.info("타임아웃 거래 취소: transactionId={}, status=DEPOSITED", 
                            transaction.getTransactionId());
                    } catch (Exception e) {
                        log.error("거래 취소 실패: transactionId={}", transaction.getTransactionId(), e);
                    }
                }
            }
            
            // DELIVERED 상태에서 오래 대기 중인 거래 취소
            List<EscrowResponse> deliveredTransactions = escrowService.getEscrowsByStatus(EscrowStatus.DELIVERED);
            for (EscrowResponse transaction : deliveredTransactions) {
                if (isTimeout(transaction.getUpdatedAt(), DELIVERED_TIMEOUT_DAYS)) {
                    try {
                        escrowService.cancelEscrow(
                            transaction.getTransactionId(),
                            "자동 취소: 검증 기한 초과 (" + DELIVERED_TIMEOUT_DAYS + "일)"
                        );
                        cancelledCount++;
                        log.info("타임아웃 거래 취소: transactionId={}, status=DELIVERED", 
                            transaction.getTransactionId());
                    } catch (Exception e) {
                        log.error("거래 취소 실패: transactionId={}", transaction.getTransactionId(), e);
                    }
                }
            }
            
            log.info("✅ 타임아웃 거래 자동 취소 작업 완료: 취소된 거래 수={}", cancelledCount);
            
        } catch (Exception e) {
            log.error("타임아웃 거래 자동 취소 작업 실패", e);
        }
    }
    
    /**
     * 정산 대기 거래 자동 처리
     * 매 시간마다 실행
     */
    @Scheduled(cron = "0 0 * * * *")
    public void processSettlementReadyTransactions() {
        log.info("⏰ 정산 대기 거래 자동 처리 작업 시작");
        
        try {
            int processedCount = 0;
            
            // OWNERSHIP_TRANSFERRED 상태의 거래를 찾아서 정산 시작
            List<EscrowResponse> readyTransactions = escrowService.getEscrowsByStatus(
                EscrowStatus.OWNERSHIP_TRANSFERRED);
            
            for (EscrowResponse transaction : readyTransactions) {
                try {
                    // 정산 시작
                    settlementService.startSettlement(transaction.getTransactionId());
                    processedCount++;
                    log.info("정산 시작: transactionId={}", transaction.getTransactionId());
                    
                    // 실제 환경에서는 외부 결제 시스템과 연동하여 정산 완료 처리
                    // 여기서는 자동으로 완료 처리하지 않고, 관리자가 수동으로 완료 처리하도록 함
                    
                } catch (Exception e) {
                    log.error("정산 시작 실패: transactionId={}", transaction.getTransactionId(), e);
                }
            }
            
            log.info("✅ 정산 대기 거래 자동 처리 작업 완료: 처리된 거래 수={}", processedCount);
            
        } catch (Exception e) {
            log.error("정산 대기 거래 자동 처리 작업 실패", e);
        }
    }
    
    /**
     * 에스크로 거래 상태 모니터링
     * 매 30분마다 실행
     */
    @Scheduled(cron = "0 */30 * * * *")
    public void monitorEscrowTransactions() {
        log.info("⏰ 에스크로 거래 상태 모니터링 시작");
        
        try {
            // 각 상태별 거래 수 집계
            int initiatedCount = escrowService.getEscrowsByStatus(EscrowStatus.INITIATED).size();
            int depositedCount = escrowService.getEscrowsByStatus(EscrowStatus.DEPOSITED).size();
            int deliveredCount = escrowService.getEscrowsByStatus(EscrowStatus.DELIVERED).size();
            int verifiedCount = escrowService.getEscrowsByStatus(EscrowStatus.VERIFIED).size();
            int ownershipTransferredCount = escrowService.getEscrowsByStatus(EscrowStatus.OWNERSHIP_TRANSFERRED).size();
            int settlingCount = escrowService.getEscrowsByStatus(EscrowStatus.SETTLING).size();
            int completedCount = escrowService.getEscrowsByStatus(EscrowStatus.COMPLETED).size();
            int disputedCount = escrowService.getEscrowsByStatus(EscrowStatus.DISPUTED).size();
            
            log.info("📊 에스크로 거래 현황: INITIATED={}, DEPOSITED={}, DELIVERED={}, VERIFIED={}, " +
                    "OWNERSHIP_TRANSFERRED={}, SETTLING={}, COMPLETED={}, DISPUTED={}",
                initiatedCount, depositedCount, deliveredCount, verifiedCount,
                ownershipTransferredCount, settlingCount, completedCount, disputedCount);
            
            // 분쟁 중인 거래가 있으면 경고
            if (disputedCount > 0) {
                log.warn("⚠️ 분쟁 중인 거래가 {}건 있습니다. 관리자 확인이 필요합니다.", disputedCount);
            }
            
            // 정산 대기 중인 거래가 있으면 알림
            if (ownershipTransferredCount > 0) {
                log.info("💰 정산 대기 중인 거래가 {}건 있습니다.", ownershipTransferredCount);
            }
            
        } catch (Exception e) {
            log.error("에스크로 거래 상태 모니터링 실패", e);
        }
    }
    
    /**
     * 타임아웃 여부 확인
     */
    private boolean isTimeout(LocalDateTime dateTime, int timeoutDays) {
        if (dateTime == null) {
            return false;
        }
        LocalDateTime timeoutDate = dateTime.plusDays(timeoutDays);
        return LocalDateTime.now().isAfter(timeoutDate);
    }
}
