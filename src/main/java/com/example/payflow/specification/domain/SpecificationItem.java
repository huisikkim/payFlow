package com.example.payflow.specification.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "specification_items")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecificationItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String itemName;
    
    @Column(columnDefinition = "TEXT")
    private String itemValue;
    
    private String unit;
    
    private Integer sequence;
}
