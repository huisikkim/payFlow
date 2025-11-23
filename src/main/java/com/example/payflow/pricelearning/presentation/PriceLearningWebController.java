package com.example.payflow.pricelearning.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ingredient/price-learning")
public class PriceLearningWebController {
    
    @GetMapping
    public String dashboard() {
        return "ingredient/price-learning-dashboard";
    }
}
