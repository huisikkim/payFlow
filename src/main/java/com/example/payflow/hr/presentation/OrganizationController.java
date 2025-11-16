package com.example.payflow.hr.presentation;

import com.example.payflow.hr.application.OrganizationService;
import com.example.payflow.hr.presentation.dto.OrganizationChartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hr/organization")
@RequiredArgsConstructor
public class OrganizationController {
    
    private final OrganizationService organizationService;
    
    @GetMapping("/chart")
    public ResponseEntity<List<OrganizationChartResponse>> getOrganizationChart() {
        List<OrganizationChartResponse> chart = organizationService.getOrganizationChart();
        return ResponseEntity.ok(chart);
    }
}
