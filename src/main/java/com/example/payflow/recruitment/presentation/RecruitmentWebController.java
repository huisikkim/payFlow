package com.example.payflow.recruitment.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/recruitment")
public class RecruitmentWebController {
    
    @GetMapping("/jobs")
    public String jobList() {
        return "recruitment/job-list";
    }
    
    @GetMapping("/jobs/{id}")
    public String jobDetail(@PathVariable Long id) {
        return "recruitment/job-detail";
    }
    
    @GetMapping("/candidates")
    public String candidateList() {
        return "recruitment/candidate-list";
    }
    
    @GetMapping("/candidates/{id}")
    public String candidateDetail(@PathVariable Long id) {
        return "recruitment/candidate-detail";
    }
    
    @GetMapping("/applications")
    public String applicationList() {
        return "recruitment/application-list";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "recruitment/dashboard";
    }
}
