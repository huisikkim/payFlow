package com.example.payflow.escrow.application;

import com.example.payflow.common.event.EventPublisher;
import com.example.payflow.escrow.application.dto.*;
import com.example.payflow.escrow.domain.*;
import com.example.payflow.escrow.domain.event.*;
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
public class VerificationService {
    
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final VerificationRepository verificationRepository;
    private final EventPublisher eventPublisher;
    private final EscrowEventSourcingService eventSourcingService;
    
    @Transactional
    public EscrowResponse confirmDelivery(DeliveryConfirmRequest request) {
        log.info("차량 인도 확인 시작: transactionId={}", request.getTransactionId());
        
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(request.getTransactionId())
            .orElseThrow(() -> new EscrowNotFoundException(request.getTransactionId()));
        
        transaction.confirmDelivery();
        
        // 이벤트 소싱에 기록
        eventSourcingService.storeEscrowEvent(
            transaction.getTransactionId(),
            "VehicleDelivered",
            "DEPOSITED",
            "DELIVERED",
            Map.of("vehicleVin", transaction.getVehicle().getVin()),
            request.getConfirmedBy()
        );
        
        // 이벤트 발행
        VehicleDeliveredEvent event = new VehicleDeliveredEvent(
            transaction.getTransactionId(),
            transaction.getVehicle().getVin()
        );
        eventPublisher.publish(event);
        
        log.info("차량 인도 확인 완료: transactionId={}", request.getTransactionId());
        
        return EscrowResponse.from(transaction);
    }
    
    @Transactional
    public VerificationResponse verifyVehicle(VerificationRequest request) {
        log.info("차량 검증 시작: transactionId={}, type={}, result={}", 
            request.getTransactionId(), request.getType(), request.getResult());
        
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(request.getTransactionId())
            .orElseThrow(() -> new EscrowNotFoundException(request.getTransactionId()));
        
        // 검증 정보 저장
        Verification verification = new Verification(
            request.getTransactionId(),
            request.getType(),
            request.getResult(),
            request.getVerifiedBy(),
            request.getNotes(),
            request.getDocumentId()
        );
        verificationRepository.save(verification);
        
        // 차량 상태 검증인 경우에만 거래 상태 업데이트
        if (request.getType() == VerificationType.VEHICLE_CONDITION) {
            transaction.verifyVehicle(request.getResult());
            
            // 이벤트 발행
            if (request.getResult() == VerificationResult.PASSED) {
                // 이벤트 소싱에 기록
                eventSourcingService.storeEscrowEvent(
                    transaction.getTransactionId(),
                    "VehicleVerified",
                    "DELIVERED",
                    "VERIFIED",
                    Map.of(
                        "verifiedBy", request.getVerifiedBy(),
                        "vehicleVin", transaction.getVehicle().getVin(),
                        "verificationId", verification.getId()
                    ),
                    request.getVerifiedBy()
                );
                
                VehicleVerifiedEvent event = new VehicleVerifiedEvent(
                    transaction.getTransactionId(),
                    request.getVerifiedBy(),
                    transaction.getVehicle().getVin()
                );
                eventPublisher.publish(event);
            } else {
                // 이벤트 소싱에 기록
                eventSourcingService.storeEscrowEvent(
                    transaction.getTransactionId(),
                    "VerificationFailed",
                    "DELIVERED",
                    "VERIFICATION_FAILED",
                    Map.of(
                        "verifiedBy", request.getVerifiedBy(),
                        "reason", request.getNotes(),
                        "verificationId", verification.getId()
                    ),
                    request.getVerifiedBy()
                );
                
                VerificationFailedEvent event = new VerificationFailedEvent(
                    transaction.getTransactionId(),
                    request.getVerifiedBy(),
                    request.getNotes()
                );
                eventPublisher.publish(event);
            }
        }
        
        log.info("차량 검증 완료: transactionId={}, verificationId={}", 
            request.getTransactionId(), verification.getId());
        
        return VerificationResponse.from(verification);
    }
    
    @Transactional
    public VerificationResponse confirmOwnershipTransfer(OwnershipTransferRequest request) {
        log.info("명의 이전 확인 시작: transactionId={}", request.getTransactionId());
        
        EscrowTransaction transaction = escrowTransactionRepository.findByTransactionId(request.getTransactionId())
            .orElseThrow(() -> new EscrowNotFoundException(request.getTransactionId()));
        
        // 명의 이전 검증 정보 저장
        Verification verification = new Verification(
            request.getTransactionId(),
            VerificationType.OWNERSHIP_TRANSFER,
            VerificationResult.PASSED,
            request.getVerifiedBy(),
            request.getNotes(),
            request.getDocumentId()
        );
        verificationRepository.save(verification);
        
        // 거래 상태 업데이트
        transaction.confirmOwnershipTransfer();
        
        // 이벤트 소싱에 기록
        eventSourcingService.storeEscrowEvent(
            transaction.getTransactionId(),
            "OwnershipTransferred",
            "VERIFIED",
            "OWNERSHIP_TRANSFERRED",
            Map.of(
                "vehicleVin", transaction.getVehicle().getVin(),
                "newOwnerId", transaction.getBuyer().getUserId(),
                "verificationId", verification.getId()
            ),
            request.getVerifiedBy()
        );
        
        // 이벤트 발행
        OwnershipTransferredEvent event = new OwnershipTransferredEvent(
            transaction.getTransactionId(),
            transaction.getVehicle().getVin(),
            transaction.getBuyer().getUserId()
        );
        eventPublisher.publish(event);
        
        log.info("명의 이전 확인 완료: transactionId={}, verificationId={}", 
            request.getTransactionId(), verification.getId());
        
        return VerificationResponse.from(verification);
    }
    
    @Transactional(readOnly = true)
    public List<VerificationResponse> getVerificationsByTransaction(String transactionId) {
        List<Verification> verifications = verificationRepository.findByTransactionId(transactionId);
        return verifications.stream()
            .map(VerificationResponse::from)
            .collect(Collectors.toList());
    }
}
