package com.example.payflow.escrow.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OwnershipTransferRequest {
    private String transactionId;
    private String verifiedBy;
    private String documentId;
    private String notes;
    private String newOwnerId;  // 새 소유자 ID
    private LocalDate transferDate;  // 이전 날짜
    private String registrationOffice;  // 등록 사무소
}
