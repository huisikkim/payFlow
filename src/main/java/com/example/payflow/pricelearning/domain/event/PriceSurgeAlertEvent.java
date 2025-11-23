package com.example.payflow.pricelearning.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PriceSurgeAlertEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String eventType;
    private final String alertId;
    private final String itemName;
    private final Long currentPrice;
    private final Long averagePrice;
    private final Double surgePercentage;
    private final String orderId;
    
    public PriceSurgeAlertEvent(String alertId, String itemName, Long currentPrice,
                               Long averagePrice, Double surgePercentage, String orderId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.eventType = "PriceSurgeAlert";
        this.alertId = alertId;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        this.averagePrice = averagePrice;
        this.surgePercentage = surgePercentage;
        this.orderId = orderId;
    }
}
