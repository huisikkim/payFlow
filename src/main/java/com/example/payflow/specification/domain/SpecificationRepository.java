package com.example.payflow.specification.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpecificationRepository extends JpaRepository<Specification, Long> {
    List<Specification> findByStatus(ProcessingStatus status);
    List<Specification> findByProductNameContaining(String productName);
}
