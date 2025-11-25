package com.example.payflow.crypto.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//@Controller  // 🔒 코인 비교 기능 비활성화 - 나중에 다시 활성화하려면 주석 해제
@RequestMapping("/crypto")
public class CryptoWebController {
    
    @GetMapping
    public String cryptoPage() {
        return "crypto/index";
    }
    
    @GetMapping("/comparison")
    public String comparisonPage() {
        return "crypto/comparison";
    }
}
