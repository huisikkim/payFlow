package com.example.payflow.parlevel.presentation.dto;

import com.example.payflow.parlevel.domain.ParLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ParLevelResponse {
    private Long id;
    private String storeId;
    private String itemName;
    private String unit;
    private Integer minLevel;
    private Integer maxLevel;
    private Integer safetyStock;
    private Integer leadTimeDays;
    private Boolean autoOrderEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ParLevelResponse from(ParLevel parLevel) {
        return new ParLevelResponse(
            parLevel.getId(),
            parLevel.getStoreId(),
            parLevel.getItemName(),
            parLevel.getUnit(),
            parLevel.getMinLevel(),
            parLevel.getMaxLevel(),
            parLevel.getSafetyStock(),
            parLevel.getLeadTimeDays(),
            parLevel.getAutoOrderEnabled(),
            parLevel.getCreatedAt(),
            parLevel.getUpdatedAt()
        );
    }
}
