package com.example.payflow.parlevel.presentation.dto;

import com.example.payflow.parlevel.domain.ConsumptionPattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ConsumptionPatternResponse {
    private Long id;
    private String storeId;
    private String itemName;
    private LocalDate consumptionDate;
    private Integer quantity;
    private String unit;
    private DayOfWeek dayOfWeek;
    private Integer weekOfMonth;
    private Integer month;
    
    public static ConsumptionPatternResponse from(ConsumptionPattern pattern) {
        return new ConsumptionPatternResponse(
            pattern.getId(),
            pattern.getStoreId(),
            pattern.getItemName(),
            pattern.getConsumptionDate(),
            pattern.getQuantity(),
            pattern.getUnit(),
            pattern.getDayOfWeek(),
            pattern.getWeekOfMonth(),
            pattern.getMonth()
        );
    }
}
