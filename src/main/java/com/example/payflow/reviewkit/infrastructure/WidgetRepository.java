package com.example.payflow.reviewkit.infrastructure;

import com.example.payflow.reviewkit.domain.Widget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("reviewKitWidgetRepository")
public interface WidgetRepository extends JpaRepository<Widget, Long> {
    
    Optional<Widget> findByWidgetId(String widgetId);
    
    List<Widget> findByBusinessId(Long businessId);
}
