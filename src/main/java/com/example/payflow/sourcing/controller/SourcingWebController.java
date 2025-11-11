package com.example.payflow.sourcing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SourcingWebController {
    
    @GetMapping("/sourcing")
    public String sourcingPage() {
        return "sourcing";
    }
}
