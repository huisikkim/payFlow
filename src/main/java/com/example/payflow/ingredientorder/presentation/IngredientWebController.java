package com.example.payflow.ingredientorder.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ingredient")
public class IngredientWebController {
    
    @GetMapping("/store")
    public String storeOrder() {
        return "ingredient/store-order";
    }
    
    @GetMapping("/store/settlements")
    public String storeSettlements() {
        return "ingredient/settlement-dashboard";
    }
    
    @GetMapping("/distributor")
    public String distributorConfirm() {
        return "ingredient/distributor-confirm";
    }
    
    @GetMapping("/distributor/settlements")
    public String distributorSettlements() {
        return "ingredient/settlement-dashboard";
    }
    
    @GetMapping("/settlement")
    public String settlementDashboard() {
        return "ingredient/settlement-dashboard";
    }
}
