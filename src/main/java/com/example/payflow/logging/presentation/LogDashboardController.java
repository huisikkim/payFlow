package com.example.payflow.logging.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 로그 대시보드 웹 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class LogDashboardController {
    
    /**
     * 로그 분석 대시보드 페이지
     */
    @GetMapping("/logs/dashboard")
    public String dashboard() {
        return "log-dashboard";
    }
}
