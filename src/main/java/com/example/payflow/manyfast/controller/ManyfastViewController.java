package com.example.payflow.manyfast.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManyfastViewController {
    
    @GetMapping("/manyfast")
    public String index() {
        return "forward:/manyfast/index.html";
    }
}
