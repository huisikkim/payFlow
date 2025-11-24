package com.example.payflow.specification.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedSpecificationDto {
    private String productName;
    private String category;
    private Double price;
    private Integer quantity;
    private List<SpecItem> specifications;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpecItem {
        private String name;
        private String value;
        private String unit;
    }
}
