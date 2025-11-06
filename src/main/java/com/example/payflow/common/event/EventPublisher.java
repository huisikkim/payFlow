package com.example.payflow.common.event;

public interface EventPublisher {
    void publish(DomainEvent event);
}
