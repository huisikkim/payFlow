package com.example.payflow.parlevel.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateParLevelRequest {
    private Integer minLevel;
    private Integer maxLevel;
    private Integer safetyStock;
    private Integer leadTimeDays;
}
