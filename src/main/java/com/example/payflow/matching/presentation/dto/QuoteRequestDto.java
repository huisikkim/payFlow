package com.example.payflow.matching.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequestDto {
    private String distributorId;
    private String requestedProducts;  // 요청 품목 (콤마 구분)
    private String message;  // 추가 요청사항
}
