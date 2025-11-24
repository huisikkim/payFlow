package com.example.payflow.menu.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientRequest {
    private String ingredientName;
    private BigDecimal quantity;
    private String unit;
    private String notes;
}
