package com.example.payflow.menu.application;

import com.example.payflow.menu.domain.*;
import com.example.payflow.menu.presentation.dto.MenuRequest;
import com.example.payflow.menu.presentation.dto.RecipeIngredientRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {
    
    private final MenuRepository menuRepository;
    
    @Transactional
    public Menu createMenu(MenuRequest request) {
        Menu menu = new Menu(
            request.getName(),
            request.getDescription(),
            request.getCategory(),
            request.getStoreId(),
            request.getSellingPrice()
        );
        
        // 레시피 재료 추가
        if (request.getRecipeIngredients() != null) {
            for (RecipeIngredientRequest ingredientReq : request.getRecipeIngredients()) {
                RecipeIngredient ingredient = new RecipeIngredient(
                    ingredientReq.getIngredientName(),
                    ingredientReq.getQuantity(),
                    ingredientReq.getUnit(),
                    ingredientReq.getNotes()
                );
                menu.addRecipeIngredient(ingredient);
            }
        }
        
        Menu saved = menuRepository.save(menu);
        log.info("🍽️ 메뉴 생성: id={}, name={}, storeId={}", saved.getId(), saved.getName(), saved.getStoreId());
        return saved;
    }
    
    @Transactional
    public Menu updateMenu(Long menuId, MenuRequest request) {
        Menu menu = menuRepository.findById(menuId)
            .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + menuId));
        
        menu.updateInfo(request.getName(), request.getDescription(), request.getCategory());
        menu.updateSellingPrice(request.getSellingPrice());
        
        log.info("🍽️ 메뉴 수정: id={}, name={}", menuId, request.getName());
        return menu;
    }
    
    @Transactional
    public void deleteMenu(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
            .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + menuId));
        
        menuRepository.delete(menu);
        log.info("🍽️ 메뉴 삭제: id={}, name={}", menuId, menu.getName());
    }
    
    @Transactional
    public void activateMenu(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
            .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + menuId));
        
        menu.activate();
        log.info("🍽️ 메뉴 활성화: id={}, name={}", menuId, menu.getName());
    }
    
    @Transactional
    public void deactivateMenu(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
            .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + menuId));
        
        menu.deactivate();
        log.info("🍽️ 메뉴 비활성화: id={}, name={}", menuId, menu.getName());
    }
    
    @Transactional(readOnly = true)
    public Menu getMenu(Long menuId) {
        return menuRepository.findByIdWithIngredients(menuId);
    }
    
    @Transactional(readOnly = true)
    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Menu> getMenusByStore(String storeId) {
        return menuRepository.findByStoreId(storeId);
    }
    
    @Transactional(readOnly = true)
    public List<Menu> getActiveMenusByStore(String storeId) {
        return menuRepository.findByStoreIdAndActive(storeId, true);
    }
    
    @Transactional(readOnly = true)
    public List<Menu> getMenusByCategory(String category) {
        return menuRepository.findByCategory(category);
    }
    
    @Transactional(readOnly = true)
    public List<String> getCategoriesByStore(String storeId) {
        return menuRepository.findDistinctCategoriesByStoreId(storeId);
    }
}
