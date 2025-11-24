package com.example.payflow.menu.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name; // 메뉴명 (예: 김치찌개, 된장찌개)
    
    @Column(length = 1000)
    private String description; // 메뉴 설명
    
    @Column(nullable = false)
    private String category; // 카테고리 (한식, 중식, 일식 등)
    
    @Column(nullable = false)
    private String storeId; // 매장 ID
    
    @Column(nullable = false)
    private Long sellingPrice; // 판매가
    
    @Column(nullable = false)
    private Boolean active; // 활성화 여부
    
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> recipeIngredients = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public Menu(String name, String description, String category, String storeId, Long sellingPrice) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.storeId = storeId;
        this.sellingPrice = sellingPrice;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addRecipeIngredient(RecipeIngredient ingredient) {
        this.recipeIngredients.add(ingredient);
        ingredient.setMenu(this);
    }
    
    public void updateSellingPrice(Long newPrice) {
        this.sellingPrice = newPrice;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateInfo(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }
}
