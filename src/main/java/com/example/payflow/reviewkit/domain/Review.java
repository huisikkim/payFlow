package com.example.payflow.reviewkit.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "ReviewKitReview")
@Table(name = "reviewkit_reviews")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String reviewerName;

    @Column(nullable = false)
    private String reviewerEmail;

    private String reviewerCompany; // Optional (for B2B reviews)

    @Column(nullable = false)
    private Integer rating; // 1-5 stars

    @Column(length = 2000)
    private String content;

    private String photoUrl; // Optional photo attachment

    private String videoUrl; // Optional video testimonial

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime approvedAt;

    private LocalDateTime rejectedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = ReviewStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = ReviewStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
    }
}
