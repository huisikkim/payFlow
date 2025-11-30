package com.example.payflow.qualityissue.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SchedulePickupRequest {
    private LocalDateTime pickupTime;
}
