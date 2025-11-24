package com.example.payflow.menu.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    
    List<Menu> findByStoreId(String storeId);
    
    List<Menu> findByStoreIdAndActive(String storeId, Boolean active);
    
    List<Menu> findByCategory(String category);
    
    List<Menu> findByStoreIdAndCategory(String storeId, String category);
    
    @Query("SELECT DISTINCT m.category FROM Menu m WHERE m.storeId = :storeId")
    List<String> findDistinctCategoriesByStoreId(@Param("storeId") String storeId);
    
    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.recipeIngredients WHERE m.id = :id")
    Menu findByIdWithIngredients(@Param("id") Long id);
}
