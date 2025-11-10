package com.example.payflow.logging.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardMetrics {
    private String timeRange;
    private Long totalEvents;
    private Long failedEvents;
    private List<TimeSeriesData> hourlyEventCounts;
    private List<EventTypeCount> eventTypeCounts;
    private List<ServiceSuccessRate> serviceSuccessRates;
    private List<ServiceProcessingTime> serviceProcessingTimes;
    private LocalDateTime generatedAt;
}
