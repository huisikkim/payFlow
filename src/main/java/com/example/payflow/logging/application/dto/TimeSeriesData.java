package com.example.payflow.logging.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesData {
    private String timestamp;
    private Long count;
}
