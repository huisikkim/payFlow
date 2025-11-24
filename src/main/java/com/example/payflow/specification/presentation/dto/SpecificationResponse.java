package com.example.payflow.specification.presentation.dto;

import com.example.payflow.specification.domain.Specification;
import com.example.payflow.specification.domain.SpecificationItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecificationResponse {
    private Long id;
    private String imagePath;
    private String extractedText;
    private String parsedJson;
    private List<SpecificationItemDto> items;
    private String productName;
    private String category;
    private Double price;
    private Integer quantity;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String errorMessage;
    
    public static SpecificationResponse from(Specification spec) {
        return SpecificationResponse.builder()
                .id(spec.getId())
                .imagePath(spec.getImagePath())
                .extractedText(spec.getExtractedText())
                .parsedJson(spec.getParsedJson())
                .items(spec.getItems().stream()
                        .map(SpecificationItemDto::from)
                        .collect(Collectors.toList()))
                .productName(spec.getProductName())
                .category(spec.getCategory())
                .price(spec.getPrice())
                .quantity(spec.getQuantity())
                .status(spec.getStatus().name())
                .createdAt(spec.getCreatedAt())
                .updatedAt(spec.getUpdatedAt())
                .errorMessage(spec.getErrorMessage())
                .build();
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpecificationItemDto {
        private Long id;
        private String itemName;
        private String itemValue;
        private String unit;
        private Integer sequence;
        
        public static SpecificationItemDto from(SpecificationItem item) {
            return SpecificationItemDto.builder()
                    .id(item.getId())
                    .itemName(item.getItemName())
                    .itemValue(item.getItemValue())
                    .unit(item.getUnit())
                    .sequence(item.getSequence())
                    .build();
        }
    }
}
