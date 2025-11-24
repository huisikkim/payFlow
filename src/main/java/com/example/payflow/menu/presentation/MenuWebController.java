package com.example.payflow.menu.presentation;

import com.example.payflow.menu.application.MenuCostCalculator;
import com.example.payflow.menu.application.MenuService;
import com.example.payflow.menu.domain.Menu;
import com.example.payflow.menu.domain.MenuCostAnalysis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuWebController {
    
    private final MenuService menuService;
    private final MenuCostCalculator menuCostCalculator;
    
    @GetMapping
    public String menuList(
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String category,
            Model model) {
        
        List<Menu> menus;
        if (storeId != null && !storeId.isEmpty()) {
            if (category != null && !category.isEmpty()) {
                menus = menuService.getMenusByStore(storeId).stream()
                    .filter(m -> m.getCategory().equals(category))
                    .toList();
            } else {
                menus = menuService.getMenusByStore(storeId);
            }
        } else if (category != null && !category.isEmpty()) {
            menus = menuService.getMenusByCategory(category);
        } else {
            menus = menuService.getAllMenus();
        }
        
        model.addAttribute("menus", menus);
        model.addAttribute("storeId", storeId);
        model.addAttribute("category", category);
        return "menu/list";
    }
    
    @GetMapping("/{menuId}")
    public String menuDetail(@PathVariable Long menuId, Model model) {
        Menu menu = menuService.getMenu(menuId);
        MenuCostAnalysis analysis = menuCostCalculator.calculateCost(menuId);
        
        model.addAttribute("menu", menu);
        model.addAttribute("analysis", analysis);
        return "menu/detail";
    }
    
    @GetMapping("/create")
    public String createForm(Model model) {
        return "menu/create";
    }
    
    @GetMapping("/store/{storeId}/costs")
    public String storeCosts(@PathVariable String storeId, Model model) {
        List<MenuCostAnalysis> analyses = menuCostCalculator.calculateAllCosts(storeId);
        
        model.addAttribute("storeId", storeId);
        model.addAttribute("analyses", analyses);
        return "menu/store-costs";
    }
    
    @GetMapping("/{menuId}/simulator")
    public String marginSimulator(@PathVariable Long menuId, Model model) {
        Menu menu = menuService.getMenu(menuId);
        MenuCostAnalysis analysis = menuCostCalculator.calculateCost(menuId);
        
        model.addAttribute("menu", menu);
        model.addAttribute("analysis", analysis);
        return "menu/simulator";
    }
}
