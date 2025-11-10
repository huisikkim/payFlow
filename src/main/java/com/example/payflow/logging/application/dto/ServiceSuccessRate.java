package com.example.payflow.logging.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSuccessRate {
    private String serviceName;
    private Double successRate;
}
