package com.example.payflow.reviewkit.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "ReviewKitWidget")
@Table(name = "reviewkit_widgets")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Widget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(unique = true, nullable = false)
    private String widgetId; // Public identifier (UUID)

    @Column(nullable = false)
    private String name; // Widget name for internal reference

    // Widget configuration
    private String theme; // "light" or "dark"
    
    private String layout; // "grid" or "list"
    
    @Builder.Default
    private Integer displayLimit = 6; // Number of reviews to show
    
    private String language; // "ko" or "en"

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (widgetId == null) {
            widgetId = UUID.randomUUID().toString();
        }
    }

    public String getEmbedCode() {
        return String.format(
            "<script src=\"/static/js/reviewkit-widget.js\"></script>\n" +
            "<div data-reviewkit-widget=\"%s\"></div>",
            widgetId
        );
    }
}
