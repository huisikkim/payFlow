package com.example.payflow.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    
    @Value("${toss.payments.client-key}")
    private String clientKey;
    
    @GetMapping("/")
    public String home() {
        return "home";
    }
    
    @GetMapping("/payment")
    public String payment(Model model) {
        model.addAttribute("clientKey", clientKey);
        return "index";
    }
    
    @GetMapping("/success")
    public String success() {
        return "success";
    }
    
    @GetMapping("/fail")
    public String fail() {
        return "fail";
    }
    
    @GetMapping("/chatbot")
    public String chatbot() {
        return "chatbot";
    }
}
