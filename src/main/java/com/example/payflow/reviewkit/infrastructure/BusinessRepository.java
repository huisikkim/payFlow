package com.example.payflow.reviewkit.infrastructure;

import com.example.payflow.reviewkit.domain.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("reviewKitBusinessRepository")
public interface BusinessRepository extends JpaRepository<Business, Long> {
    
    Optional<Business> findBySlug(String slug);
    
    List<Business> findByOwnerId(Long ownerId);
    
    boolean existsBySlug(String slug);
}
