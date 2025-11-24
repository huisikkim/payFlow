package com.example.payflow.menu.presentation;

import com.example.payflow.menu.application.MarginSimulator;
import com.example.payflow.menu.application.MenuCostCalculator;
import com.example.payflow.menu.application.MenuService;
import com.example.payflow.menu.domain.MarginSimulation;
import com.example.payflow.menu.domain.Menu;
import com.example.payflow.menu.domain.MenuCostAnalysis;
import com.example.payflow.menu.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuController {
    
    private final MenuService menuService;
    private final MenuCostCalculator menuCostCalculator;
    private final MarginSimulator marginSimulator;
    
    @PostMapping
    public ResponseEntity<MenuResponse> createMenu(@RequestBody MenuRequest request) {
        Menu menu = menuService.createMenu(request);
        return ResponseEntity.ok(MenuResponse.from(menu));
    }
    
    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(
            @PathVariable Long menuId,
            @RequestBody MenuRequest request) {
        Menu menu = menuService.updateMenu(menuId, request);
        return ResponseEntity.ok(MenuResponse.from(menu));
    }
    
    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{menuId}/activate")
    public ResponseEntity<Void> activateMenu(@PathVariable Long menuId) {
        menuService.activateMenu(menuId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{menuId}/deactivate")
    public ResponseEntity<Void> deactivateMenu(@PathVariable Long menuId) {
        menuService.deactivateMenu(menuId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{menuId}")
    public ResponseEntity<MenuResponse> getMenu(@PathVariable Long menuId) {
        Menu menu = menuService.getMenu(menuId);
        return ResponseEntity.ok(MenuResponse.from(menu));
    }
    
    @GetMapping
    public ResponseEntity<List<MenuResponse>> getAllMenus() {
        List<Menu> menus = menuService.getAllMenus();
        List<MenuResponse> responses = menus.stream()
            .map(MenuResponse::from)
            .toList();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<MenuResponse>> getMenusByStore(@PathVariable String storeId) {
        List<Menu> menus = menuService.getMenusByStore(storeId);
        List<MenuResponse> responses = menus.stream()
            .map(MenuResponse::from)
            .toList();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/store/{storeId}/active")
    public ResponseEntity<List<MenuResponse>> getActiveMenusByStore(@PathVariable String storeId) {
        List<Menu> menus = menuService.getActiveMenusByStore(storeId);
        List<MenuResponse> responses = menus.stream()
            .map(MenuResponse::from)
            .toList();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<MenuResponse>> getMenusByCategory(@PathVariable String category) {
        List<Menu> menus = menuService.getMenusByCategory(category);
        List<MenuResponse> responses = menus.stream()
            .map(MenuResponse::from)
            .toList();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/store/{storeId}/categories")
    public ResponseEntity<List<String>> getCategoriesByStore(@PathVariable String storeId) {
        List<String> categories = menuService.getCategoriesByStore(storeId);
        return ResponseEntity.ok(categories);
    }
    
    // 원가 계산
    @GetMapping("/{menuId}/cost")
    public ResponseEntity<MenuCostAnalysisResponse> calculateCost(@PathVariable Long menuId) {
        MenuCostAnalysis analysis = menuCostCalculator.calculateCost(menuId);
        return ResponseEntity.ok(MenuCostAnalysisResponse.from(analysis));
    }
    
    @GetMapping("/store/{storeId}/costs")
    public ResponseEntity<List<MenuCostAnalysisResponse>> calculateAllCosts(@PathVariable String storeId) {
        List<MenuCostAnalysis> analyses = menuCostCalculator.calculateAllCosts(storeId);
        List<MenuCostAnalysisResponse> responses = analyses.stream()
            .map(MenuCostAnalysisResponse::from)
            .toList();
        return ResponseEntity.ok(responses);
    }
    
    // 마진 시뮬레이션
    @GetMapping("/{menuId}/simulate/price")
    public ResponseEntity<MarginSimulationResponse> simulateByPrice(
            @PathVariable Long menuId,
            @RequestParam Long targetPrice) {
        MarginSimulation simulation = marginSimulator.simulateByPrice(menuId, targetPrice);
        return ResponseEntity.ok(MarginSimulationResponse.from(simulation));
    }
    
    @GetMapping("/{menuId}/simulate/margin")
    public ResponseEntity<MarginSimulationResponse> simulateByMargin(
            @PathVariable Long menuId,
            @RequestParam BigDecimal targetMargin) {
        MarginSimulation simulation = marginSimulator.simulateByMargin(menuId, targetMargin);
        return ResponseEntity.ok(MarginSimulationResponse.from(simulation));
    }
}
