package com.example.payflow.escrow.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.escrow.application.dto.DisputeRequest;
import com.example.payflow.escrow.application.dto.DisputeResponse;
import com.example.payflow.escrow.domain.*;
import com.example.payflow.escrow.domain.event.DisputeRaisedEvent;
import com.example.payflow.escrow.domain.event.DisputeResolvedEvent;
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
public class DisputeService {
    
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final DisputeRepository disputeRepository;
    private final EventPublisher eventPublisher;
    private final EscrowEventSourcingService eventSourcingService;
    
    @Transactional
    public DisputeResponse raiseDispute(DisputeRequest request) {
        log.info("분쟁 제기: transactionId={}, raisedBy={}", 
            request.getTransactionId(), request.getRaisedBy());
        
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(request.getTransactionId())
            .orElseThrow(() -> new EscrowNotFoundException(request.getTransactionId()));
        
        // 분쟁 정보 생성
        Dispute dispute = new Dispute(
            request.getTransactionId(),
            request.getReason(),
            request.getRaisedBy()
        );
        disputeRepository.save(dispute);
        
        // 거래 상태 업데이트
        EscrowStatus previousStatus = transaction.getStatus();
        transaction.raiseDispute(request.getReason(), request.getRaisedBy());
        
        // 이벤트 소싱에 기록
        eventSourcingService.storeEscrowEvent(
            transaction.getTransactionId(),
            "DisputeRaised",
            previousStatus.name(),
            "DISPUTED",
            Map.of(
                "raisedBy", request.getRaisedBy(),
                "reason", request.getReason(),
                "disputeId", dispute.getId()
            ),
            request.getRaisedBy()
        );
        
        // 이벤트 발행
        DisputeRaisedEvent event = new DisputeRaisedEvent(
            transaction.getTransactionId(),
            request.getRaisedBy(),
            request.getReason()
        );
        eventPublisher.publish(event);
        
        log.info("분쟁 제기 완료: transactionId={}, disputeId={}", 
            request.getTransactionId(), dispute.getId());
        
        return DisputeResponse.from(dispute);
    }
    
    @Transactional
    public DisputeResponse resolveDispute(Long disputeId, String resolution, String resolvedBy) {
        log.info("분쟁 해결: disputeId={}, resolvedBy={}", disputeId, resolvedBy);
        
        Dispute dispute = disputeRepository.findById(disputeId)
            .orElseThrow(() -> new IllegalArgumentException("분쟁을 찾을 수 없습니다: " + disputeId));
        
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(dispute.getTransactionId())
            .orElseThrow(() -> new EscrowNotFoundException(dispute.getTransactionId()));
        
        // 분쟁 해결
        dispute.resolve(resolution, resolvedBy);
        
        // 이벤트 소싱에 기록
        eventSourcingService.storeEscrowEvent(
            transaction.getTransactionId(),
            "DisputeResolved",
            "DISPUTED",
            transaction.getStatus().name(),
            Map.of(
                "resolvedBy", resolvedBy,
                "resolution", resolution,
                "disputeId", disputeId
            ),
            resolvedBy
        );
        
        // 거래 상태는 분쟁 해결 결과에 따라 별도로 처리
        // (예: 구매자 승 -> 취소 및 환불, 판매자 승 -> 정산 진행)
        
        // 이벤트 발행
        DisputeResolvedEvent event = new DisputeResolvedEvent(
            transaction.getTransactionId(),
            resolvedBy,
            resolution
        );
        eventPublisher.publish(event);
        
        log.info("분쟁 해결 완료: disputeId={}", disputeId);
        
        return DisputeResponse.from(dispute);
    }
    
    @Transactional(readOnly = true)
    public List<DisputeResponse> getDisputesByTransaction(String transactionId) {
        List<Dispute> disputes = disputeRepository.findByTransactionId(transactionId);
        return disputes.stream()
            .map(DisputeResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DisputeResponse> getOpenDisputes() {
        List<Dispute> disputes = disputeRepository.findByStatus(DisputeStatus.OPEN);
        return disputes.stream()
            .map(DisputeResponse::from)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DisputeResponse> getDisputesByStatus(DisputeStatus status) {
        List<Dispute> disputes = disputeRepository.findByStatus(status);
        return disputes.stream()
            .map(DisputeResponse::from)
            .collect(Collectors.toList());
    }
}
