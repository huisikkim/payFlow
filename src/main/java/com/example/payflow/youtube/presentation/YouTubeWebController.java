package com.example.payflow.youtube.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/youtube")
public class YouTubeWebController {

    /**
     * YouTube 인기 급상승 영상 페이지
     */
    @GetMapping("/popular")
    public String popularVideosPage() {
        return "youtube/popular";
    }
}
