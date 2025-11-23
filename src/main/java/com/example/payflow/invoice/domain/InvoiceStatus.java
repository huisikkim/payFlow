package com.example.payflow.invoice.domain;

public enum InvoiceStatus {
    UPLOADED,     // 업로드됨
    PARSING,      // 파싱 중
    PARSED,       // 파싱 완료
    PARSE_FAILED, // 파싱 실패
    VERIFIED      // 검증 완료
}
