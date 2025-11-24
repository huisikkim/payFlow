package com.example.payflow.menu.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequest {
    private String name;
    private String description;
    private String category;
    private String storeId;
    private Long sellingPrice;
    private List<RecipeIngredientRequest> recipeIngredients;
}
