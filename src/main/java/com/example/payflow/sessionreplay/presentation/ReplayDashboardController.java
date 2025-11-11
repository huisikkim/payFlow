package com.example.payflow.sessionreplay.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 세션 재생 대시보드 웹 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class ReplayDashboardController {
    
    /**
     * 세션 재생 대시보드 페이지
     */
    @GetMapping("/session-replay/dashboard")
    public String dashboard() {
        return "session-replay-dashboard";
    }
    
    /**
     * 세션 재생 플레이어 페이지
     */
    @GetMapping("/session-replay/player/{sessionId}")
    public String player(@PathVariable String sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        return "session-replay-player";
    }
}
