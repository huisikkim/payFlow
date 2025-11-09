package com.example.payflow.saga.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderSagaRepository extends JpaRepository<OrderSaga, Long> {
    Optional<OrderSaga> findBySagaId(String sagaId);
    Optional<OrderSaga> findByOrderId(String orderId);
}
