package com.example.payflow.escrow.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.escrow.application.dto.DepositRequest;
import com.example.payflow.escrow.application.dto.DepositResponse;
import com.example.payflow.escrow.domain.*;
import com.example.payflow.escrow.domain.event.DepositConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositService {
    
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final DepositRepository depositRepository;
    private final EventPublisher eventPublisher;
    private final EscrowEventSourcingService eventSourcingService;
    
    @Transactional
    public DepositResponse processDeposit(DepositRequest request) {
        log.info("입금 처리 시작: transactionId={}, amount={}", 
            request.getTransactionId(), request.getAmount());
        
        // 거래 조회 및 상태 확인
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(request.getTransactionId())
            .orElseThrow(() -> new EscrowNotFoundException(request.getTransactionId()));
        
        // 입금 정보 생성
        Deposit deposit = new Deposit(
            request.getTransactionId(),
            request.getAmount(),
            request.getDepositMethod(),
            request.getDepositReference()
        );
        depositRepository.save(deposit);
        
        // 거래 상태 업데이트
        transaction.confirmDeposit(request.getAmount());
        
        // 입금 확인
        deposit.confirm();
        
        // 이벤트 소싱에 기록
        eventSourcingService.storeEscrowEvent(
            transaction.getTransactionId(),
            "DepositConfirmed",
            "INITIATED",
            "DEPOSITED",
            Map.of(
                "amount", request.getAmount(),
                "depositMethod", request.getDepositMethod(),
                "depositId", deposit.getId()
            ),
            transaction.getBuyer().getUserId()
        );
        
        // 이벤트 발행
        DepositConfirmedEvent event = new DepositConfirmedEvent(
            transaction.getTransactionId(),
            request.getAmount(),
            request.getDepositMethod()
        );
        eventPublisher.publish(event);
        
        log.info("입금 처리 완료: transactionId={}, depositId={}", 
            request.getTransactionId(), deposit.getId());
        
        return DepositResponse.from(deposit);
    }
    
    @Transactional(readOnly = true)
    public List<DepositResponse> getDepositsByTransaction(String transactionId) {
        List<Deposit> deposits = depositRepository.findByTransactionId(transactionId);
        return deposits.stream()
            .map(DepositResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DepositResponse> getConfirmedDeposits(String transactionId) {
        List<Deposit> deposits = depositRepository.findByTransactionIdAndConfirmedAtIsNotNull(transactionId);
        return deposits.stream()
            .map(DepositResponse::from)
            .collect(Collectors.toList());
    }
}
