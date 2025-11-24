package com.example.payflow.specification.presentation;

import com.example.payflow.specification.application.SpecificationService;
import com.example.payflow.specification.presentation.dto.SpecificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/specification")
public class SpecificationWebController {
    
    private final SpecificationService service;
    
    public SpecificationWebController(SpecificationService service) {
        this.service = service;
    }
    
    @GetMapping
    public String index(Model model) {
        try {
            List<SpecificationResponse> specifications = service.getAllSpecifications();
            model.addAttribute("specifications", specifications);
        } catch (Exception e) {
            log.error("Failed to load specifications", e);
            model.addAttribute("error", "명세표 목록을 불러올 수 없습니다");
        }
        return "specification/index";
    }
    
    @GetMapping("/upload")
    public String uploadPage() {
        return "specification/upload";
    }
    
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            SpecificationResponse spec = service.getSpecification(id);
            model.addAttribute("specification", spec);
        } catch (Exception e) {
            log.error("Failed to load specification", e);
            model.addAttribute("error", "명세표를 찾을 수 없습니다");
        }
        return "specification/detail";
    }
}
