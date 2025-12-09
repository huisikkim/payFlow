package com.example.payflow.crypto.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 거래소 비교 화면 컨트롤러
 */
@Controller
public class CryptoComparisonWebController {
    
    @GetMapping("/crypto/comparison")
    public String comparisonPage() {
        return "crypto-comparison";
    }
    
    @GetMapping("/crypto/api-docs")
    public String apiDocsPage() {
        return "crypto-api-docs";
    }
}
