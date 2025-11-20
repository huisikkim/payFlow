package com.example.payflow.auction.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionCreateRequest {
    
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;
    
    @NotNull(message = "시작가는 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "시작가는 0보다 커야 합니다.")
    private BigDecimal startPrice;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "즉시 구매가는 0보다 커야 합니다.")
    private BigDecimal buyNowPrice;
    
    @NotNull(message = "최소 입찰 단위는 필수입니다.")
    @DecimalMin(value = "100.0", message = "최소 입찰 단위는 100원 이상이어야 합니다.")
    @Builder.Default
    private BigDecimal minBidIncrement = new BigDecimal("1000");
    
    @NotNull(message = "경매 시작 시간은 필수입니다.")
    @Future(message = "경매 시작 시간은 미래여야 합니다.")
    private LocalDateTime startTime;
    
    @NotNull(message = "경매 종료 시간은 필수입니다.")
    @Future(message = "경매 종료 시간은 미래여야 합니다.")
    private LocalDateTime endTime;
}
