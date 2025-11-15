package com.example.payflow.hr.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/hr")
public class HrWebController {
    
    @GetMapping("/attendance")
    public String attendancePage() {
        return "hr-attendance";
    }
    
    @GetMapping("/leave")
    public String leavePage() {
        return "hr-leave";
    }
}
