package com.example.payflow.matching.presentation.dto;

import com.example.payflow.matching.domain.QuoteRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponseDto {
    private QuoteRequest.QuoteStatus status;  // ACCEPTED 또는 REJECTED
    private Integer estimatedAmount;  // 예상 금액
    private String response;  // 응답 메시지
}
