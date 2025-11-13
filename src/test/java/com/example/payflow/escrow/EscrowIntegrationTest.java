package com.example.payflow.escrow;

import com.example.payflow.escrow.application.*;
import com.example.payflow.escrow.application.dto.*;
import com.example.payflow.escrow.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 에스크로 통합 테스트
 * 전체 거래 플로우를 테스트
 */
@SpringBootTest
@Transactional
class EscrowIntegrationTest {
    
    @Autowired
    private EscrowService escrowService;
    
    @Autowired
    private DepositService depositService;
    
    @Autowired
    private VerificationService verificationService;
    
    @Autowired
    private SettlementService settlementService;
    
    @Autowired
    private EscrowTransactionRepository escrowTransactionRepository;
    
    @Test
    void 에스크로_거래_전체_플로우_테스트() {
        // 1. 거래 생성
        CreateEscrowRequest createRequest = createTestEscrowRequest();
        EscrowResponse escrow = escrowService.createEscrow(createRequest);
        
        assertThat(escrow).isNotNull();
        assertThat(escrow.getTransactionId()).isNotNull();
        assertThat(escrow.getStatus()).isEqualTo(EscrowStatus.INITIATED);
        
        // 2. 입금 처리
        DepositRequest depositRequest = new DepositRequest(
            escrow.getTransactionId(),
            escrow.getAmount(),
            "BANK_TRANSFER",
            "DEP-001"
        );
        DepositResponse deposit = depositService.processDeposit(depositRequest);
        
        assertThat(deposit).isNotNull();
        assertThat(deposit.isConfirmed()).isTrue();
        
        // 거래 상태 확인
        EscrowResponse afterDeposit = escrowService.getEscrow(escrow.getTransactionId());
        assertThat(afterDeposit.getStatus()).isEqualTo(EscrowStatus.DEPOSITED);
        
        // 3. 차량 인도 확인
        DeliveryConfirmRequest deliveryRequest = new DeliveryConfirmRequest(
            escrow.getTransactionId(),
            "seller1"
        );
        EscrowResponse afterDelivery = verificationService.confirmDelivery(deliveryRequest);
        
        assertThat(afterDelivery.getStatus()).isEqualTo(EscrowStatus.DELIVERED);
        
        // 4. 차량 검증
        VerificationRequest verificationRequest = new VerificationRequest(
            escrow.getTransactionId(),
            VerificationType.VEHICLE_CONDITION,
            VerificationResult.PASSED,
            "inspector1",
            "차량 상태 양호",
            "DOC-001"
        );
        VerificationResponse verification = verificationService.verifyVehicle(verificationRequest);
        
        assertThat(verification).isNotNull();
        assertThat(verification.getResult()).isEqualTo(VerificationResult.PASSED);
        
        // 거래 상태 확인
        EscrowResponse afterVerification = escrowService.getEscrow(escrow.getTransactionId());
        assertThat(afterVerification.getStatus()).isEqualTo(EscrowStatus.VERIFIED);
        
        // 5. 명의 이전 확인
        OwnershipTransferRequest ownershipRequest = new OwnershipTransferRequest(
            escrow.getTransactionId(),
            "inspector1",
            "DOC-002",
            "명의 이전 완료"
        );
        VerificationResponse ownershipVerification = verificationService.confirmOwnershipTransfer(ownershipRequest);
        
        assertThat(ownershipVerification).isNotNull();
        
        // 거래 상태 확인
        EscrowResponse afterOwnership = escrowService.getEscrow(escrow.getTransactionId());
        assertThat(afterOwnership.getStatus()).isEqualTo(EscrowStatus.OWNERSHIP_TRANSFERRED);
        
        // 6. 정산 시작
        SettlementResponse settlement = settlementService.startSettlement(escrow.getTransactionId());
        
        assertThat(settlement).isNotNull();
        assertThat(settlement.getTotalAmount()).isEqualTo(escrow.getAmount());
        assertThat(settlement.getSellerAmount()).isEqualTo(escrow.getSellerAmount());
        
        // 거래 상태 확인
        EscrowResponse afterSettlement = escrowService.getEscrow(escrow.getTransactionId());
        assertThat(afterSettlement.getStatus()).isEqualTo(EscrowStatus.SETTLING);
        
        // 7. 정산 완료
        SettlementResponse completedSettlement = settlementService.completeSettlement(
            escrow.getTransactionId(),
            "BANK_TRANSFER",
            "PAY-001"
        );
        
        assertThat(completedSettlement.isCompleted()).isTrue();
        
        // 최종 거래 상태 확인
        EscrowResponse finalEscrow = escrowService.getEscrow(escrow.getTransactionId());
        assertThat(finalEscrow.getStatus()).isEqualTo(EscrowStatus.COMPLETED);
        assertThat(finalEscrow.getCompletedAt()).isNotNull();
    }
    
    @Test
    void 거래_취소_플로우_테스트() {
        // 1. 거래 생성
        CreateEscrowRequest createRequest = createTestEscrowRequest();
        EscrowResponse escrow = escrowService.createEscrow(createRequest);
        
        assertThat(escrow.getStatus()).isEqualTo(EscrowStatus.INITIATED);
        
        // 2. 거래 취소
        EscrowResponse cancelled = escrowService.cancelEscrow(
            escrow.getTransactionId(),
            "구매자 요청으로 취소"
        );
        
        assertThat(cancelled.getStatus()).isEqualTo(EscrowStatus.CANCELLED);
    }
    
    @Test
    void 검증_실패_플로우_테스트() {
        // 1. 거래 생성 및 입금
        CreateEscrowRequest createRequest = createTestEscrowRequest();
        EscrowResponse escrow = escrowService.createEscrow(createRequest);
        
        DepositRequest depositRequest = new DepositRequest(
            escrow.getTransactionId(),
            escrow.getAmount(),
            "BANK_TRANSFER",
            "DEP-001"
        );
        depositService.processDeposit(depositRequest);
        
        // 2. 차량 인도
        DeliveryConfirmRequest deliveryRequest = new DeliveryConfirmRequest(
            escrow.getTransactionId(),
            "seller1"
        );
        verificationService.confirmDelivery(deliveryRequest);
        
        // 3. 차량 검증 실패
        VerificationRequest verificationRequest = new VerificationRequest(
            escrow.getTransactionId(),
            VerificationType.VEHICLE_CONDITION,
            VerificationResult.FAILED,
            "inspector1",
            "차량 상태 불량",
            null
        );
        VerificationResponse verification = verificationService.verifyVehicle(verificationRequest);
        
        assertThat(verification.getResult()).isEqualTo(VerificationResult.FAILED);
        
        // 거래 상태 확인
        EscrowResponse afterVerification = escrowService.getEscrow(escrow.getTransactionId());
        assertThat(afterVerification.getStatus()).isEqualTo(EscrowStatus.VERIFICATION_FAILED);
    }
    
    private CreateEscrowRequest createTestEscrowRequest() {
        ParticipantDto buyer = new ParticipantDto(
            "buyer1",
            "홍길동",
            "buyer@test.com",
            "010-1234-5678"
        );
        
        ParticipantDto seller = new ParticipantDto(
            "seller1",
            "김판매",
            "seller@test.com",
            "010-9876-5432"
        );
        
        VehicleDto vehicle = new VehicleDto(
            "VIN123456789",
            "현대",
            "그랜저",
            2023,
            "12가3456"
        );
        
        return new CreateEscrowRequest(
            buyer,
            seller,
            vehicle,
            new BigDecimal("50000000"),
            new BigDecimal("0.03")
        );
    }
}
