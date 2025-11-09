package com.example.payflow.inventory.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    Optional<InventoryReservation> findByOrderId(String orderId);
}
