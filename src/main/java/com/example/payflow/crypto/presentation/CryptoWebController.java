package com.example.payflow.crypto.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/crypto")
public class CryptoWebController {
    
    @GetMapping
    public String cryptoPage() {
        return "crypto/index";
    }
}
