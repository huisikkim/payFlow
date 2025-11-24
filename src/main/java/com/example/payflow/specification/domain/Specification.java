package com.example.payflow.specification.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "specifications")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Specification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String imagePath;
    
    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;
    
    @Column(columnDefinition = "LONGTEXT")
    private String parsedJson;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "specification_id")
    @Builder.Default
    private List<SpecificationItem> items = new ArrayList<>();
    
    @Column(nullable = false)
    private String productName;
    
    private String category;
    
    private Double price;
    
    private Integer quantity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String errorMessage;
    
    public void updateExtractedText(String text) {
        this.extractedText = text;
        this.status = ProcessingStatus.TEXT_EXTRACTED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateParsedData(String json, List<SpecificationItem> items, String productName, String category, Double price, Integer quantity) {
        this.parsedJson = json;
        this.items = items;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.status = ProcessingStatus.PARSED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsError(String errorMsg) {
        this.status = ProcessingStatus.ERROR;
        this.errorMessage = errorMsg;
        this.updatedAt = LocalDateTime.now();
    }
}
