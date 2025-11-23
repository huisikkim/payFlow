package com.example.payflow.parlevel.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/parlevel")
@RequiredArgsConstructor
public class ParLevelWebController {
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dynamic Par Level - 재고 예측 발주");
        return "parlevel-dashboard";
    }
    
    @GetMapping("/{storeId}/settings")
    public String settings(@PathVariable String storeId, Model model) {
        model.addAttribute("storeId", storeId);
        model.addAttribute("pageTitle", "Par Level 설정");
        return "parlevel-settings";
    }
    
    @GetMapping("/{storeId}/predictions")
    public String predictions(@PathVariable String storeId, Model model) {
        model.addAttribute("storeId", storeId);
        model.addAttribute("pageTitle", "발주 예측");
        return "parlevel-predictions";
    }
    
    @GetMapping("/{storeId}/consumption")
    public String consumption(@PathVariable String storeId, Model model) {
        model.addAttribute("storeId", storeId);
        model.addAttribute("pageTitle", "소비 패턴 분석");
        return "parlevel-consumption";
    }
}
