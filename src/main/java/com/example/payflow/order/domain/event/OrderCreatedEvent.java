package com.example.payflow.order.domain.event;

import com.example.payflow.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class OrderCreatedEvent implements DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String orderId;
    private final String orderName;
    private final Long amount;
    private final String customerEmail;
    
    public OrderCreatedEvent(String orderId, String orderName, Long amount, String customerEmail) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
        this.customerEmail = customerEmail;
    }
    
    @Override
    public String getEventType() {
        return "OrderCreated";
    }
}
