package com.example.payflow.ainjob.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ainjob")
public class AinjobWebController {
    
    @GetMapping
    public String dashboard() {
        return "ainjob/dashboard";
    }
    
    @GetMapping("/applicants")
    public String applicantList() {
        return "ainjob/applicant-list";
    }
    
    @GetMapping("/applicants/create")
    public String applicantCreate() {
        return "ainjob/applicant-create";
    }
    
    @GetMapping("/applicants/{id}")
    public String applicantDetail(@PathVariable Long id) {
        return "ainjob/applicant-detail";
    }
    
    @GetMapping("/job-postings")
    public String jobPostingList() {
        return "ainjob/job-posting-list";
    }
    
    @GetMapping("/job-postings/create")
    public String jobPostingCreate() {
        return "ainjob/job-posting-create";
    }
    
    @GetMapping("/job-postings/{id}")
    public String jobPostingDetail(@PathVariable Long id) {
        return "ainjob/job-posting-detail";
    }
    
    @GetMapping("/applications")
    public String applicationList() {
        return "ainjob/application-list";
    }
}
