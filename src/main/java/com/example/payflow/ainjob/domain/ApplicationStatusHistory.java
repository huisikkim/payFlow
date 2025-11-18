package com.example.payflow.ainjob.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ainjob_application_status_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private ApplicationTracking application;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ApplicationStatus fromStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus toStatus;
    
    @Column(length = 2000)
    private String reason;
    
    @Column(length = 100)
    private String changedBy;
    
    @Column(nullable = false)
    private LocalDateTime changedAt;
    
    public static ApplicationStatusHistory create(ApplicationStatus fromStatus, ApplicationStatus toStatus,
                                                  String reason, String changedBy) {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.fromStatus = fromStatus;
        history.toStatus = toStatus;
        history.reason = reason;
        history.changedBy = changedBy;
        history.changedAt = LocalDateTime.now();
        return history;
    }
    
    void setApplication(ApplicationTracking application) {
        this.application = application;
    }
}
