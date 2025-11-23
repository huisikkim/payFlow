package com.example.payflow.ingredientorder.presentation.dto;

import com.example.payflow.ingredientorder.domain.IngredientOrderItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IngredientOrderItemDto {
    
    @NotBlank(message = "품목명은 필수입니다.")
    private String itemName;
    
    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;
    
    @NotNull(message = "단가는 필수입니다.")
    @Min(value = 0, message = "단가는 0 이상이어야 합니다.")
    private Long unitPrice;
    
    private String unit;
    
    private Long subtotal;
    
    public IngredientOrderItemDto(String itemName, Integer quantity, Long unitPrice, String unit) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.subtotal = quantity * unitPrice;
    }
    
    public static IngredientOrderItemDto from(IngredientOrderItem item) {
        return new IngredientOrderItemDto(
            item.getItemName(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getUnit()
        );
    }
}
