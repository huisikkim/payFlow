package com.example.payflow.invoice.presentation.dto;

import com.example.payflow.invoice.domain.InvoiceItem;
import lombok.Getter;

@Getter
public class InvoiceItemDto {
    
    private final String itemName;
    private final Integer quantity;
    private final Long unitPrice;
    private final Long amount;
    private final String unit;
    
    public InvoiceItemDto(String itemName, Integer quantity, Long unitPrice, Long amount, String unit) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
        this.unit = unit;
    }
    
    public static InvoiceItemDto from(InvoiceItem item) {
        return new InvoiceItemDto(
            item.getItemName(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getAmount(),
            item.getUnit()
        );
    }
}
