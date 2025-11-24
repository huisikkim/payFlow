package com.example.payflow.menu.presentation.dto;

import com.example.payflow.menu.domain.Menu;
import com.example.payflow.menu.domain.RecipeIngredient;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MenuResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String storeId;
    private Long sellingPrice;
    private Boolean active;
    private List<RecipeIngredientResponse> recipeIngredients;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Getter
    @AllArgsConstructor
    public static class RecipeIngredientResponse {
        private Long id;
        private String ingredientName;
        private BigDecimal quantity;
        private String unit;
        private String notes;
        
        public static RecipeIngredientResponse from(RecipeIngredient ingredient) {
            return new RecipeIngredientResponse(
                ingredient.getId(),
                ingredient.getIngredientName(),
                ingredient.getQuantity(),
                ingredient.getUnit(),
                ingredient.getNotes()
            );
        }
    }
    
    public static MenuResponse from(Menu menu) {
        List<RecipeIngredientResponse> ingredients = menu.getRecipeIngredients().stream()
            .map(RecipeIngredientResponse::from)
            .toList();
        
        return new MenuResponse(
            menu.getId(),
            menu.getName(),
            menu.getDescription(),
            menu.getCategory(),
            menu.getStoreId(),
            menu.getSellingPrice(),
            menu.getActive(),
            ingredients,
            menu.getCreatedAt(),
            menu.getUpdatedAt()
        );
    }
}
