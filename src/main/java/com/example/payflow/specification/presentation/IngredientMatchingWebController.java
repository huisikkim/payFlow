package com.example.payflow.specification.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 재료 매칭 웹 페이지 컨트롤러
 */
@Controller
@RequestMapping("/ingredients")
public class IngredientMatchingWebController {
    
    /**
     * 재료 매칭 테스트 페이지
     */
    @GetMapping("/matching-test")
    public String matchingTestPage() {
        return "ingredient/matching-test";
    }
}
